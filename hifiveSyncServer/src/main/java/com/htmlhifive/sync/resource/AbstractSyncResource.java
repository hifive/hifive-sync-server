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

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.common.ResourceItemCommonDataRepository;
import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.lock.LockCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * リソースの抽象実装クラス.<br>
 * このクラスを継承して作成される各リソースクラスで共通に行われる処理を実装しています.
 *
 * @author kishigam
 * @param <I> リソースアイテムの型
 */
public abstract class AbstractSyncResource<I> implements SyncResource<I> {

	/**
	 * このリソースが受け付けるアイテム型への変換を行うコンバータ.
	 */
	@Resource(type = JsonResourceItemConverter.class)
	private ResourceItemConverter<I> defaultItemConverter;

	/**
	 * 共通データのリポジトリ.
	 */
	@Resource
	private ResourceItemCommonDataRepository repository;

	/**
	 * ロック方式の実装オブジェクト.<br>
	 */
	private LockStrategy lockStrategy;

	/**
	 * 競合発生時の競合解決を行う更新戦略オブジェクト.
	 */
	private UpdateStrategy updateStrategy;

	/**
	 * 指定されたリソースアイテム共通データに対応するリソースアイテムを取得します.
	 *
	 * @param downloadCommon 下り更新共通データ
	 * @param itemCommonData リソースアイテム共通データ
	 * @return リソースアイテムのラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper<I> get(DownloadCommonData downloadCommon, ResourceItemCommonData itemCommonData) {

		//TODO: 悲観・排他ロックのチェック

		// 引数の共通データにはリソース名が含まれていない可能性があるため、ここでIDを再生成
		ResourceItemCommonDataId id = new ResourceItemCommonDataId(name(), itemCommonData.getId().getResourceItemId());

		ResourceItemCommonData common = currentCommonData(id);

		I item = doGet(common.getTargetItemId());

		return new ResourceItemWrapper<>(common, item);
	}

	/**
	 * クエリの条件に合致する全リソースアイテムを取得します.
	 *
	 * @param syncCommon 共通データ
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 */
	@Override
	public List<ResourceItemWrapper<I>> getByQuery(SyncCommonData syncCommon, ResourceQueryConditions query) {

		//TODO: 悲観・排他ロックのチェック

		List<ResourceItemCommonData> commonDataList = repository.findModified(name(), query.getLastDownloadTime());

		Map<I, ResourceItemCommonData> items = doGetByQuery(commonDataList, query.getConditions());

		List<ResourceItemWrapper<I>> resultList = new ArrayList<>();
		for (I item : items.keySet()) {

			ResourceItemCommonData common = items.get(item);

			ResourceItemWrapper<I> itemWrapper = new ResourceItemWrapper<>(common, item);

			resultList.add(itemWrapper);
		}

		return resultList;
	}

	/**
	 * リソースアイテムを新規登録します.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 新規登録されたアイテムの情報を含むラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper<I> create(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon, I item) {

		try {

			// 成功すると共通データで管理する各リソースアイテムのIDが返され、登録を行ったアイテムがサーバで管理される
			String targetItemId = doCreate(item);

			// 対象リソースアイテムのID、更新時刻を設定し、リソースアイテム共通データを新規保存
			itemCommon.setTargetItemId(targetItemId);
			itemCommon.setLastModified(uploadCommon.getLastUploadTime());
			saveNewCommonData(itemCommon);

			// 登録されたリソースアイテム共通データ、アイテムをリターン
			itemCommon.setConflictType(SyncConflictType.NONE);
			return new ResourceItemWrapper<>(itemCommon, item);

		} catch (DuplicateIdException e) {

			// キー重複の場合、共通データ、リソースアイテムには現在サーバで管理されているものを設定する
			ResourceItemCommonData currentCommon = currentCommonData(itemCommon.getId().getResourceName(),
					e.getDuplicatedTargetItemId());

			currentCommon.setConflictType(SyncConflictType.DUPLICATE_ID);
			return new ResourceItemWrapper<>(currentCommon, itemType().cast(e.getCurrentItem()));
		}
	}

	/**
	 * リソースアイテムを指定されたアイテムの内容で更新します.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 更新されたアイテムの情報を含むラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper<I> update(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon, I item) {

		return updateOrDelete(uploadCommon, itemCommon, item);
	}

	/**
	 * リクエストヘッダが指定するリソースアイテムを削除します.<br>
	 * 競合の解決方法によって、実際にはアイテムが更新される可能性があります.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @return 削除、あるいは更新されたアイテムの情報を含むラッパーオブジェクト
	 */
	@Override
	public ResourceItemWrapper<I> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon) {

		return updateOrDelete(uploadCommon, itemCommon, null);
	}

	@Override
	public void lock(LockCommonData lockCommon, ResourceItemWrapper<I> itemWrapper) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void releaseLock(LockCommonData lockCommonData, ResourceItemWrapper<I> itemWrapper) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public List<ResourceItemCommonDataId> lockedItemInfo(LockCommonData lockCommonData) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public ResourceItemCommonData reserve(ResourceItemCommonDataId id) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * 更新、削除共通のデータ更新ロジックを実行します.<br>
	 * 更新および削除は、競合が発生した際の解決結果によって実際に行う更新処理が変わります.<br>
	 * (更新が実行される中で実際には削除処理が実行される、あるいはその逆)
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 更新されたアイテムの情報を含むラッパーオブジェクト
	 */
	private ResourceItemWrapper<I> updateOrDelete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon,
			I item) {

		// 更新前共通データの取得
		ResourceItemCommonData currentCommon = currentCommonData(itemCommon.getId());

		//TODO: 悲観・排他/共有ロックのチェック
		//		try {

		// 競合判定
		// 競合がなければ、更新対象は渡されたitemとなる
		I updateItem = item;
		if (conflict(uploadCommon.getSyncTime(), currentCommon)) {

			// サーバで保持しているアイテムを取得
			I currentItem = currentCommon.getAction() == SyncAction.DELETE ? null : doGet(currentCommon
					.getTargetItemId());

			try {
				// 競合解決、更新アイテムを決定し、更新対象のリソースアイテムを上書き
				updateItem = updateStrategy.resolveConflict(itemCommon, item, currentCommon, currentItem);

			} catch (ItemUpdatedException e) {

				// 更新を行わず、競合タイプ設定してリターン
				currentCommon.setConflictType(SyncConflictType.UPDATED);
				return new ResourceItemWrapper<>(currentCommon, currentItem);
			}
		}

		if (updateItem == null) {

			itemCommon.setAction(SyncAction.DELETE);
			doDelete(currentCommon.getTargetItemId());
		} else {
			itemCommon.setAction(SyncAction.UPDATE);
			if (currentCommon.getAction() == SyncAction.DELETE) {
				try {
					doCreate(updateItem);
				} catch (DuplicateIdException e) {
					// データ不整合のため、CONFLICTを返さない
					throw new SyncException("inconsistent data. Deleted", e);
				}
			}
			doUpdate(updateItem);
		}

		currentCommon.modify(itemCommon.getAction(), uploadCommon.getLastUploadTime());
		ResourceItemCommonData commonAfterUpdate = saveUpdatedCommonData(currentCommon);

		commonAfterUpdate.setConflictType(SyncConflictType.NONE);
		return new ResourceItemWrapper<>(commonAfterUpdate, updateItem);

		//		} finally {
		//			lockStrategy.unlock(uploadCommon, currentCommon, item);
		//		}
	}

	/**
	 * 単一データreadメソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * サブクラスでは与えられたIDが示すリソースアイテムを返すようにこのメソッドを実装します.
	 *
	 * @param targetItemId 対象リソースアイテムのID
	 * @return リソースアイテム
	 */
	protected abstract I doGet(String targetItemId);

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致するものを検索し、返します.
	 *
	 * @param commonDataList 共通データリスト
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @return 条件に合致するリソースアイテム(CommonDataを値として持つMap)
	 */
	protected abstract Map<I, ResourceItemCommonData> doGetByQuery(List<ResourceItemCommonData> commonDataList,
			Map<String, String[]> conditions);

	/**
	 * createメソッドのリソース別独自処理. <br>
	 * 追加されたデータのリソースIDを返す.
	 *
	 * @param newItem 生成内容を含むリソースアイテム
	 * @return 採番されたリソースアイテムID
	 * @throws DuplicateIdException 追加しようとしたデータのIDが重複する場合
	 */
	protected abstract String doCreate(I newItem) throws DuplicateIdException;

	/**
	 * updateEメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではIが示すリソースアイテムを与えられたアイテムの内容で更新するようにこのメソッドを実装します.
	 *
	 * @param item 更新内容を含むリソースアイテム
	 */
	protected abstract I doUpdate(I item);

	/**
	 * deleteメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではこのリソースでのアイテムのIDが示すリソースアイテムを削除するようにこのメソッドを実装します.<br>
	 * また、削除後のリソースアイテムを表すものとして、各リソースアイテムのIDのみが設定されたアイテム型オブジェクトを返す必要があります.
	 *
	 * @param targetItemId 各リソースにおけるアイテムのID
	 */
	protected abstract I doDelete(String targetItemId);

	/**
	 * リソースアイテム共通クラスだけを強制的に更新します.
	 *
	 * @param resourceItemId リソースアイテムID
	 * @param action アクション
	 * @param lastModified 更新時刻
	 */
	protected void doUpdateCommonDataForce(String resourceItemId, SyncAction updateAction, long updateTime) {

		ResourceItemCommonData common = currentCommonData(new ResourceItemCommonDataId(name(), resourceItemId));

		common.modify(updateAction, updateTime);

		repository.save(common);
	}

	/**
	 * オブジェクトをリソースアイテムの型に変換するためのコンバータオブジェクトを返します.
	 *
	 * @return コンバータ
	 */
	@Override
	public ResourceItemConverter<I> itemConverter() {

		return defaultItemConverter;
	}

	/**
	 * このリソースのリソース名を返します.
	 *
	 * @return リソース名
	 */
	@Override
	public String name() {

		SyncResourceService resourceAnnotation = this.getClass().getAnnotation(SyncResourceService.class);
		if (resourceAnnotation == null) {
			throw new SyncException("target resource must be annotated by SyncResourceService");
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
	public Class<I> itemType() {

		// この抽象クラスの型を取得
		ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();

		// この抽象クラスで1つ目の型変数に指定されているのがアイテムの型
		return (Class<I>) thisType.getActualTypeArguments()[0];
	}

	/**
	 * リソースのロック方式を実装したロック戦略実装を設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param lockManager セットする lockManager
	 */
	@Override
	public void setLockStrategy(LockStrategy lockStrategy) {
		this.lockStrategy = lockStrategy;
	}

	/**
	 * 競合発生時の更新方法を指定する更新戦略実装を設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param updateStrategy セットする updateStrategy
	 */
	@Override
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {
		this.updateStrategy = updateStrategy;
	}

	/**
	 * リソースアイテム共通データのIDオブジェクトで共通データエンティティを検索し、返します.<br>
	 *
	 * @param id リソースアイテム共通データID
	 * @return 共通データエンティティ
	 */
	private ResourceItemCommonData currentCommonData(ResourceItemCommonDataId id) {

		ResourceItemCommonData common = repository.findOne(id);

		if (common == null) {
			throw new BadRequestException("itemCommonData not found : " + id.getResourceName() + "-"
					+ id.getResourceItemId());
		}
		return common;
	}

	/**
	 * リソース名、そのリソースごとのアイテムにおけるIDで共通データエンティティを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId 対象リソースアイテムのID
	 * @return 共通データエンティティ
	 */
	private ResourceItemCommonData currentCommonData(String resourceName, String targetItemId) {

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
	private ResourceItemCommonData saveNewCommonData(ResourceItemCommonData common) {

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
	private ResourceItemCommonData saveUpdatedCommonData(ResourceItemCommonData common) {

		return repository.save(common);
	}

	/**
	 * リソースアイテム共通データのバージョン比較により、リソースアイテムの更新が実行できるか判定します.<br>
	 *
	 * @param forUpdate update(/delete)対象リソースアイテムの共通データ
	 * @param server サーバで保持している現在の共通データ
	 * @return update(/delete)できる場合true.
	 */
	private boolean conflict(long uploadTime, ResourceItemCommonData server) {

//		return server.getLastModified() > uploadTime;
	return true;
	}
}