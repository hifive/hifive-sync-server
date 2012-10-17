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
package com.htmlhifive.sync.common;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityExistsException;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.resource.SyncAction;

/**
 * リソースアイテム共通データを更新するサービス実装.<br>
 * リポジトリへアクセスし、リソースアイテム共通データに対する操作を実行します.
 *
 * @author kishigam
 */
@Service
public class ResourceItemCommonDataRepositoryService implements ResourceItemCommonDataService,
		ResourceItemCommonDataDirectModifier {

	/**
	 * リソースアイテム共通データのリポジトリ.
	 */
	@Resource
	private ResourceItemCommonDataRepository repository;

	/**
	 * リソースアイテム共通データのIDオブジェクトで共通データエンティティを検索し、返します.<br>
	 *
	 * @param id リソースアイテム共通データID
	 * @return 共通データエンティティ
	 */
	@Override
	public ResourceItemCommonData currentCommonData(ResourceItemCommonDataId id) {

		ResourceItemCommonData common = repository.findOne(id);

		if (common == null) {
			throw new BadRequestException("itemCommonData not found : " + id.getResourceName() + "-"
					+ id.getResourceItemId());
		}
		return common;
	}

	/**
	 * 指定されたリソースで、指定された時刻以降に更新されたリソースアイテムの共通データを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param lastDownloadTime 前回下り更新時刻
	 * @return 共通データエンティティのリスト
	 */
	@Override
	public List<ResourceItemCommonData> modifiedCommonData(String resourceName, long lastDownloadTime) {

		return repository.findModified(resourceName, lastDownloadTime);
	}

	/**
	 * リソース名、そのリソースごとのアイテムにおけるIDで共通データエンティティを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 対象リソースアイテムのID
	 * @return 共通データエンティティ
	 */
	@Override
	public ResourceItemCommonData currentCommonData(String resourceName, String targetItemId) {

		ResourceItemCommonData common = repository.findByTargetItemId(resourceName, targetItemId);

		if (common == null) {
			throw new BadRequestException("itemCommonData not found : " + resourceName + "-" + targetItemId);
		}
		return common;
	}

	/**
	 * 新規リソースに対応する共通データを保存します.
	 *
	 * @param common リソースアイテム共通データ
	 * @return 保存された共通データ
	 */
	@Override
	public ResourceItemCommonData saveNewCommonData(ResourceItemCommonData common) {

		if (repository.exists(common.getId())) {

			EntityExistsException cause = new EntityExistsException("duplicated common data : id = " + common.getId());
			throw new BadRequestException("inconsistent data exists", cause);
		}

		return repository.save(common);
	}

	/**
	 * リソースに対応する共通データを保存します.<br>
	 *
	 * @param common リソースアイテム共通データ
	 * @return 保存された共通データ
	 */
	@Override
	public ResourceItemCommonData saveUpdatedCommonData(ResourceItemCommonData common) {

		return repository.save(common);
	}

	/**
	 * リソースアイテム共通クラスを指定された情報で更新します.<br>
	 * 指定されたアクションが{@link SyncAction#CREATE}の場合、は新規登録されます.
	 *
	 * @param resourceName リソース名
	 * @param resourceItemId リソースアイテムID
	 * @param targetItemId ターゲットアイテムID
	 * @param action アクション
	 * @param updateTime 更新時刻
	 */
	@Override
	public void modify(String resourceName, String resourceItemId, String targetItemId, SyncAction action,
			long updateTime) {

		ResourceItemCommonDataId id = new ResourceItemCommonDataId(resourceName, resourceItemId);

		ResourceItemCommonData common = repository.findOne(id);

		if (action == SyncAction.CREATE) {
			if (common != null) {
				throw new SyncException("resource item common data has already exists.  resource : " + resourceName
						+ " , resourceItemId : " + resourceItemId);
			}
		}

		common = new ResourceItemCommonData(id, targetItemId);

		common.modify(action, updateTime);

		repository.save(common);
	}
}
