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
package com.htmlhifive.sync.resource;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityExistsException;

import com.htmlhifive.sync.commondata.CommonData;
import com.htmlhifive.sync.commondata.CommonDataId;
import com.htmlhifive.sync.commondata.CommonDataRepository;
import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.exception.NotFoundException;
import com.htmlhifive.sync.exception.SyncException;

/**
 * リソースの抽象実装クラス.<br>
 * このクラスを継承して作成される各リソースクラスで共通に行われる処理を実装しています.
 *
 * @author kishigam
 * @param <T> リソースアイテムの型
 */
public abstract class AbstractSyncResource<T> implements SyncResource<T> {

	/**
	 * 共通データのリポジトリ.
	 */
	@Resource
	private CommonDataRepository repository;

	/**
	 * リソースごとに決まるロック方式のマネージャオブジェクト.<br>
	 */
	private LockManager lockManager;

	/**
	 * ロックエラー発生時の競合解決を行う更新戦略オブジェクト.
	 */
	private UpdateStrategy updateStrategy;

	/**
	 * 指定されたリソースアイテムIDを持つリソースアイテムを取得します.<br>
	 *
	 * @param resourceItemId リソースアイテムID
	 * @return リソースアイテムのラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper read(String resourceItemId) {

		CommonData common = currentCommonData(resourceItemId);

		T item = doRead(common.getTargetItemId());

		ResourceItemWrapper itemWrapper = new ResourceItemWrapper(common.getId().getResourceItemId());
		itemWrapper.setAction(common.getAction());
		itemWrapper.setLastModified(common.getLastModified());
		itemWrapper.setItem(item);

		return itemWrapper;
	}

	/**
	 * クエリの条件に合致する全リソースアイテムを取得します.<br>
	 * ロックは考慮しません.
	 *
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 */
	@Override
	public List<ResourceItemWrapper> readByQuery(ResourceQuery query) {

		List<CommonData> commonDataList = repository.findModified(getResourceName(), query.getLastDownloadTime());

		Map<T, CommonData> items = doReadByQuery(commonDataList, query.getConditions());

		List<ResourceItemWrapper> resultList = new ArrayList<>();
		for (T item : items.keySet()) {

			CommonData common = items.get(item);

			ResourceItemWrapper itemWrapper = new ResourceItemWrapper(common.getId().getResourceItemId());
			itemWrapper.setAction(common.getAction());
			itemWrapper.setLastModified(common.getLastModified());
			itemWrapper.setItem(item);

			resultList.add(itemWrapper);
		}

		return resultList;
	}

	/**
	 * リソースアイテムを新規追加登録します.
	 *
	 * @param itemWrapper 登録リソースアイテムのラッパーオブジェクト
	 * @return 登録されたリソースアイテムのラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper create(ResourceItemWrapper itemWrapper) {

		ResourceItemWrapper resultItemWrapper;
		try {
			T createItem = itemConverter().convert(itemWrapper.getItem(), getItemType());

			// 成功すると共通データで管理する各リソースアイテムのIDが返され、登録を行ったアイテムがサーバで管理される
			String targetItemId = doCreate(createItem);

			CommonData newCommon = saveNewCommonData(itemWrapper, targetItemId);

			resultItemWrapper = newCommon.generateItemWrapper();
			resultItemWrapper.setItem(createItem);

		} catch (DuplicateIdException e) {

			// キー重複の場合、共通データ、リソースアイテムには現在サーバで管理されているものを設定する
			CommonData existingCommon = currentCommonData(itemWrapper.getResourceItemId());

			resultItemWrapper = existingCommon.generateItemWrapper();
			resultItemWrapper.setResultType(SyncResultType.DUPLICATEDID);
			resultItemWrapper.setItem(e.getCurrentItem());
		}

		return resultItemWrapper;
	}

	/**
	 * リソースアイテムを更新します.
	 *
	 * @param itemWrapper 更新リソースアイテムのラッパーオブジェクト
	 * @return 更新されたリソースアイテムのラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper update(ResourceItemWrapper itemWrapper) {

		T updateItem = itemConverter().convert(itemWrapper.getItem(), getItemType());
		CommonData commonBeforUpdate = currentCommonData(itemWrapper.getResourceItemId());
		// ロックエラー判定
		if (!lockManager.canUpdate(itemWrapper, commonBeforUpdate)) {

			// サーバで保持しているアイテムを取得
			T serverItem = commonBeforUpdate.getAction() == SyncAction.DELETE ? null : doRead(commonBeforUpdate
					.getTargetItemId());

			ResourceItemWrapper conflictedItemWrapper = commonBeforUpdate.generateItemWrapper();
			conflictedItemWrapper.setItem(serverItem);

			try {
				// 競合解決、更新アイテムを決定し、更新対象のリソースアイテムを上書き
				updateItem = updateStrategy.resolveConflict(itemWrapper, conflictedItemWrapper, getItemType());

			} catch (ItemUpdatedException e) {
				// 更新を行わずリターン
				conflictedItemWrapper.setResultType(SyncResultType.UPDATED);
				return conflictedItemWrapper;
			}
		}

		doUpdate(updateItem);
		CommonData commonAfterUpdate = saveUpdatedCommonData(itemWrapper);

		lockManager.release(commonAfterUpdate);

		ResourceItemWrapper resultItemWrapper = commonAfterUpdate.generateItemWrapper();
		resultItemWrapper.setItem(updateItem);

		return resultItemWrapper;
	}

	/**
	 * リソースアイテムを削除します.
	 *
	 * @param itemWrapper 削除リソースアイテムのラッパーオブジェクト
	 * @return 更新されたリソースアイテムのラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper delete(ResourceItemWrapper itemWrapper) {

		T updateItem = null;

		CommonData commonBeforUpdate = currentCommonData(itemWrapper.getResourceItemId());
		// ロックエラー判定
		if (!lockManager.canUpdate(itemWrapper, commonBeforUpdate)) {

			// サーバで保持しているアイテムを取得
			T serverItem = commonBeforUpdate.getAction() == SyncAction.DELETE ? null : doRead(commonBeforUpdate
					.getTargetItemId());

			ResourceItemWrapper conflictedItemWrapper = commonBeforUpdate.generateItemWrapper();
			conflictedItemWrapper.setItem(serverItem);

			try {
				// 競合解決、更新アイテムを決定し、更新対象のリソースアイテムを上書き
				updateItem = updateStrategy.resolveConflict(itemWrapper, conflictedItemWrapper, getItemType());

			} catch (ItemUpdatedException e) {
				// 更新を行わずリターン
				conflictedItemWrapper.setResultType(SyncResultType.UPDATED);
				return conflictedItemWrapper;
			}
		}

		if (updateItem != null) {

			doUpdate(updateItem);

			// アクションを切り替える
			itemWrapper.setAction(SyncAction.UPDATE);
		} else {
			doDelete(commonBeforUpdate.getTargetItemId());
		}

		CommonData commonAfterUpdate = saveUpdatedCommonData(itemWrapper);

		lockManager.release(commonAfterUpdate);

		ResourceItemWrapper resultItemWrapper = commonAfterUpdate.generateItemWrapper();
		resultItemWrapper.setItem(updateItem);

		return resultItemWrapper;
	}

	/**
	 * 単一データreadメソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * サブクラスでは与えられたIDが示すリソースアイテムを返すようにこのメソッドを実装します.
	 *
	 * @param targetItemId 対象リソースアイテムのID
	 * @return リソースアイテム
	 */
	protected abstract T doRead(String targetItemId);

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致するものを検索し、返します.
	 *
	 * @param commonDataList 共通データリスト
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @return 条件に合致するリソースアイテム(CommonDataを値として持つMap)
	 */
	protected abstract Map<T, CommonData> doReadByQuery(List<CommonData> commonDataList,
			Map<String, String[]> conditions);

	/**
	 * createメソッドのリソース別独自処理. <br>
	 * 追加されたデータのリソースIDを返す.
	 *
	 * @param newItem 生成内容を含むリソースアイテム
	 * @return 採番されたリソースアイテムID
	 * @throws DuplicateIdException 追加しようとしたデータのIDが重複する場合
	 */
	protected abstract String doCreate(T newItem) throws DuplicateIdException;

	/**
	 * updateEメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではIが示すリソースアイテムを与えられたアイテムの内容で更新するようにこのメソッドを実装します.
	 *
	 * @param item 更新内容を含むリソースアイテム
	 */
	protected abstract void doUpdate(T item);

	/**
	 * deleteメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではこのリソースでのアイテムのIDが示すリソースアイテムを削除するようにこのメソッドを実装します.<br>
	 *
	 * @param targetItemId 各リソースにおけるアイテムのID
	 */
	protected abstract void doDelete(String targetItemId);

	/**
	 * このリソースのリソースアイテムを専用の型に変換するコンバータを返します.
	 *
	 * @return コンバータ
	 */
	protected ResourceItemConverter<T> itemConverter() {

		// TODO: コンフィグ化
		return new JsonResourceItemConverter<>();
	}

	/**
	 * このリソースのリソース名を返します.
	 *
	 * @return リソース名
	 */
	@Override
	public String getResourceName() {

		SyncResourceService resourceAnnotation = this.getClass().getAnnotation(SyncResourceService.class);
		if (resourceAnnotation == null) {
			throw new SyncException("target resource must be annotated by @SyncResourceService");
		}

		return resourceAnnotation.resourceName();
	}

	/**
	 * このリソースのアイテム型を返します.
	 *
	 * @return アイテムの型を表すClassオブジェクト
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getItemType() {

		// この抽象クラスの型を取得
		ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();

		// この抽象クラスで1つ目の型変数に指定されているのがアイテムの型
		return (Class<T>) thisType.getActualTypeArguments()[0];
	}

	/**
	 * リソースのロックを管理するマネージャを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param lockManager セットする lockManager
	 */
	@Override
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	/**
	 * リソースが楽観的ロックを使用している時にロックエラー発生時の更新方法を指定するストラテジーオブジェクトを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param updateStrategy セットする updateStrategy
	 */
	@Override
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {
		this.updateStrategy = updateStrategy;
	}

	/**
	 * 共通データのキーとなる同期データIDで共通データエンティティを検索し、返します.<br>
	 * 取得できない場合、{@link NotFoundException}をスローします.
	 *
	 * @param resourceItemId リソースアイテムID
	 * @return 共通データエンティティ
	 * @throws NotFoundException IDからエンティティが取得できなかったとき
	 */
	private CommonData currentCommonData(String resourceItemId) {

		CommonData common = repository.findOne(new CommonDataId(getResourceName(), resourceItemId));

		if (common == null) {
			throw new NotFoundException("entity not found");
		}
		return common;
	}

	/**
	 * 新規リソースに対応する共通データを生成し、保存します.
	 *
	 * @param itemWrapper リソースアイテムのラッパーオブジェクト
	 * @param targetItemId 各リソースアイテムのID
	 * @return 保存された共通データ
	 */
	private CommonData saveNewCommonData(ResourceItemWrapper itemWrapper, String targetItemId) {

		CommonData newCommon = new CommonData(new CommonDataId(getResourceName(), itemWrapper.getResourceItemId()),
				targetItemId);
		newCommon.modifiy(itemWrapper);

		if (repository.exists(newCommon.getId())) {

			EntityExistsException cause = new EntityExistsException("duplicated common data : id = "
					+ newCommon.getId());
			throw new BadRequestException("inconsistent data exists", cause);
		}

		return repository.save(newCommon);
	}

	/**
	 * リソースに対応する共通データを更新し、保存します.<br>
	 * ロックは開放されます.
	 *
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @return リソースからのレスポンスヘッダ
	 */
	private CommonData saveUpdatedCommonData(ResourceItemWrapper itemWrapper) {

		CommonData updatingCommon = currentCommonData(itemWrapper.getResourceItemId());

		updatingCommon.modifiy(itemWrapper);

		repository.save(updatingCommon);

		return updatingCommon;
	}

	/**
	 * リソースに対応する共通データに対し、ロックを設定します.<br>
	 *
	 * @param id 共通データID
	 * @param lockKey ロックキー
	 * @return 共通データ
	 */
	private CommonData getLock(String resourceItemId, String lockKey) {

		CommonData existingCommon = currentCommonData(resourceItemId);

		// TODO: ロックエラー判定→LockManager？
		existingCommon.setLockKey(lockKey);
		repository.save(existingCommon);

		return existingCommon;
	}

	/**
	 * リソースに対応する共通データに対し、ロックを解除して更新します.
	 *
	 * @param id 共通データID
	 * @param lockKey ロックキー
	 */
	private void releaseLock(String resourceItemId, String lockKey) {

		CommonData existingCommon = currentCommonData(resourceItemId);
		existingCommon.setLockKey(null);
		repository.save(existingCommon);
	}
}