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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.resource.DefaultSyncResourceManager;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.SyncConflictType;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.SyncResourceManager;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.lock.LockCommonData;
import com.htmlhifive.sync.service.lock.LockRequest;
import com.htmlhifive.sync.service.lock.LockResponse;
import com.htmlhifive.sync.service.upload.UploadCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonDataRepository;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * リソースの同期処理を実行するデフォルトサービス実装.<br>
 * このオブジェクトのパブリックメソッドがトランザクション境界になります.
 *
 * @author kishigam
 */
@Transactional
@Service
public class DefaultSynchronizer implements Synchronizer {

	/**
	 * クライアントに返す今回の同期時刻を、実際の同期実行時刻の何ミリ秒前とするかを設定します.<br>
	 * (デフォルト：0)
	 */
	private long bufferTimeForDownload = 0L;

	/**
	 * 上り更新、および下り更新の同期モード.<br>
	 * (デフォルト：PREPAREなし)
	 */
	private SyncModeType syncMode = SyncModeType.NOT_PREPARE;

	/**
	 * DUPLICATE_ID競合発生時の処理継続可否.<br>
	 * (デフォルト：継続しない)
	 */
	private boolean continueOnConflictOfDuplicateId = false;

	/**
	 * DUPLICATE_ID競合発生時の処理継続可否.<br>
	 * (デフォルト：継続しない)
	 */
	private boolean continueOnConflictOfUpdated = false;

	/**
	 * リソースへのインターフェースを取得するためのマネージャ.
	 */
	@Resource(type = DefaultSyncResourceManager.class)
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

		ResultMapBuilder resultBuilder = new ResultMapBuilder();

		// PREPAREモードの場合、リソースアイテムをreserve
		if (this.syncMode == SyncModeType.PREPARE) {

			// TODO 次期バージョンにて実装予定
			//
			// prepare(sort,reserve)
			// get

		} else {

			// クエリごとに処理し、結果をコンテナに格納
			for (String resourceName : request.getQueries().keySet()) {

				for (ResourceQueryConditions queryConditions : request.getQueries().get(resourceName)) {

					// リソースアイテムを取得、リソース名ごとに結果をマージ(同一アイテムは1つのみ含む)
					SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
					resultBuilder.addAll(resourceName, resource.getByQuery(requestCommon, queryConditions));
				}
			}
		}

		// レスポンス用共通データ
		DownloadCommonData responseCommon = new DownloadCommonData(requestCommon.getStorageId());

		// リクエストの処理時刻でレスポンスの最終下り更新時刻を更新(同タイミングで上り更新されたデータを読み出すためのバッファを考慮)
		responseCommon.setLastDownloadTime(requestCommon.getSyncTime() - bufferTimeForDownload);

		// 下り更新レスポンスに結果を設定
		DownloadResponse response = new DownloadResponse(responseCommon);
		response.setResourceItems(resultBuilder.build());

		return response;
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
			return new UploadResponse(responseCommon);
		}

		// PREPAREモードの場合
		if (this.syncMode == SyncModeType.PREPARE) {

			// TODO 次期バージョンにて実装予定
			// prepare(sort,reserve)
		}

		// 競合の種類ごとに、競合しているリソースアイテムを収集
		Map<SyncConflictType, ResultMapBuilder> resultBuilderMap = createBuilderMap();

		// リソースアイテムごとに処理(上り更新リクエストデータでは、ResourceItemWrapperにリソース名を持つ)
		for (ResourceItemWrapper<? extends Map<String, Object>> uploadingItemWrapper : request.getResourceItems()) {

			String resourceName = uploadingItemWrapper.getItemCommonData().getId().getResourceName();

			// 上り更新を実行
			ResourceItemWrapper<?> itemWrapperAfterUpload = doUpload(resourceManager.locateSyncResource(resourceName),
					requestCommon, uploadingItemWrapper);

			SyncConflictType conflictType = itemWrapperAfterUpload.getItemCommonData().getConflictType();

			// 結果をresultBuilderに加える
			resultBuilderMap.get(conflictType).add(resourceName, itemWrapperAfterUpload);

			// 処理継続を判断、継続しない場合ConflictExceptionをスロー
			if (!continueOnConflict(conflictType)) {
				throwConflictException(conflictType, resultBuilderMap.get(conflictType).build());
			}

			// 継続の場合ステータスに設定
			responseCommon.setConflictType(conflictType);
		}

		// 以下の優先順で競合アイテムリストをConflictExceptionに含めスロー
		if (responseCommon.getConflictType() == SyncConflictType.DUPLICATE_ID) {
			throwConflictException(SyncConflictType.DUPLICATE_ID, resultBuilderMap.get(SyncConflictType.DUPLICATE_ID)
					.build());
		}
		if (responseCommon.getConflictType() == SyncConflictType.UPDATED) {
			throwConflictException(SyncConflictType.UPDATED, resultBuilderMap.get(SyncConflictType.UPDATED).build());
		}

		// リクエストの処理時刻でレスポンスの最終下り更新時刻を更新し、保存
		responseCommon.setLastUploadTime(requestCommon.getSyncTime());
		repository.save(responseCommon);

		// レスポンスの生成とリターン(アイテムリストはセットしない)
		return new UploadResponse(responseCommon);
	}

	/**
	 * ロックの取得を実行します.<br>
	 * リソースごとに、指定されたクエリでリソースアイテムを検索、PREPAREモードで各リソースアイテムのロックを行います.<br>
	 * 全ての対象リソースアイテムがロックできた場合のみ、各リソースアイテムを上り更新するためのロックトークンを返します.<br>
	 * 1件でもロックに失敗した場合、LockExceptionがスローされ、他の全てのリソースアイテムもロックされません.<br>
	 * ロックを取得していない場合、ロックの取得をサポートしないロック方式の場合は空の結果が返ります.
	 *
	 * @param request ロックリクエストデータ
	 * @return ロック取得レスポンスデータ
	 */
	@Override
	public LockResponse getLock(LockRequest request) {

		// 今回のロック取得実行時刻を共通データへセット
		LockCommonData requestCommon = request.getLockCommonData();
		requestCommon.setSyncTime(generateSyncTime());

		// ロックトークンを発行し、共通データへセット
		requestCommon.setLockToken(generateLockToken(request));

		ResultMapBuilder resultBuilder = new ResultMapBuilder();
		// クエリごとに処理し、結果をコンテナに格納
		for (String resourceName : request.getQueries().keySet()) {

			for (ResourceQueryConditions queryConditions : request.getQueries().get(resourceName)) {

				// ロック取得結果を取得、リソース名ごとに結果をマージ(同一アイテムは1つのみ含む)
				SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
				resultBuilder.addAll(resourceName, resource.getByQuery(requestCommon, queryConditions));
			}
		}

		// TODO 次期バージョンにて実装予定
		// prepare(sort,reserve)
		// lock

		// レスポンス用共通データ
		LockCommonData responseCommon = new LockCommonData();
		responseCommon.setStorageId(requestCommon.getStorageId());
		responseCommon.setLockToken(requestCommon.getLockToken());

		// ロック取得レスポンスに結果を設定
		LockResponse response = new LockResponse(responseCommon);
		response.setResourceItems(resultBuilder.build());

		return response;
	}

	/**
	 * ロックの開放を実行します.<br>
	 * ロックが解放できない場合、LockExceptionがスローされます. この処理はリソースが悲観的ロック方式を採用している場合に実行できます.<br>
	 * その他のロック方式の場合は何も起こりません.
	 *
	 * @param request ロックリクエストデータ
	 */
	@Override
	public void releaseLock(LockRequest request) {

		// TODO 次期バージョンにて実装予定
		// lockedItemInfo
		// prepare(sort,reserve)
		// releaseLock
	}

	/**
	 * リソースのロックを取得する際に使用するロックトークンを発行します.
	 *
	 * @param request ロックリクエストデータ
	 * @return ロックトークン
	 */
	protected String generateLockToken(LockRequest request) {

		// デフォルト実装として、ストレージIDを使用する
		return request.getLockCommonData().getStorageId();
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
			// 最終上り更新時刻は初期値のため、isLaterUploadThan()は必ずfalseになる状態となる
		}
		return lastCommon;
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
			return continueOnConflictOfDuplicateId;

		if (resultType == SyncConflictType.UPDATED)
			return continueOnConflictOfUpdated;

		return true;
	}

	/**
	 * 上り更新レスポンスを競合発生時の例外に設定し、スローします.
	 *
	 * @param conflictType 競合タイプ
	 * @param conflictItemMap 競合したリソースアイテムの情報を保持しているMap
	 */
	private void throwConflictException(SyncConflictType conflictType,
			Map<String, List<ResourceItemWrapper<?>>> conflictItemMap) {

		UploadCommonData conflictCommon = new UploadCommonData();
		conflictCommon.setConflictType(conflictType);

		UploadResponse response = new UploadResponse(conflictCommon);
		response.setResourceItems(conflictItemMap);

		throw new ConflictException(response);
	}

	/**
	 * 各同期サービスの実行時刻を決定します.<br>
	 * 現在時刻を使用します.
	 *
	 * @return 同期実行時刻
	 */
	private long generateSyncTime() {

		return new Date().getTime();
	}

	/**
	 * リソースから収集したリソースアイテムを、リソース名、リソースごとのアイテムリストのMapに格納するビルダークラス.<br>
	 * アイテムリストでは、同一アイテムは1つだけ含むように集約されます.
	 */
	private class ResultMapBuilder {

		/**
		 * ビルダー内部用のMapオブジェクト.<br>
		 * 同一アイテムを1つしか含まないようにアイテムをSetで保持します.
		 */
		Map<String, Set<ResourceItemWrapper<?>>> setMap = new HashMap<>();

		/**
		 * ビルダー内部で保持しているリソース名ごとのアイテムセットを返します.<br>
		 * 保持されていない場合、セットインスタンスを生成してMapに格納します.
		 *
		 * @param resourceName リソース名
		 * @return リソース名ごとのアイテムセット
		 */
		Set<ResourceItemWrapper<?>> getItemSet(String resourceName) {

			if (!setMap.containsKey(resourceName)) {
				setMap.put(resourceName, new HashSet<ResourceItemWrapper<?>>());
			}

			return setMap.get(resourceName);
		}

		/**
		 * リソースアイテムをリソース名ごとのアイテムセットに加えます.<br>
		 *
		 * @param resourceName
		 * @param itemWrapper
		 */
		void add(String resourceName, ResourceItemWrapper<?> itemWrapper) {

			getItemSet(resourceName).add(itemWrapper);
		}

		/**
		 * リソースアイテムリストの持つアイテム全てををリソース名ごとのアイテムセットに加えます.<br>
		 *
		 * @param resourceName
		 * @param itemWrapperList
		 */
		void addAll(String resourceName, List<? extends ResourceItemWrapper<?>> itemWrapperList) {

			getItemSet(resourceName).addAll(itemWrapperList);
		}

		/**
		 * 内部用のアイテムSetを持つMapを、Listを持つMapに変換して返します.
		 *
		 * @return アイテムリストを持つMap
		 */
		Map<String, List<ResourceItemWrapper<?>>> build() {

			Map<String, List<ResourceItemWrapper<?>>> resultMap = new HashMap<>();

			for (String resourceName : setMap.keySet()) {
				resultMap.put(resourceName, new ArrayList<>(setMap.get(resourceName)));
			}

			return resultMap;
		}
	}

	/**
	 * {@link SyncConflictType}ごとの{@link ResultMapBuilder}を格納したMapを返します.
	 *
	 * @return ResultMapBuilderのMap
	 */
	private Map<SyncConflictType, ResultMapBuilder> createBuilderMap() {

		Map<SyncConflictType, ResultMapBuilder> map = new HashMap<>();
		for (SyncConflictType conflictType : SyncConflictType.values()) {
			map.put(conflictType, new ResultMapBuilder());
		}

		return map;
	}

	/**
	 * 設定用setterメソッド.<br>
	 * アプリケーションから使用することはありません.
	 *
	 * @param bufferTimeForDownload セットする bufferTimeForDownload
	 */
	public void setBufferTimeForDownload(long bufferTimeForDownload) {
		this.bufferTimeForDownload = bufferTimeForDownload;
	}

	/**
	 * 設定用setterメソッド.<br>
	 * アプリケーションから使用することはありません.
	 *
	 * @param syncMode セットする syncMode
	 */
	public void setSyncMode(SyncModeType syncMode) {
		this.syncMode = syncMode;
	}

	/**
	 * 設定用setterメソッド.<br>
	 * アプリケーションから使用することはありません.
	 *
	 * @param continueOnConflictOfDuplicateId セットする continueOnConflictOfDuplicateId
	 */
	public void setContinueOnConflictOfDuplicateId(boolean continueOnConflictOfDuplicateId) {
		this.continueOnConflictOfDuplicateId = continueOnConflictOfDuplicateId;
	}

	/**
	 * 設定用setterメソッド.<br>
	 * アプリケーションから使用することはありません.
	 *
	 * @param continueOnConflictOfUpdated セットする continueOnConflictOfUpdated
	 */
	public void setContinueOnConflictOfUpdated(boolean continueOnConflictOfUpdated) {
		this.continueOnConflictOfUpdated = continueOnConflictOfUpdated;
	}
}