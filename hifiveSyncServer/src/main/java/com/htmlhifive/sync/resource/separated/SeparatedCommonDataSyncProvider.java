/*
 * Copyright (C) 2012 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.htmlhifive.sync.resource.separated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityExistsException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.commondata.CommonDataBean;
import com.htmlhifive.sync.commondata.CommonDataRepository;
import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.NotFoundException;
import com.htmlhifive.sync.resource.SyncProvider;
import com.htmlhifive.sync.resource.SyncRequestHeader;
import com.htmlhifive.sync.resource.SyncResponseHeader;

/**
 * 共通データを専用のデータエンティティ、リポジトリを使用して永続化する共通データ管理サービス実装.<br>
 * {@link SyncRequestHeader 同期リクエストヘッダ}、{@link SyncResponseHeader 同期レスポンスヘッダ} でデータを受渡します.<br>
 * {@link CommonDataBean 共通データエンティティ}を、専用のリポジトリを用いて永続化します.
 *
 * @author kishigam
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class SeparatedCommonDataSyncProvider implements SyncProvider {

	/**
	 * Sync共通データのリポジトリ.
	 */
	@Resource
	private CommonDataRepository repository;

	/**
	 * リソースに対応する共通データを返します.<br>
	 *
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @return リソースからのレスポンスヘッダ
	 */
	@Override
	public SyncResponseHeader getCommonData(SyncRequestHeader requestHeader) {

		CommonDataBean common = findBean(requestHeader.getSyncDataId());

		SyncResponseHeader responseHeader = common.createResponseHeader();
		// クエリ情報は共通データとして管理していないため、リクエストヘッダからコピーする
		responseHeader.setQueryMap(requestHeader.getQueryMap());

		return responseHeader;
	}

	/**
	 * 指定された時刻以降に更新された全てのリソースに対応する共通データを返します. ロックは考慮しません.
	 *
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @return リソースID文字列をKey、リソースからのレスポンスヘッダをValueとするMap
	 */
	@Override
	public Map<String, SyncResponseHeader> getCommonDataModifiedSince(SyncRequestHeader requestHeader) {

		List<CommonDataBean> commons = repository.findModified(requestHeader.getDataModelName(),
				requestHeader.getLastSyncTime());

		Map<String, SyncResponseHeader> resultMap = new HashMap<>();
		for (CommonDataBean common : commons) {

			SyncResponseHeader responseHeader = common.createResponseHeader();
			// クエリ情報は共通データとして管理していないため、リクエストヘッダからコピーする
			responseHeader.setQueryMap(requestHeader.getQueryMap());

			resultMap.put(common.getResourceIdStr(), responseHeader);
		}

		return resultMap;
	}

	/**
	 * リソースに対応する共通データに対し、ロックを設定します.<br>
	 *
	 * @param responseHeader リソースからのレスポンスヘッダ
	 */
	@Override
	public boolean getLock(SyncRequestHeader requestHeader) {

		CommonDataBean common = findBean(requestHeader.getSyncDataId());
		if (!common.getLockKey().equals(requestHeader.getStorageId())) {
			return false;
		}
		common.setLockKey(requestHeader.getStorageId());
		repository.save(common);

		return true;
	}

	/**
	 * リソースに対応する共通データに対し、ロックを解除して更新します.
	 *
	 * @param responseHeader リソースからのレスポンスヘッダ
	 */
	@Override
	public void releaseLock(SyncResponseHeader responseHeader) {

		CommonDataBean common = findBean(responseHeader.getSyncDataId());
		if (!common.getLockKey().equals(responseHeader.getStorageId())) {

			throw new IllegalStateException("this data is locked by another.");
		}
		common.setLockKey(null);
		repository.save(common);
	}

	/**
	 * 新規リソースに対応する共通データを生成し、保存します.
	 *
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @param targetResourceIdStr リソースエレメントに固有のID文字列
	 * @return リソースからのレスポンスヘッダ
	 */
	@Override
	public SyncResponseHeader saveNewCommonData(SyncRequestHeader requestHeader, String targetResourceIdStr) {

		String newSyncDataId = generateSyncDataId(requestHeader);

		if (repository.exists(newSyncDataId)) {

			EntityExistsException cause = new EntityExistsException("duplicated common data : syncDataId = "
					+ newSyncDataId);
			throw new BadRequestException("inconsistent data exist", cause);
		}

		CommonDataBean newCommon = new CommonDataBean(newSyncDataId, requestHeader, targetResourceIdStr);

		repository.save(newCommon);

		SyncResponseHeader responseHeader = newCommon.createResponseHeader();

		// 新規登録の場合はストレージローカルIDを設定
		responseHeader.setStorageLocalId(requestHeader.getStorageLocalId());

		return responseHeader;
	}

	/**
	 * リソースに対応する共通データを更新し、保存します.<br>
	 * ロックは開放されます.
	 *
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @return リソースからのレスポンスヘッダ
	 */
	@Override
	public SyncResponseHeader saveUpdatedCommonData(SyncRequestHeader requestHeader) {

		CommonDataBean updatingCommon = findBean(requestHeader.getSyncDataId());

		updatingCommon.modifiy(requestHeader);

		repository.save(updatingCommon);

		SyncResponseHeader responseHeader = updatingCommon.createResponseHeader();

		return responseHeader;
	}

	/**
	 * 共通データのキーとなる同期データIDを生成します.<br>
	 * クライアントのストレージIDと、クライアント内で新規データに対して採番されたストレージローカルIDを使用します.
	 *
	 * @param header リソースへのリクエストヘッダ
	 * @return 同期データID
	 */
	private String generateSyncDataId(SyncRequestHeader header) {
		return header.getStorageId() + header.getStorageLocalId();
	}

	/**
	 * 共通データのキーとなる同期データIDで共通データエンティティを検索し、返します.<br>
	 * 取得できない場合、{@link NotFoundException}をスローします.
	 *
	 * @param syncDataId 同期データID
	 * @return 共通データエンティティ
	 * @throws NotFoundException IDからエンティティが取得できなかったとき
	 */
	private CommonDataBean findBean(String syncDataId) {

		CommonDataBean common = repository.findOne(syncDataId);

		if (common == null) {
			throw new NotFoundException("entity not found");
		}
		return common;
	}
}
