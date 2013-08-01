/*
 * Copyright (C) 2012-2013 NS Solutions Corporation
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
package com.htmlhifive.sync.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataRepository;
import com.htmlhifive.sync.resource.common.SyncAction;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncRequestCommonData;

/**
 * @author kishigam
 */
public class DefaultSynchronizer implements Synchronizer {

	/**
	 * sync機能の動作設定パラメータオブジェクト
	 */
	private SyncConfigurationParameter syncConfigurationParameter;

	/**
	 * JPA EntityManager.<br>
	 * getNewではキー重複を検出するために、{@link JpaRepository JpaRepository}を経由せず、直接使用します.
	 */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * 共通データのバージョン管理データのリポジトリ
	 */
	@Autowired
	private ResourceItemCommonDataRepository resourceItemCommonDataRepository;

	/**
	 * 競合発生時の競合戦略クラスインスタンス.
	 */
	private UpdateStrategy defaultUpdateStrategy;

	/**
	 * 指定されたIDを持つだけの新規リソースアイテム共通データを生成します.<br/>
	 * このメソッドにより共通データを生成した後、{@link DefaultSynchronizer#modify(ResourceItemCommonData)} メソッドを使用して、内容を更新する必要があります.
	 *
	 * @param itemCommonId リソースアイテム共通データのIDオブジェクト
	 * @return 生成された共通データ
	 */
	@Override
	public ResourceItemCommonData getNew(ResourceItemCommonDataId itemCommonId) {

		ResourceItemCommonData common = new ResourceItemCommonData(itemCommonId);

		try {
			entityManager.persist(common);
			entityManager.flush();
		} catch (PersistenceException e) {
			// キー重複の可能性(厳密ではない)
			if (!(e.getCause() instanceof ConstraintViolationException)) {
				throw e;
			}
			common.setSyncAction(SyncAction.DUPLICATE);
		}
		return common;
	}

	/**
	 * 指定されたIDを持つリソースアイテム共通データを悲観的ロック("for update")を用いて取得します.
	 *
	 * @param itemCommonId リソースアイテム共通データのIDオブジェクト
	 * @return リソースアイテム共通データ
	 */
	@Override
	public ResourceItemCommonData getForUpdate(ResourceItemCommonDataId itemCommonId) {

		return resourceItemCommonDataRepository.findOneForUpdate(itemCommonId);
	}

	/**
	 * 指定された対象リソースアイテムのID値を持ち、指定時刻以降に更新されているリソースアイテム共通データを取得します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemIdList 対象リソースアイテムのID値のリスト
	 * @param modifiedSince 検索に用いる時刻
	 * @return 更新されているリソースアイテム共通データ
	 */
	@Override
	public List<ResourceItemCommonData> getModified(String resourceName, List<String> targetItemIdList,
			long modifiedSince) {

		List<ResourceItemCommonData> commonList = new ArrayList<>();

		// IDのソート順に取得
		List<String> tempList = new ArrayList<>(targetItemIdList);
		Collections.sort(tempList);

		for (String id : tempList) {

			ResourceItemCommonData common = resourceItemCommonDataRepository.findModified(resourceName, id,
					modifiedSince);

			if (common != null) {
				commonList.add(common);
			}
		}

		return commonList;
	}

	/**
	 * 指定された対象リソースアイテムのID値を持ち、指定時刻以降に更新されているリソースアイテム共通データを悲観的ロック("for update")を用いて取得します.<br/>
	 * ID値の順にソートして実行されるため、返されるリストは元のID値リストの順とは異なる場合があります.
	 *
	 * @param resourceName リソース名
	 * @param targetItemIdList 対象リソースアイテムのID値のリスト
	 * @param modifiedSince 検索に用いる時刻
	 * @return 更新されているリソースアイテム共通データ
	 */
	@Override
	public List<ResourceItemCommonData> getModifiedForUpdate(String resourceName, List<String> targetItemIdList,
			long modifiedSince) {
		List<ResourceItemCommonData> commonList = new ArrayList<>();

		// IDのソート順に取得
		List<String> tempList = new ArrayList<>(targetItemIdList);
		Collections.sort(tempList);

		for (String id : tempList) {

			ResourceItemCommonData common = resourceItemCommonDataRepository.findModifiedForUpdate(resourceName, id,
					modifiedSince);

			if (common != null) {
				commonList.add(common);
			}
		}

		return commonList;
	}

	/**
	 * リソースアイテム共通データのバージョン比較により、リソースアイテムの更新競合が発生しているときtrueを返します.
	 *
	 * @param clientItemCommon update(/delete)対象リソースアイテムの最終更新時刻
	 * @param currentItemCommon サーバで保持している現在の共通データ
	 * @return 競合が発生している場合true.
	 */
	@Override
	public boolean isConflicted(ResourceItemCommonData clientItemCommon, ResourceItemCommonData currentItemCommon,
			SyncRequestCommonData requestCommon) {

		if (currentItemCommon.getLastModified() <= clientItemCommon.getLastModified()) {
			return false;
		}

		// 多重化リクエストの各リクエストが同一リソースアイテムを更新する場合は競合としない
		// syncリクエスト共通データの同期時刻に更新されていれば、それに該当すると判断する
		return currentItemCommon.getLastModified() != requestCommon.getSyncTime();
	}

	/**
	 * リソースアイテム共通データを指定されたアイテムの内容で更新します.
	 *
	 * @param itemCommon リソースアイテム共通データ
	 * @return 更新後のリソースアイテム共通データ
	 */
	@Override
	public ResourceItemCommonData modify(ResourceItemCommonData itemCommon) {

		ResourceItemCommonData saved = resourceItemCommonDataRepository.save(itemCommon);
		return saved;
	}

	@Override
	public SyncConfigurationParameter getSyncConfigurationParameter() {
		return syncConfigurationParameter;
	}

	@Override
	public void setSyncConfigurationParameter(SyncConfigurationParameter syncConfigurationParameter) {
		this.syncConfigurationParameter = syncConfigurationParameter;
	}

	@Override
	public UpdateStrategy getDefaultUpdateStrategy() {
		return defaultUpdateStrategy;
	}

	@Override
	public void setDefaultUpdateStrategy(UpdateStrategy defaultUpdateStrategy) {
		this.defaultUpdateStrategy = defaultUpdateStrategy;
	}
}
