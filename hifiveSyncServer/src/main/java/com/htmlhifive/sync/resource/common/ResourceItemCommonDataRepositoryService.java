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
package com.htmlhifive.sync.resource.common;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	 * 新規データの永続化に使用するEntityManager.
	 *
	 * @see {@link this#saveNewCommonData(ResourceItemCommonData)}
	 */
	@PersistenceContext
	private EntityManager entitymanager;

	/**
	 * リソースアイテム共通データのIDオブジェクトで共通データエンティティを検索し、返します.<br>
	 *
	 * @param id リソースアイテム共通データID
	 * @return 共通データエンティティ
	 */
	@Override
	public ResourceItemCommonData currentCommonData(ResourceItemCommonDataId id) {

		return repository.findOne(id);
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

		return repository.findByTargetItemId(resourceName, targetItemId);
	}

	/**
	 * リソース名、そのリソースごとのアイテムにおける識別子で共通データエンティティを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 対象リソースアイテムの識別子
	 * @return 共通データエンティティ
	 */
	@Override
	public List<ResourceItemCommonData> currentCommonData(String resourceName, List<String> targetItemIds) {

		// 空のリストを渡すとHibernateが例外をスローする.
		// nullを1件含むリストに置き換えて実行してもよいが、検索結果なしを表す空リストを生成して返す
		if (targetItemIds.isEmpty()) {
			return Collections.emptyList();
		}

		return repository.findByTargetItemIds(resourceName, targetItemIds);
	}

	/**
	 * リソースアイテム共通データのIDオブジェクトで共通データエンティティを"for update"で検索し、返します.<br>
	 *
	 * @param id リソースアイテム共通データID
	 * @return 共通データエンティティ
	 */
	@Override
	public ResourceItemCommonData currentCommonDataForUpdate(ResourceItemCommonDataId id) {

		return repository.findOneForUpdate(id);
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
	 * 指定されたリソースで、指定された時刻以降に更新されたリソースアイテムの共通データを検索し、返します.<br>
	 * 検索結果は、指定された対象リソースアイテム識別子に該当するものに限定されます.
	 *
	 * @param resourceName リソース名
	 * @param lastDownloadTime 前回下り更新時刻
	 * @param targetItemIds リソースアイテムごとの識別子(複数可能)
	 * @return 共通データエンティティのリスト
	 */
	@Override
	public List<ResourceItemCommonData> modifiedCommonData(String resourceName, long lastDownloadTime,
			List<String> targetItemIds) {

		// 空のリストを渡すとHibernateが例外をスローする.
		// nullを1件含むリストに置き換えて実行してもよいが、検索結果なしを表す空リストを生成して返す
		if (targetItemIds.isEmpty()) {
			return Collections.emptyList();
		}

		return repository.findModified(resourceName, lastDownloadTime, targetItemIds);
	}

	/**
	 * 新規リソースに対応する共通データを保存します.
	 *
	 * @param common リソースアイテム共通データ
	 * @return 保存された共通データ
	 */
	@Override
	public ResourceItemCommonData saveNewCommonData(ResourceItemCommonData common) {

		// JPARepository#saveではキー重複の時EntityManager#mergeが呼ばれてしまう可能性があるため、EntityManager#persistを使用する
		// また、flushしないとトランザクションコミット時まで一意制約違反が遅れて発生してしまうためここでflushする
		entitymanager.persist(common);
		entitymanager.flush();

		return common;
	}

	/**
	 * リソースに対応する共通データを保存します.<br>
	 *
	 * @param common リソースアイテム共通データ
	 * @return 保存された共通データ
	 */
	@Override
	public ResourceItemCommonData saveUpdatedCommonData(ResourceItemCommonData common) {

		if (!repository.exists(common.getId())) {

			throw new BadRequestException("ResourceItemCommonData is not found. : " + common.getId().getResourceName()
					+ " , " + common.getId().getResourceItemId());
		}

		return repository.save(common);
	}

	/**
	 * リソースアイテム共通クラスを指定された情報で更新します.<br>
	 * 指定されたアクションが{@link SyncAction#CREATE}の場合は新規登録されます.
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
			common = new ResourceItemCommonData(id, targetItemId);
		}

		common.modify(action, updateTime);

		repository.save(common);
	}
}
