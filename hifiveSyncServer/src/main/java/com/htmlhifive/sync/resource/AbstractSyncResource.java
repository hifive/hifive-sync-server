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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.security.authentication.LockedException;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataService;
import com.htmlhifive.sync.resource.lock.LockStrategy;
import com.htmlhifive.sync.resource.lock.ResourceLockStatusType;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.lock.LockCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * リソースの抽象実装クラス.<br>
 * このクラスを継承して作成される各リソースクラスで共通に行われる処理を実装しています.
 *
 * @author kishigam
 * @param <I> リソースアイテムの型
 */
@SuppressWarnings("deprecation")
public abstract class AbstractSyncResource<I> implements SyncResource<I> {

	/**
	 * このリソースのアイテムが必要とするロックの種類.<br>
	 * TODO 次期バージョンの実装で使用予定
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private ResourceLockStatusType requiredLockStatus;

	/**
	 * ロック方式の実装オブジェクト.<br>
	 * TODO 次期バージョンの実装で使用予定
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private LockStrategy lockStrategy;

	/**
	 * 競合発生時の競合解決を行う更新戦略オブジェクト.
	 */
	private UpdateStrategy updateStrategy;

	/**
	 * このリソースが受け付けるアイテム型への変換を行うコンバータ.
	 */
	@Resource
	private ResourceItemConverter<I> defaultItemConverter;

	/**
	 * リソースアイテム共通データを管理するサービス.
	 */
	@Resource
	private ResourceItemCommonDataService commonDataService;

	/**
	 * この抽象クラスに対して設定されている{@link this#get(SyncCommonData, List)}、{@link this#getByQuery(SyncCommonData,
	 * ResourceQueryConditions)}メソッド実行時のデータ検証回数.<br>
	 * 共通データとアイテムデータの整合性を指定された回数検証します.<br>
	 * 検証により不整合となった場合、{@link LockException}がスローされます.
	 */
	private int countOfVerification;

	/**
	 * 指定されたリソースアイテム共通データに対応するリソースアイテムを取得します.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommonData リソースアイテム共通データ
	 * @return リソースアイテムのラッパーオブジェクト
	 */
	@Override
	public List<ResourceItemWrapper<I>> get(SyncCommonData syncCommon, List<ResourceItemCommonData> commonDataList) {

		if (syncCommon == null) {
			throw new NullPointerException("SyncCommonData is null.");
		}

		// TODO 次期バージョンにて実装予定
		//		if (requiredLockStatus == ResourceLockStatusType.EXCLUSIVE) {
		//			for (ResourceItemCommonData itemCommon : commonDataList) {
		//				lockStrategy.checkReadLockStatus(syncCommon, itemCommon);
		//			}
		//		}

		List<ResourceItemCommonData> idRecreatingCommonDataList = new ArrayList<>();
		List<String> idList = new ArrayList<>();
		for (ResourceItemCommonData itemCommon : commonDataList) {

			// for updateで取得済みでない場合は取得(非 for update)
			ResourceItemCommonData common = itemCommon;
			if (!itemCommon.isForUpdate()) {

				// 引数の共通データにはリソース名が含まれていない可能性があるため、ここでIDを再生成
				ResourceItemCommonDataId id = new ResourceItemCommonDataId(name(), itemCommon.getId()
						.getResourceItemId());

				common = commonDataService.currentCommonData(id);
				if (common == null) {
					throw new BadRequestException("itemCommonData not found : " + id.getResourceName() + "-"
							+ id.getResourceItemId());
				}
			}

			idRecreatingCommonDataList.add(common);
			idList.add(common.getTargetItemId());
		}

		Map<String, I> items = doGet(idList.toArray(new String[] {}));
		List<ResourceItemWrapper<I>> resultList = wrapResourceItem(idRecreatingCommonDataList, items);

		return countOfVerification == 0 ? resultList : verify(resultList, countOfVerification);
	}

	/**
	 * クエリの条件に合致する全リソースアイテムを取得します.<br>
	 * この処理では「非 for update」での取得を行います.
	 *
	 * @param syncCommon 共通データ
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 */
	@Override
	public List<ResourceItemWrapper<I>> getByQuery(SyncCommonData syncCommon, ResourceQueryConditions query) {

		List<ResourceItemCommonData> commonDataList = commonDataService.modifiedCommonData(name(),
				query.getLastDownloadTime());

		// TODO 次期バージョンにて実装予定
		//		if (requiredLockStatus == ResourceLockStatusType.EXCLUSIVE) {
		//			for (ResourceItemCommonData itemCommon : commonDataList) {
		//				lockStrategy.checkReadLockStatus(syncCommon, itemCommon);
		//			}
		//		}

		List<String> idList = new ArrayList<>();
		for (ResourceItemCommonData common : commonDataList) {
			idList.add(common.getTargetItemId());
		}

		Map<String, I> items = doGetByQuery(query.getConditions(), idList.toArray(new String[] {}));
		List<ResourceItemWrapper<I>> resultList = wrapResourceItem(commonDataList, items);

		return countOfVerification == 0 ? resultList : verify(resultList, countOfVerification);
	}

	/**
	 * リソースアイテムデータとそれぞれに対応するリソースアイテム共通データを{@link ResourceItemWrapper}に格納し、そのリストを返します.
	 *
	 * @param commonDataList リソースアイテム共通データのコレクション
	 * @param items リソースアイテム(アイテムの識別子をkeyとするMap)
	 * @return リソースアイテム(ラッパーオブジェクト)のリスト
	 */
	private List<ResourceItemWrapper<I>> wrapResourceItem(Collection<ResourceItemCommonData> commonDataList,
			Map<String, I> items) {

		List<ResourceItemWrapper<I>> resultList = new ArrayList<>();
		for (ResourceItemCommonData common : commonDataList) {

			resultList.add(new ResourceItemWrapper<>(common, items.get(common.getTargetItemId())));
		}
		return resultList;
	}

	/**
	 * {@link this#countOfVerification}に設定された回数だけ共通データとアイテムデータの整合性を検証します.<br>
	 * 検証できなかった場合、{@link LockException}がスローされます.<br>
	 * 検証は、共通データを再取得し、前回取得した共通データと一致した場合に成功します.<br>
	 * 失敗した場合、指定回数に達していなければさらに再取得を行います.<br>
	 * 最終的に、検証に成功した共通データに対応するアイテムデータが返されます.
	 *
	 * @param itemWrapperList 検証するリソースアイテム(ラッパーオブジェクト)のリスト
	 * @param count 検証回数
	 * @return 検証後のリソースアイテム(ラッパーオブジェクト)のリスト
	 * @throws LockedException 検証できなかった場合
	 */
	private List<ResourceItemWrapper<I>> verify(List<ResourceItemWrapper<I>> itemWrapperList, int count) {

		boolean isVerified = true;

		// TODO: 共通データの一括取得
		Map<String, ResourceItemCommonData> comparisonCommonMap = new HashMap<>();
		for (ResourceItemWrapper<I> itemWrapper : itemWrapperList) {

			ResourceItemCommonData comparisonCommon = commonDataService.currentCommonData(itemWrapper
					.getItemCommonData().getId());
			comparisonCommonMap.put(comparisonCommon.getTargetItemId(), comparisonCommon);

			// 再取得した共通データとの差異がある場合検証失敗
			if (!comparisonCommon.equals(itemWrapper.getItemCommonData())) {
				isVerified = false;
			}
		}

		// 全てのリソースアイテムで検証成功したら終了
		if (isVerified) {
			return itemWrapperList;
		}

		// 最後まで検証できなかった場合、例外
		count--;
		if (count == 0) {
			throw new LockException("Item verification is failed.");
		}

		// 検証回数残がある場合、リソースアイテムを再取得
		Map<String, I> getAgain = doGet(comparisonCommonMap.keySet().toArray(new String[] {}));
		List<ResourceItemWrapper<I>> nextList = wrapResourceItem(comparisonCommonMap.values(), getAgain);

		return verify(nextList, count);
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

			// 共通データは存在しないはず
			if (commonDataService.currentCommonData(itemCommon.getId()) != null) {
				throw new SyncException("Inconsistent ResourceItemCommonData.: " + itemCommon.getId().getResourceName()
						+ "-" + itemCommon.getId().getResourceItemId());
			}

			// 対象リソースアイテムのIDを設定し、リソースアイテム共通データを新規生成
			itemCommon.setTargetItemId(targetItemId);
			// アクション、上り更新実行時刻を設定
			itemCommon.modify(itemCommon.getAction(), uploadCommon.getSyncTime());
			// 保存
			commonDataService.saveNewCommonData(itemCommon);

			// 登録されたリソースアイテム共通データ、アイテムをリターン
			itemCommon.setConflictType(SyncConflictType.NONE);
			return new ResourceItemWrapper<>(itemCommon, item);

		} catch (DuplicateIdException e) {

			// キー重複の場合、共通データ、リソースアイテムには現在サーバで管理されているものを設定する
			ResourceItemCommonData currentCommon = commonDataService.currentCommonData(itemCommon.getId()
					.getResourceName(), e.getDuplicatedTargetItemId());

			// 起こりえない(データ不整合)
			if (currentCommon == null) {
				throw new SyncException("itemCommonData not found : " + itemCommon.getId() + "-"
						+ e.getDuplicatedTargetItemId());
			}

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

		try {
			// TODO 次期バージョンにて実装予定
			//		if (requiredLockStatus != ResourceLockStatusType.UNLOCK) {
			//			lockStrategy.checkWriteLockStatus(uploadCommon, itemCommon, requiredLockStatus);
			//		}

			// for updateで取得済みでない場合は取得(ここでfor updateによって取得)
			ResourceItemCommonData currentCommon = itemCommon;
			if (!itemCommon.isForUpdate()) {
				currentCommon = commonDataService.currentCommonDataForUpdate(itemCommon.getId());
				if (currentCommon == null) {
					throw new BadRequestException("itemCommonData not found : " + itemCommon.getId().getResourceName()
							+ "-" + itemCommon.getId().getResourceItemId());
				}
			}
			// 競合判定
			// 競合がなければ、更新対象は渡されたitemとなる
			I updateItem = item;
			if (
			// TODO 次期バージョンにて実装予定
			//		requiredLockStatus == ResourceLockStatusType.UNLOCK &&
			isConflicted(itemCommon, currentCommon, uploadCommon)) {

				try {
					updateItem = resolveConflict(itemCommon, item, currentCommon);
				} catch (ItemUpdatedException e) {

					// 更新を行わずリターン(例外から取得した共通データには競合タイプがセットされている)

					I currentItem = itemType().cast(e.getCurrentItem());
					if (currentItem == null) {
						// nullはアイテムが削除されていることを表す
						// クライアントに返す値としては論理削除後の状態(IDのみ設定されたアイテムオブジェクト)とする.
						currentItem = doDelete(e.getConflictedCommonData().getTargetItemId());
					}

					return new ResourceItemWrapper<>(e.getConflictedCommonData(), currentItem);
				}
			}

			if (updateItem == null) {

				itemCommon.setAction(SyncAction.DELETE);
				// doDeleteの結果、IDのみ設定されたアイテムが返される
				updateItem = doDelete(currentCommon.getTargetItemId());
			} else {

				itemCommon.setAction(SyncAction.UPDATE);
				doUpdate(updateItem);
			}

			currentCommon.modify(itemCommon.getAction(), uploadCommon.getSyncTime());
			ResourceItemCommonData commonAfterUpdate = commonDataService.saveUpdatedCommonData(currentCommon);

			commonAfterUpdate.setConflictType(SyncConflictType.NONE);
			return new ResourceItemWrapper<>(commonAfterUpdate, updateItem);

		} finally {
			// TODO 次期バージョンにて実装予定
			//			lockStrategy.unlock(uploadCommon, itemCommon);
		}
	}

	/**
	 * 更新競合発生時にリソースアイテムデータおよび共通データの情報から競合解決を行います.<br>
	 * このリソースに設定されている競合解決戦略を実行します.
	 *
	 * @param itemCommon 今回の更新後リソースアイテム共通データ
	 * @param item 今回の更新後リソースアイテム
	 * @param currentCommon 現在のリソースアイテム共通データ
	 * @return 競合解決後のリソースアイテム
	 * @throws ItemUpdatedException 競合が解決できないとき
	 */
	private I resolveConflict(ResourceItemCommonData itemCommon, I item, ResourceItemCommonData currentCommon)
			throws ItemUpdatedException {

		// サーバで保持しているアイテムを取得
		// 論理削除のため、削除状態の場合は明示的にnullを用いる必要がある
		I currentItem;
		if (currentCommon.getAction() == SyncAction.DELETE) {
			currentItem = null;
		} else {
			currentItem = doGet(currentCommon.getTargetItemId()).get(currentCommon.getTargetItemId());
		}

		// 競合解決、更新アイテムを決定し、更新対象のリソースアイテムを上書き
		return updateStrategy.resolveConflict(itemCommon, item, currentCommon, currentItem);
	}

	/**
	 * 指定されたリソースアイテムをロックします.
	 *
	 * @param lockCommon ロック取得共通データ
	 * @param itemCommonDataList リソースアイテム共通データのリスト
	 * @return ロックしたアイテムの情報を含むラッパーオブジェクトのリスト
	 * @throws LockException ロックできなかった場合
	 */
	@Deprecated
	@Override
	public List<ResourceItemWrapper<I>> lock(LockCommonData lockCommon, List<ResourceItemCommonData> itemCommonDataList) {

		// TODO 次期バージョンにて実装予定
		return null;

	}

	/**
	 * 指定されたリソースアイテムのロックを開放します.
	 *
	 * @param lockCommon ロック取得共通データ
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @throws LockException ロックの開放に失敗した場合
	 */
	@Deprecated
	@Override
	public void releaseLock(LockCommonData lockCommon, List<ResourceItemCommonData> itemCommonList) {

		// TODO 次期バージョンにて実装予定
	}

	/**
	 * ロックされている全リソースアイテムの共通データを返します.<br>
	 *
	 * @param lockCommonData ロック取得共通データ
	 * @return リソースアイテム共通データのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	@Deprecated
	@Override
	public List<ResourceItemCommonData> lockedItemsList(LockCommonData lockCommonData) {

		// TODO 次期バージョンにて実装予定
		return null;
	}

	/**
	 * 他のリクエストの影響を防止するために、指定されたリソースアイテムを「予約」状態にします.<br>
	 * 更新やロックの対象とする全てのリソースアイテムを予約することで、デッドロックによる処理失敗の可能性をなくすことができます.
	 *
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @return アクセス権を取得したリソースアイテム共通データのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	@Override
	public List<ResourceItemCommonData> forUpdate(List<ResourceItemCommonData> itemCommonList) {

		// 予約対象をリソースアイテムIDでソートしたリストを生成(コピー)
		List<ResourceItemCommonData> sortedItemsList = new ArrayList<>(itemCommonList);
		Collections.sort(sortedItemsList);

		List<ResourceItemCommonData> reservedCommonDataList = new ArrayList<>();
		for (ResourceItemCommonData itemCommon : sortedItemsList) {

			// 引数の共通データにはリソース名が含まれていない可能性があるため、ここでIDを再生成
			ResourceItemCommonDataId id = new ResourceItemCommonDataId(name(), itemCommon.getId().getResourceItemId());

			// for updateで取得
			ResourceItemCommonData currentForUpdate = commonDataService.currentCommonDataForUpdate(id);
			if (currentForUpdate == null) {
				throw new BadRequestException("itemCommonData not found : " + id.getResourceName() + "-"
						+ id.getResourceItemId());
			}

			currentForUpdate.setForUpdate(true);
			reservedCommonDataList.add(currentForUpdate);
		}

		return reservedCommonDataList;
	}

	/**
	 * データ取得メソッドのリソース別独自処理を行う抽象メソッド.<br>
	 * 与えられた識別子が示すリソースアイテムを返します.
	 *
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return リソースアイテム(識別子をKeyとするMap)
	 */
	protected abstract Map<String, I> doGet(String... ids);

	/**
	 * クエリによってリソースアイテムを取得する抽象メソッド.<br>
	 * 与えられた識別子が示すアイテムの中で、データ項目が指定された条件に合致するものを返します.
	 *
	 * @param conditions 条件Map(データ項目名,データ項目の条件)
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return 条件に合致するリソースアイテム(共通データをkeyとするMap)
	 */
	protected abstract Map<String, I> doGetByQuery(Map<String, String[]> conditions, String... ids);

	/**
	 * createメソッドのリソース別独自処理. <br>
	 * 追加されたアイテムの識別子を返します.
	 *
	 * @param newItem 生成内容を含むリソースアイテム
	 * @return リソースアイテムの識別子
	 * @throws DuplicateIdException 追加しようとしたアイテムの識別子が重複する場合
	 */
	protected abstract String doCreate(I newItem) throws DuplicateIdException;

	/**
	 * updateEメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * アイテムの内容を更新し、そのアイテムの識別子を返します.
	 *
	 * @param item 更新内容を含むリソースアイテム
	 * @return リソースアイテムの識別子
	 */
	protected abstract String doUpdate(I item);

	/**
	 * deleteメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * 識別子が示すアイテムを論理削除状態にします.<br>
	 * 削除後のアイテムを表す識別子だけのオブジェクトを返します.
	 *
	 * @param id リソースアイテムの識別子
	 * @return 削除されたアイテムを表すリソースアイテム
	 */
	protected abstract I doDelete(String id);

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
	 * このリソースへアクセスする際に要求されるロック方式を返します.<br>
	 *
	 * @return リソースロック状態タイプ
	 */
	@Override
	public ResourceLockStatusType requiredLockStatus() {

		return this.getClass().getAnnotation(SyncResourceService.class).requiredLockStatus();
	}

	/**
	 * リソースアイテム共通データのバージョン比較により、リソースアイテムの更新競合が発生しているときtrueを返します.<br>
	 *
	 * @param client update(/delete)対象リソースアイテムの共通データ
	 * @param server サーバで保持している現在の共通データ
	 * @return 競合が発生している場合true.
	 */
	private boolean isConflicted(ResourceItemCommonData client, ResourceItemCommonData server,
			UploadCommonData uploadCommon) {

		if (server.getLastModified() <= client.getLastModified()) {
			return false;
		}

		// 同一リクエスト内で同一リソースアイテムを更新する場合は競合でない
		// 上記条件はサーバ側バージョン(最終更新時刻)と今回のリクエスト時刻が等しいかどうかで判断する
		return server.getLastModified() != uploadCommon.getSyncTime();
	}

	/**
	 * 全てのリソースに共通の設定情報を適用します.<br>
	 * リソース実装クラス(あるいはその抽象スーパークラス)では、この設定情報を参照することができます. <br>
	 * 通常、アプリケーションから使用することはありません.<br>
	 *
	 * @param resourceConfigurations 適用する設定情報
	 */
	@Override
	public void applyResourceConfigurations(Properties resourceConfigurations) {

		Object propVal_countOfVerification = resourceConfigurations.get("AbstractSyncResource.countOfVerification");

		if (propVal_countOfVerification != null) {
			this.countOfVerification = Integer.valueOf((String) propVal_countOfVerification);
		}
	}

	/**
	 * このリソースが要求するロック状態を設定します.<br>
	 * 通常、アプリケーションから使用することはありません.<br>
	 * TODO 次期バージョンにて実装予定
	 *
	 * @param requiredLockStatus セットする requiredLockStatus
	 */
	@Deprecated
	@Override
	public void setRequiredLockStatus(ResourceLockStatusType requiredLockStatus) {
		this.requiredLockStatus = requiredLockStatus;
	}

	/**
	 * リソースのロック方式を実装したロック戦略実装を設定します.<br>
	 * 通常、アプリケーションから使用することはありません.<br>
	 * TODO 次期バージョンにて実装予定
	 *
	 * @param lockManager セットする lockManager
	 */
	@Deprecated
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
}
