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
package com.htmlhifive.sync.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.SyncConflictType;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.SyncResourceManager;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.download.DownloadControlType;
import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.upload.UploadCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonDataRepository;
import com.htmlhifive.sync.service.upload.UploadControlType;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * リソースの同期処理を実行するデフォルトサービス実装.<br>
 * このオブジェクトのパブリックメソッドがトランザクション境界になります.
 *
 * @author kishigam
 */
@Service
public class DefaultSynchronizer implements Synchronizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSynchronizer.class);

	/**
	 * 同期制御の設定情報.
	 */
	@Resource
	private SyncConfiguration syncConfiguration;

	/**
	 * リソースへのインターフェースを取得するためのマネージャ.
	 */
	@Resource
	private SyncResourceManager resourceManager;

	/**
	 * 同期ステータスのリポジトリ.
	 */
	@Resource
	private UploadCommonDataRepository repository;

	/**
	 * 下り更新を実行します.<br>
	 * リソースごとに、指定されたクエリでリソースアイテムを検索、取得します.
	 *
	 * @param request 下り更新リクエストデータ
	 * @return 下り更新レスポンスデータ
	 */
	@Override
	public DownloadResponse download(DownloadRequest request) {

		// 今回の同期実行時刻を共通データへセット
		DownloadCommonData requestCommon = request.getDownloadCommonData();
		requestCommon.setSyncTime(generateSyncTime());

		// リソースに対してクエリを実行し、リソースアイテムを取得
		Map<String, List<ResourceItemWrapper<?>>> downloadingItems = getItems(request.getQueries(), requestCommon);

		// READ_LOCKの場合、取得したアイテムそれぞれに対してアクセス権の確保と再取得を行う
		if (this.syncConfiguration.downloadControl() == DownloadControlType.READ_LOCK) {

			Map<String, List<ResourceItemCommonData>> reservedCommonDataMap = getItemsReadLock(downloadingItems);

			// 再取得した結果で上書き
			downloadingItems = getReservedItems(requestCommon, reservedCommonDataMap);
		}

		// レスポンス用共通データ
		DownloadCommonData responseCommon = new DownloadCommonData(requestCommon.getStorageId());

		// レスポンスの最終下り更新時刻を更新
		responseCommon.setLastDownloadTime(calcDownloadTime(requestCommon));

		// 下り更新レスポンスに結果を設定
		DownloadResponse response = new DownloadResponse(responseCommon);
		response.setResourceItems(downloadingItems);

		return response;
	}

	/**
	 * 指定されたクエリを実行し、リソースからリソースアイテムを取得します.<br>
	 * リソースごとのアイテムリストをMapで返します.
	 *
	 * @param queries リソースクエリオブジェクト
	 * @param syncCommon 同期共通データ
	 * @return リソース名とリソースアイテム(ラッパーオブジェクト)リストのMap
	 */
	private Map<String, List<ResourceItemWrapper<?>>> getItems(Map<String, List<ResourceQueryConditions>> queries,
			SyncCommonData syncCommon) {

		ResourceItemsMapBuilder<ResourceItemWrapper<?>> resultBuilder = new ResourceItemsMapBuilder<>();

		// リソースごと、クエリごとに処理し、結果をコンテナに格納
		for (String resourceName : queries.keySet()) {
			for (ResourceQueryConditions queryConditions : queries.get(resourceName)) {

				// リソースアイテムを取得、リソース名ごとに結果をマージ(同一アイテムは1つのみ含む)
				SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
				resultBuilder.addAll(resourceName, resource.getByQuery(syncCommon, queryConditions));
			}
		}
		return resultBuilder.build();
	}

	/**
	 * 予約されたリソースアイテムを取得します.
	 *
	 * @param syncCommon 同期共通データ
	 * @param reservedCommonDataMap 予約済みアイテムの共通データリストのリソース別Map
	 * @return リソース名とリソースアイテム(ラッパーオブジェクト)リストのMap
	 */
	private Map<String, List<ResourceItemWrapper<?>>> getReservedItems(SyncCommonData syncCommon,
			Map<String, List<ResourceItemCommonData>> reservedCommonDataMap) {

		ResourceItemsMapBuilder<ResourceItemWrapper<?>> resultBuilder = new ResourceItemsMapBuilder<>();

		// リソースごとに再取得
		for (String resourceName : reservedCommonDataMap.keySet()) {

			SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
			List<? extends ResourceItemWrapper<?>> gotItemWrappers = resource.get(syncCommon,
					reservedCommonDataMap.get(resourceName));

			resultBuilder.addAll(resourceName, gotItemWrappers);
		}
		return resultBuilder.build();
	}

	/**
	 * 下り更新時刻を算出します.<br>
	 * 他のクライアントによって同時に実行された上り更新結果を次回の下り更新で取得できるように、設定された時間だけ前にずらした時刻を求めます.
	 *
	 * @param downloadCommon 下り更新共通データ
	 * @return 算出された下り更新時刻
	 */
	private long calcDownloadTime(DownloadCommonData downloadCommon) {

		long buffer = new Integer(syncConfiguration.bufferTimeForDownload()).longValue() * 1000L;

		long result = downloadCommon.getSyncTime() - buffer;

		return result < 0 ? 0 : result;
	}

	/**
	 * リソースアイテムの読み取りロックを行い、アイテムを占有操作できるようにします.<br>
	 * {@link ResourceItemWrapper}のリストを{@link ResourceItemCommonData}のリストに変換してから処理します.
	 *
	 * @param reservingItemsMap 対象リソースアイテム(ラッパーオブジェクト)のリスト(リソース別Map)
	 * @return 予約済みリソースアイテムの共通データリスト(リソース別Map)
	 */
	private Map<String, List<ResourceItemCommonData>> getItemsReadLock(
			Map<String, List<ResourceItemWrapper<?>>> reservingItemsMap) {

		// リソースアイテム共通データリストのMapに変換
		Map<String, List<ResourceItemCommonData>> commonListMap = new HashMap<>();
		for (String resourceName : reservingItemsMap.keySet()) {

			List<ResourceItemCommonData> commonList = new ArrayList<>();
			for (ResourceItemWrapper<?> wrapper : reservingItemsMap.get(resourceName)) {
				commonList.add(wrapper.getItemCommonData());
			}
			commonListMap.put(resourceName, commonList);
		}

		return getItemsForUpdate(commonListMap);
	}

	/**
	 * 上り更新を実行します.<br>
	 * 対象のリソースを判断し、リソースアイテムの更新内容に応じた更新処理を呼び出します.
	 *
	 * @param request 上り更新リクエストデータ
	 * @return 上り更新レスポンスデータ
	 */
	@Override
	public UploadResponse upload(UploadRequest request) {

		// リクエストの上り更新共通データに今回の上り更新処理時刻を設定
		UploadCommonData requestCommon = request.getUploadCommonData();
		requestCommon.setSyncTime(generateSyncTime());

		// 保存していた、前回までの上り更新共通データを取得し、レスポンスの共通データとして初期設定
		UploadCommonData responseCommon = lastUploadCommonData(requestCommon.getStorageId());

		// 二重送信判定
		// サーバ側で管理されている前回上り更新時刻より前の場合、二重送信等で処理済みと判断し、そのまま返す.
		if (responseCommon.isLaterUploadThan(requestCommon)) {

			LOGGER.info(new StringBuilder().append("info : duplicate uploading detected. storageId : ")
					.append(responseCommon.getStorageId()).append(", lastUpdateTime : ")
					.append(responseCommon.getLastUploadTime()).toString());

			return new UploadResponse(responseCommon);
		}

		// RESERVEタイプの場合は先に対象アイテムを予約
		if (this.syncConfiguration.uploadControl() == UploadControlType.RESERVE) {

			reserveUploadingWrappedItems(request.getResourceItems());
		}

		// AVOID_DEADLOCKタイプの場合は更新順をソートした結果に変更する
		List<? extends ResourceItemWrapper<? extends Map<String, Object>>> itemsToUpload = request.getResourceItems();

		if (this.syncConfiguration.uploadControl() == UploadControlType.AVOID_DEADLOCK) {
			itemsToUpload = new ArrayList<>(request.getResourceItems());
			Collections.sort(itemsToUpload);
		}

		// 競合の種類ごとに、競合しているリソースアイテムを収集
		Map<SyncConflictType, ResourceItemsMapBuilder<ResourceItemWrapper<?>>> resultBuilderMap = createBuilderMap();

		responseCommon.setConflictType(SyncConflictType.NONE);

		// リソースアイテムごとに処理(上り更新リクエストデータでは、ResourceItemWrapperにリソース名を持つ)
		for (Iterator<? extends ResourceItemWrapper<? extends Map<String, Object>>> itr = itemsToUpload.iterator(); itr
				.hasNext();) {

			ResourceItemWrapper<? extends Map<String, Object>> uploadingItemWrapper = itr.next();

			String resourceName = uploadingItemWrapper.getItemCommonData().getId().getResourceName();

			// 上り更新を実行
			ResourceItemWrapper<?> itemWrapperAfterUpload = doUpload(resourceManager.locateSyncResource(resourceName),
					requestCommon, uploadingItemWrapper);

			SyncConflictType conflictType = itemWrapperAfterUpload.getItemCommonData().getConflictType();

			// 結果をresultBuilderに加える
			resultBuilderMap.get(conflictType).add(resourceName, itemWrapperAfterUpload);

			// リクエスト全体の競合ステータスを設定
			if (isConflictTypeChanging(responseCommon.getConflictType(), conflictType)) {
				responseCommon.setConflictType(conflictType);
			}

			if (!continueOnConflict(responseCommon.getConflictType()))
				break;
		}

		// 以下の優先順で競合アイテムリストをConflictExceptionに含めスロー
		if (responseCommon.getConflictType() == SyncConflictType.DUPLICATE_ID) {
			throwConflictException(SyncConflictType.DUPLICATE_ID, resultBuilderMap);
		}
		if (responseCommon.getConflictType() == SyncConflictType.UPDATED) {
			throwConflictException(SyncConflictType.UPDATED, resultBuilderMap);
		}

		// リクエストの処理時刻でレスポンスの最終下り更新時刻を更新し、保存
		responseCommon.setLastUploadTime(requestCommon.getSyncTime());
		repository.save(responseCommon);

		// レスポンスの生成とリターン(アイテムリストはセットしない)
		return new UploadResponse(responseCommon);
	}

	/**
	 * リポジトリを検索し、前回の上り更新共通データを取得します.<br>
	 * 存在しない場合は新規生成します.
	 *
	 * @param storageId クライアントのストレージID
	 * @return 同期ステータスオブジェクト
	 */
	private UploadCommonData lastUploadCommonData(String storageId) {

		UploadCommonData lastCommon = repository.findOne(storageId);
		if (lastCommon == null) {
			lastCommon = new UploadCommonData();
			lastCommon.setStorageId(storageId);
		}
		return lastCommon;
	}

	/**
	 * リソースアイテムを「予約」し、アイテムを占有操作できるようにします.<br>
	 * {@link ResourceItemWrapper}のリストを{@link ResourceItemCommonData}のリストに変換してから処理します.<br>
	 * リソース名はリソースアイテムラッパーオブジェクトに含まれるものを使用します.
	 *
	 * @param reservingItems 対象リソースアイテム(ラッパーオブジェクト)のリスト
	 * @return 予約済みリソースアイテムの共通データリスト(リソース別Map)
	 */
	private Map<String, List<ResourceItemCommonData>> reserveUploadingWrappedItems(
			List<? extends ResourceItemWrapper<?>> reservingItems) {

		// リソースアイテム共通データリストのMapに変換
		ResourceItemsMapBuilder<ResourceItemCommonData> mapBuilder = new ResourceItemsMapBuilder<>();
		for (ResourceItemWrapper<?> itemWrapper : reservingItems) {

			String resourceName = itemWrapper.getItemCommonData().getId().getResourceName();

			// UPDATE, DELETEの場合のみ対象とする
			if (itemWrapper.getItemCommonData().getAction() == SyncAction.UPDATE
					|| itemWrapper.getItemCommonData().getAction() == SyncAction.DELETE) {
				mapBuilder.add(resourceName, itemWrapper.getItemCommonData());
			}
		}

		return getItemsForUpdate(mapBuilder.build());
	}

	/**
	 * リソースに対してアクションに応じたリクエストメソッドを呼び出すヘルパー.<br>
	 * エレメント型を固定することで、JSONからの型変換を行ったエレメントをリソースに渡すことができます.
	 *
	 * @param resource リソース
	 * @param requestCommon 上り更新共通データ
	 * @param itemWrapper 更新するリソースアイテム
	 * @return 更新後のリソースアイテム
	 */
	private <T> ResourceItemWrapper<T> doUpload(SyncResource<T> resource, UploadCommonData requestCommon,
			ResourceItemWrapper<? extends Map<String, Object>> itemWrapper) throws ConflictException {

		ResourceItemCommonData itemCommon = itemWrapper.getItemCommonData();
		T item;
		switch (itemWrapper.getItemCommonData().getAction()) {
			case CREATE:
				item = resource.itemConverter().convertToItem(itemWrapper.getItem(), resource.itemType());
				return resource.create(requestCommon, itemCommon, item);

			case UPDATE:
				item = resource.itemConverter().convertToItem(itemWrapper.getItem(), resource.itemType());
				return resource.update(requestCommon, itemCommon, item);

			case DELETE:
				return resource.delete(requestCommon, itemCommon);

			default:
				throw new BadRequestException("undefined action has called.");
		}

	}

	/**
	 * 競合タイプに応じて、次のリソースアイテムの更新を継続するかどうかを判断し、返します.
	 *
	 * @param resultType 上り更新結果の競合タイプ
	 * @return 継続する場合true
	 */
	private boolean continueOnConflict(SyncConflictType resultType) {

		if (resultType == SyncConflictType.DUPLICATE_ID)
			return this.syncConfiguration.isContinueOnConflictOfDuplicateId();

		if (resultType == SyncConflictType.UPDATED)
			return this.syncConfiguration.isContinueOnConflictOfUpdated();

		return true;
	}

	/**
	 * 現在の競合タイプが、今回のリソースアイテム更新における競合タイプで更新されるときtrueを返します.
	 *
	 * @param currentConflictType 現在の競合タイプ
	 * @param conflictType 今回のリソースアイテム更新における競合タイプ
	 * @return 今回のリソースアイテム更新における競合タイプで現在のタイプが更新されるときtrue
	 */
	private boolean isConflictTypeChanging(SyncConflictType currentConflictType, SyncConflictType conflictType) {
		return currentConflictType == SyncConflictType.NONE || conflictType == SyncConflictType.DUPLICATE_ID;
	}

	/**
	 * 上り更新レスポンスを競合発生時の例外に設定し、スローします.
	 *
	 * @param conflictType 競合タイプ
	 * @param resultBuilderMap 競合したリソースアイテムの情報を保持しているMap
	 */
	private void throwConflictException(SyncConflictType conflictType,
			Map<SyncConflictType, ResourceItemsMapBuilder<ResourceItemWrapper<?>>> resultBuilderMap) {

		UploadCommonData conflictCommon = new UploadCommonData();
		conflictCommon.setConflictType(conflictType);

		UploadResponse response = new UploadResponse(conflictCommon);
		response.setResourceItems(resultBuilderMap.get(conflictType).build());

		throw new ConflictException(response);
	}

	/**
	 * リソースアイテムに対して"for update"操作を行い、そのリソースアイテム共通データを返します.<br>
	 * アイテムの内容は取得されません.
	 *
	 * @param reservingItemsMap 対象リソースアイテム共通データのリスト(リソース別Map)
	 * @return 予約済みリソースアイテムの共通データリスト(リソース別Map)
	 */
	private Map<String, List<ResourceItemCommonData>> getItemsForUpdate(
			Map<String, List<ResourceItemCommonData>> reservingItemsMap) {

		// 結果を保持するMap
		Map<String, List<ResourceItemCommonData>> reservedCommonDataMap = new HashMap<>();

		// 対象をリソース名でソート
		TreeMap<String, List<ResourceItemCommonData>> sortedItemsMap = new TreeMap<>(reservingItemsMap);

		for (String resourceName : sortedItemsMap.keySet()) {

			SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
			reservedCommonDataMap.put(resourceName, resource.forUpdate(sortedItemsMap.get(resourceName)));
		}

		return reservedCommonDataMap;
	}

	/**
	 * 各同期サービスの実行時刻を決定します.<br>
	 * 現在時刻を使用します.
	 *
	 * @return 同期実行時刻
	 */
	private long generateSyncTime() {

		return syncConfiguration.generateSyncTime();
	}

	/**
	 * リソースから収集したリソースアイテムを、リソース名、リソースごとのアイテムリストのMapに格納するビルダークラス.<br>
	 * アイテムリストでは、同一アイテムは1つだけ含むように集約されます.
	 *
	 * @param <T> リソースアイテムを識別するオブジェクト(共通データまたはラッパーオブジェクト)
	 */
	private class ResourceItemsMapBuilder<T> {

		/**
		 * ビルダー内部用のMapオブジェクト.<br>
		 * 同一アイテムを1つしか含まないようにアイテムをSetで保持します.
		 */
		Map<String, Set<T>> setMap = new HashMap<>();

		/**
		 * ビルダー内部で保持しているリソース名ごとのアイテムセットを返します.<br>
		 * 保持されていない場合、セットインスタンスを生成してMapに格納します.
		 *
		 * @param resourceName リソース名
		 * @return リソース名ごとのアイテムセット
		 */
		Set<T> getItemSet(String resourceName) {

			if (!setMap.containsKey(resourceName)) {
				setMap.put(resourceName, new HashSet<T>());
			}

			return setMap.get(resourceName);
		}

		/**
		 * リソースアイテム(ラッパーオブジェクト)をリソース名ごとのアイテムセットに加えます.<br>
		 *
		 * @param resourceName リソース名
		 * @param item アイテム(共通データまたはラッパーオブジェクト)
		 */
		void add(String resourceName, T item) {

			getItemSet(resourceName).add(item);
		}

		/**
		 * リソースアイテム(ラッパーオブジェクト)リストの持つアイテム全てををリソース名ごとのアイテムセットに加えます.<br>
		 *
		 * @param resourceName リソース名
		 * @param itemList アイテム(共通データまたはラッパーオブジェクト)のリスト
		 */
		void addAll(String resourceName, List<? extends T> itemList) {

			getItemSet(resourceName).addAll(itemList);
		}

		/**
		 * リソース名ごとのリソースアイテム(ラッパーオブジェクト)のリストを持つMapを構築して返します.
		 *
		 * @return itemList アイテム(共通データまたはラッパーオブジェクト)のリストを持つMap
		 */
		Map<String, List<T>> build() {

			Map<String, List<T>> resultMap = new HashMap<>();

			for (String resourceName : setMap.keySet()) {
				resultMap.put(resourceName, new ArrayList<>(setMap.get(resourceName)));
			}

			return resultMap;
		}
	}

	/**
	 * {@link SyncConflictType}ごとの{@link ResourceItemsMapBuilder}を格納したMapを返します.
	 *
	 * @return ResultMapBuilderのMap
	 */
	private Map<SyncConflictType, ResourceItemsMapBuilder<ResourceItemWrapper<?>>> createBuilderMap() {

		Map<SyncConflictType, ResourceItemsMapBuilder<ResourceItemWrapper<?>>> map = new HashMap<>();
		for (SyncConflictType conflictType : SyncConflictType.values()) {
			map.put(conflictType, new ResourceItemsMapBuilder<ResourceItemWrapper<?>>());
		}

		return map;
	}
}