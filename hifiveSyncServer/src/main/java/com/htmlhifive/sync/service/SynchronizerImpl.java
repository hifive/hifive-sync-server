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

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQuery;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.SyncResourceManager;
import com.htmlhifive.sync.resource.SyncResultType;

/**
 * リソースの同期処理を実行するサービス実装.<br>
 * このオブジェクトのパブリックメソッドがトランザクション境界です.
 *
 * @author kishigam
 */
@Transactional
@Service
public class SynchronizerImpl implements Synchronizer {

	/**
	 * リソースへのインターフェースを取得するためのマネージャ.
	 */
	@Resource
	private SyncResourceManager resourceManager;

	/**
	 * 同期ステータスのリポジトリ.
	 */
	@Resource
	private SyncStatusRepository repository;

	/**
	 * クライアントに返す今回の同期時刻を、実際の同期実行時刻の何ミリ秒前とするかを設定します.<br>
	 * (現在の設定は60,000ミリ秒＝1分)
	 */
	private static final long BUFFER_TIME_FOR_DOWNLOAD = 1L * 60L * 1_000L;

	/**
	 * 下り更新を実行します.<br>
	 * リソースごとに、指定されたクエリでリソースアイテムを検索、取得します.
	 *
	 * @param storageId クライアントのストレージID
	 * @param queries クエリリスト
	 * @return 下り更新結果を含む同期ステータスオブジェクト
	 */
	@Override
	public SyncStatus download(String storageId, List<ResourceQueriesContainer> queries) {

		// クライアントごとの(前回)同期ステータスオブジェクトを取得
		SyncStatus currentStatus = currentStatus(storageId);

		// 下り更新時刻の設定
		currentStatus.setLastDownloadTime(generateDownloadTime());

		// リソース名ごとに結果をコンテナに格納する
		Map<String, ResourceItemsContainer> resultMap = new HashMap<>();

		// クエリごとに処理し、結果をコンテナに格納
		for (ResourceQueriesContainer queryContainer : queries) {

			String resourceName = queryContainer.getResourceName();

			// Mapにリソース名ごとのコンテナが存在しない場合は生成
			if (!resultMap.containsKey(resourceName)) {
				resultMap.put(resourceName, new ResourceItemsContainer(resourceName));
			}

			ResourceItemsContainer resultContainer = resultMap.get(resourceName);
			for (ResourceQuery query : queryContainer.getQueryList()) {

				// synchronizerへリクエスト発行
				SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
				List<ResourceItemWrapper> resultItemList = resource.readByQuery(query);

				// 結果をマージする(同じリソースアイテムは1件のみとなる)
				resultContainer.mergeItem(resultItemList);
			}

			resultMap.put(resourceName, resultContainer);
		}
		currentStatus.setResourceItems(new ArrayList<>(resultMap.values()));

		// 下り更新では同期ステータスを更新しない
		return currentStatus;
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
	 * @param storageId クライアントのストレージID
	 * @param lastUploadTime クライアントごとの前回上り更新時刻
	 * @param resourceItems リソースアイテムリスト
	 * @return 上り更新結果を含む同期ステータスオブジェクト
	 */
	@Override
	public SyncStatus upload(String storageId, long lastUploadTime, List<ResourceItemsContainer> resourceItems) {

		// クライアントごとの(前回)同期ステータスオブジェクトを取得
		SyncStatus currentStatus = currentStatus(storageId);

		// 二重送信判定
		// サーバ側で管理されている前回上り更新時刻より前の場合、二重送信と判断し、現在の同期ステータスをそのまま返します.
		if (currentStatus.isNewerStatusThanClient(lastUploadTime)) {
			return currentStatus;
		}

		// 上り更新時刻の設定
		currentStatus.setLastUploadTime(generateUploadTime());

		// リソース名ごとに結果をコンテナに格納する
		Map<String, ResourceItemsContainer> resultMap = new HashMap<>();

		// リソースごとに処理(1リソースが複数回出現する可能性もある)
		for (ResourceItemsContainer uploadingItemContainer : resourceItems) {

			String resourceName = uploadingItemContainer.getResourceName();

			// アイテムごとに処理
			for (ResourceItemWrapper uploadingItem : uploadingItemContainer.getItemsList()) {

				// 更新時刻を各リソースアイテムに設定
				uploadingItem.setLastModified(currentStatus.getLastUploadTime());

				SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);

				ResourceItemWrapper itemWrapper = doSyncUpload(resource, uploadingItem);

				// 競合でない場合のアイテムは使用しない
				if (itemWrapper.getResultType() == SyncResultType.OK) {
					continue;
				}

				// Mapにリソース名ごとのコンテナが存在しない場合は生成
				if (!resultMap.containsKey(resourceName)) {

					ResourceItemsContainer container = new ResourceItemsContainer(resourceName);
					container.setItemsLIst(new ArrayList<ResourceItemWrapper>());

					resultMap.put(resourceName, container);
				}

				ResourceItemsContainer resultContainer = resultMap.get(resource);

				// 競合しているアイテムをItemMapにマージ(同じリソースアイテムは1件のみとなる)
				resultContainer.mergeItem(itemWrapper);
				resultMap.put(resourceName, resultContainer);

				// 競合タイプを判断し、ConflictExceptionのスローまたは続行
				if (itemWrapper.getResultType() == SyncResultType.DUPLICATEDID) {
					throwConflictException(SyncResultType.DUPLICATEDID, resultMap);
				} else {
					currentStatus.setResultType(SyncResultType.UPDATED);
				}
			}
		}

		if (currentStatus.getResultType() == SyncResultType.UPDATED) {
			throwConflictException(SyncResultType.UPDATED, resultMap);
		}

		repository.save(currentStatus);

		return currentStatus;
	}

	/**
	 * 上り更新時刻を決定します.<br>
	 * 現在時刻を使用します.
	 *
	 * @return
	 */
	private long generateUploadTime() {

		return new Date().getTime();
	}

	/**
	 * リソースに対してアクションに応じたリクエストメソッドを呼び出すヘルパー.<br>
	 * エレメント型を固定することで、JSONからの型変換を行ったエレメントをリソースに渡すことができます.
	 *
	 * @param resource リソース
	 * @param itemWrapper 更新するリソースアイテム
	 * @return 更新後のリソースアイテム
	 */
	private ResourceItemWrapper doSyncUpload(SyncResource<?> resource, ResourceItemWrapper itemWrapper)
			throws ConflictException {

		switch (itemWrapper.getAction()) {
			case CREATE:
				return resource.create(itemWrapper);
			case UPDATE:
				return resource.update(itemWrapper);
			case DELETE:
				return resource.delete(itemWrapper);
			default:
				throw new BadRequestException("undefined action has called.");
		}

	}

	/**
	 * 競合したリソースアイテムのリストを生成し、競合発生時の例外をスローします.
	 *
	 * @param conflictType 競合タイプ
	 * @param conflictItemMap 競合したリソースアイテムの情報を保持しているMap
	 */
	private void throwConflictException(SyncResultType conflictType, Map<String, ResourceItemsContainer> conflictItemMap) {

		List<ResourceItemsContainer> containerList = new ArrayList<>();
		for (String resourceName : conflictItemMap.keySet()) {

			containerList.add(conflictItemMap.get(resourceName));
		}
		throw new ConflictException(conflictType, containerList);
	}

	/**
	 * リポジトリを検索し、前回の同期ステータスを取得します.<br>
	 * 存在しない場合は新規生成します.
	 *
	 * @param storageId クライアントのストレージID
	 * @return 同期ステータスオブジェクト
	 */
	private SyncStatus currentStatus(String storageId) {

		SyncStatus status = repository.findOne(storageId);
		if (status == null) {
			status = new SyncStatus(storageId);
		}
		return status;
	}
}