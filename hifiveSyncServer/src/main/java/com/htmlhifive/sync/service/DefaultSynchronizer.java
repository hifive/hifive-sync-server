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
import java.util.List;
import java.util.Map;

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
	 * (現在の設定は60,000ミリ秒＝1分)
	 */
	private static final long BUFFER_TIME_FOR_DOWNLOAD = 1L * 60L * 1_000L;

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

		// リソース名ごとに結果をコンテナに格納する
		Map<String, List<ResourceItemWrapper<?>>> resultMap = new HashMap<>();

		// クエリごとに処理し、結果をコンテナに格納
		for (String resourceName : request.getQueries().keySet()) {

			// Mapにリソース名ごとのコンテナが存在しない場合は生成
			if (!resultMap.containsKey(resourceName)) {
				resultMap.put(resourceName, new ArrayList<ResourceItemWrapper<?>>());
			}

			List<ResourceItemWrapper<?>> resultList = resultMap.get(resourceName);
			for (ResourceQueryConditions queryConditions : request.getQueries().get(resourceName)) {

				// リソースへリクエスト発行
				SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
				List<? extends ResourceItemWrapper<?>> resultItemList = resource.executeQuery(queryConditions);

				// 結果をマージする(同じリソースアイテムは1件のみとなる)
				for (ResourceItemWrapper<?> itemWrapper : resultItemList) {
					if (resultList.contains(itemWrapper)) {
						resultList.add(itemWrapper);
					}
				}
			}
			resultMap.put(resourceName, resultList);
		}

		// レスポンス用共通データ
		DownloadCommonData responseCommon = new DownloadCommonData();
		responseCommon.setStorageId(request.getDownloadCommonData().getStorageId());

		// 下り更新時刻を算出(バッファ考慮)し、レスポンスに設定
		responseCommon.setLastDownloadTime(generateDownloadTime());

		// 下り更新レスポンスに結果を設定
		DownloadResponse response = new DownloadResponse(responseCommon);
		response.setResourceItems(resultMap);

		// 下り更新では同期ステータスを更新しない
		return response;
	}

	/**
	 * 下り更新時刻を決定します.<br>
	 * 同時に行われている他のクライアントによる上り更新データを確実に含めるため、バッファ時間を考慮します.
	 *
	 * @return
	 */
	private long generateDownloadTime() {

		return new Date().getTime() - BUFFER_TIME_FOR_DOWNLOAD;
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

		// リクエストの上り更新共通データ
		UploadCommonData requestCommon = request.getUploadCommonData();

		// 保存していた、前回までの上り更新共通データを取得し、レスポンスの共通データとして初期設定
		UploadCommonData responseCommon = lastUploadCommonData(requestCommon.getStorageId());

		// 二重送信判定
		// サーバ側で管理されている前回上り更新時刻より前の場合、二重送信等で処理済みと判断し、をそのまま返します.
		if (responseCommon.isLaterUploadThan(requestCommon)) {
			return new UploadResponse(responseCommon);
		}

		// 競合の種類ごとに、競合データリソースのアイテムリスト(リソース別Map)に格納する
		Map<SyncConflictType, Map<String, List<ResourceItemWrapper<?>>>> resultMapHolder = new HashMap<>();
		resultMapHolder.put(SyncConflictType.DUPLICATEDID, new HashMap<String, List<ResourceItemWrapper<?>>>());
		resultMapHolder.put(SyncConflictType.UPDATED, new HashMap<String, List<ResourceItemWrapper<?>>>());

		// リソースアイテムごとに処理(上り更新リクエストデータのみ、ResourceItemWrapperにリソース名を持つ)
		for (ResourceItemWrapper<? extends Map<String, Object>> uploadingItemWrapper : request.getResourceItems()) {

			String resourceName = uploadingItemWrapper.getItemCommonData().getId().getResourceName();

			SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);

			ResourceItemWrapper<?> itemWrapperAfterUpload = doUpload(resource, requestCommon, uploadingItemWrapper);

			SyncConflictType conflictType = itemWrapperAfterUpload.getItemCommonData().getConflictType();

			// 競合でない場合は次のリソースアイテムの更新へ(更新後のアイテムは使用しない)
			if (conflictType == null) {
				continue;
			}

			Map<String, List<ResourceItemWrapper<?>>> resultMap = resultMapHolder.get(conflictType);

			// Mapにリソース名ごとのコンテナが存在しない場合は生成
			if (!resultMap.containsKey(resourceName)) {

				List<ResourceItemWrapper<?>> itemList = new ArrayList<>();
				resultMap.put(resourceName, itemList);
			}

			// 競合しているアイテムをItemMapにマージ(同じリソースアイテムは1件のみとなる)
			if (!resultMap.get(resourceName).contains(itemWrapperAfterUpload)) {
				resultMap.get(resourceName).add(itemWrapperAfterUpload);
			}

			// 競合時の処理継続判断(競合のタイプごとに)
			if (!continueOnConflict(conflictType)) {

				// 継続しない場合はConflictExceptionをスロー
				throwConflictException(conflictType, resultMap);
			}

			// ステータスに設定し、次のリソースアイテムの更新へ
			responseCommon.setConflictType(conflictType);
		}

		// 以下の優先順で複数の競合データをConflictExceptionでスロー
		if (responseCommon.getConflictType() == SyncConflictType.DUPLICATEDID) {
			throwConflictException(SyncConflictType.DUPLICATEDID, resultMapHolder.get(SyncConflictType.DUPLICATEDID));
		}
		if (responseCommon.getConflictType() == SyncConflictType.UPDATED) {
			throwConflictException(SyncConflictType.UPDATED, resultMapHolder.get(SyncConflictType.UPDATED));
		}

		// 競合がない場合、上り更新時刻をレスポンスの共通データに設定し、更新する
		responseCommon.setLastUploadTime(generateUploadTime());
		repository.save(responseCommon);

		// レスポンスの生成とリターン(アイテムリストはセットしない)
		return new UploadResponse(responseCommon);
	}

	/**
	 * 上り更新時刻を決定します.<br>
	 * 現在時刻を使用します.
	 *
	 * @return 上り更新時刻
	 */
	private long generateUploadTime() {

		return new Date().getTime();
	}

	/**
	 * リソースに対してアクションに応じたリクエストメソッドを呼び出すヘルパー.<br>
	 * エレメント型を固定することで、JSONからの型変換を行ったエレメントをリソースに渡すことができます.
	 *
	 * @param resource リソース
	 * @param requestCommon
	 * @param itemWrapper 更新するリソースアイテム
	 * @return 更新後のリソースアイテム
	 */
	private <T> ResourceItemWrapper<T> doUpload(SyncResource<T> resource, UploadCommonData requestCommon,
			ResourceItemWrapper<? extends Map<String, Object>> itemWrapper) throws ConflictException {

		ResourceItemCommonData itemCommon = itemWrapper.getItemCommonData();
		T item;
		switch (itemWrapper.getItemCommonData().getAction()) {
			case CREATE:
				item = resource.getResourceItemConverter().convert(itemWrapper.getItem(), resource.getItemType());
				return resource.create(requestCommon, itemCommon, item);

			case UPDATE:
				item = resource.getResourceItemConverter().convert(itemWrapper.getItem(), resource.getItemType());
				return resource.update(requestCommon, itemCommon, item);

			case DELETE:
				return resource.delete(requestCommon, itemCommon);

			default:
				throw new BadRequestException("undefined action has called.");
		}

	}

	/**
	 * 更新結果ごとに、次のリソースアイテムの更新を継続するかどうかを判断し、返します.
	 *
	 * @param resultType 上り更新結果のタイプ
	 * @return 継続する場合true
	 */
	private boolean continueOnConflict(SyncConflictType resultType) {

		// TODO:コンフィグ化
		return resultType != SyncConflictType.DUPLICATEDID && resultType != SyncConflictType.UPDATED;
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
			// 最終上り更新時刻は初期値のため、isLaterUploadThan()は必ずfalseになる状態となっている
		}
		return lastCommon;
	}
}