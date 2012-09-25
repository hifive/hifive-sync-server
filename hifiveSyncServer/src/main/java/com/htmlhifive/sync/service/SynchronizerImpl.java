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
 * リソースの同期処理を実行するデフォルトサービス実装.<br>
 * このオブジェクトのパブリックメソッドがトランザクション境界になります.
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
	 * @param queries クエリリスト(リソース別Map)
	 * @return 下り更新結果を含む同期ステータスオブジェクト
	 */
	@Override
	public SyncStatus download(String storageId, Map<String, List<ResourceQuery>> queries) {

		// クライアントごとの(前回)同期ステータスオブジェクトを取得
		SyncStatus currentStatus = currentStatus(storageId);

		// 下り更新時刻の設定
		currentStatus.setLastDownloadTime(generateDownloadTime());

		// リソース名ごとに結果をコンテナに格納する
		Map<String, List<ResourceItemWrapper>> resultMap = new HashMap<>();

		// クエリごとに処理し、結果をコンテナに格納
		for (String resourceName : queries.keySet()) {

			// Mapにリソース名ごとのコンテナが存在しない場合は生成
			if (!resultMap.containsKey(resourceName)) {
				resultMap.put(resourceName, new ArrayList<ResourceItemWrapper>());
			}

			List<ResourceItemWrapper> resultList = resultMap.get(resourceName);
			for (ResourceQuery query : queries.get(resourceName)) {

				// synchronizerへリクエスト発行
				SyncResource<?> resource = resourceManager.locateSyncResource(resourceName);
				List<ResourceItemWrapper> resultItemList = resource.readByQuery(query);

				// 結果をマージする(同じリソースアイテムは1件のみとなる)
				for (ResourceItemWrapper itemWrapper : resultItemList) {
					if (resultList.contains(itemWrapper)) {
						resultList.add(itemWrapper);
					}
				}
			}
			resultMap.put(resourceName, resultList);
		}
		currentStatus.setResourceItems(resultMap);

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
	public SyncStatus upload(String storageId, long lastUploadTime, List<ResourceItemWrapper> resourceItems) {

		// クライアントごとの(前回)同期ステータスオブジェクトを取得
		SyncStatus currentStatus = currentStatus(storageId);

		// 二重送信判定
		// サーバ側で管理されている前回上り更新時刻より前の場合、二重送信と判断し、現在の同期ステータスをそのまま返します.
		if (currentStatus.isNewerStatusThanClient(lastUploadTime)) {
			return currentStatus;
		}

		// 上り更新時刻の設定
		currentStatus.setLastUploadTime(generateUploadTime());

		// 競合の種類ごとに、競合データリソースのアイテムリスト(リソース別Map)に格納する
		Map<SyncResultType, Map<String, List<ResourceItemWrapper>>> resultMapHolder = new HashMap<>();
		resultMapHolder.put(SyncResultType.DUPLICATEDID, new HashMap<String, List<ResourceItemWrapper>>());
		resultMapHolder.put(SyncResultType.UPDATED, new HashMap<String, List<ResourceItemWrapper>>());

		// リソースアイテムごとに処理(上り更新リクエストデータのみ、ResourceItemWrapperにリソース名を持つ)
		for (ResourceItemWrapper uploadingItemWrapper : resourceItems) {

			// 更新時刻を設定
			// TODO:lastModifiedフィールドを使用してはいけない
			uploadingItemWrapper.setUploadTime(currentStatus.getLastUploadTime());

			SyncResource<?> resource = resourceManager.locateSyncResource(uploadingItemWrapper.getResourceName());

			ResourceItemWrapper itemWrapperAfterUpload = doUpload(resource, uploadingItemWrapper);

			// 競合でない場合は次のリソースアイテムの更新へ(更新後のアイテムは使用しない)
			if (itemWrapperAfterUpload.getResultType() == SyncResultType.OK) {
				continue;
			}

			Map<String, List<ResourceItemWrapper>> resultMap = resultMapHolder.get(itemWrapperAfterUpload
					.getResultType());

			// Mapにリソース名ごとのコンテナが存在しない場合は生成
			if (!resultMap.containsKey(uploadingItemWrapper.getResourceName())) {

				List<ResourceItemWrapper> itemList = new ArrayList<>();
				resultMap.put(uploadingItemWrapper.getResourceName(), itemList);
			}

			// 競合しているアイテムをItemMapにマージ(同じリソースアイテムは1件のみとなる)
			if (!resultMap.get(resource).contains(itemWrapperAfterUpload)) {
				resultMap.get(resource).add(itemWrapperAfterUpload);
			}

			// 競合時の処理継続判断(競合のタイプごとに)
			if (!continueOnConflict(itemWrapperAfterUpload.getResultType())) {

				// 継続しない場合はConflictExceptionをスロー
				throwConflictException(itemWrapperAfterUpload.getResultType(), resultMap);
			}

			// ステータスに設定し、次のリソースアイテムの更新へ
			currentStatus.setResultType(itemWrapperAfterUpload.getResultType());
		}

		// 以下の優先順で複数の競合データをConflictExceptionでスロー
		if (currentStatus.getResultType() == SyncResultType.DUPLICATEDID) {
			throwConflictException(SyncResultType.DUPLICATEDID, resultMapHolder.get(SyncResultType.DUPLICATEDID));
		}
		if (currentStatus.getResultType() == SyncResultType.UPDATED) {
			throwConflictException(SyncResultType.UPDATED, resultMapHolder.get(SyncResultType.UPDATED));
		}

		// 競合がない場合はステータスを更新し、リターン
		return repository.save(currentStatus);
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
	 * @param itemWrapper 更新するリソースアイテム
	 * @return 更新後のリソースアイテム
	 */
	private ResourceItemWrapper doUpload(SyncResource<?> resource, ResourceItemWrapper itemWrapper)
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
	 * 更新結果ごとに、次のリソースアイテムの更新を継続するかどうかを判断し、返します.
	 *
	 * @param resultType 上り更新結果のタイプ
	 * @return 継続する場合true
	 */
	private boolean continueOnConflict(SyncResultType resultType) {

		// TODO:コンフィグ化
		return resultType != SyncResultType.DUPLICATEDID && resultType != SyncResultType.UPDATED;
	}

	/**
	 * 競合したリソースアイテムのリストを生成し、競合発生時の例外をスローします.
	 *
	 * @param conflictType 競合タイプ
	 * @param conflictItemMap 競合したリソースアイテムの情報を保持しているMap
	 */
	private void throwConflictException(SyncResultType conflictType,
			Map<String, List<ResourceItemWrapper>> conflictItemMap) {

		throw new ConflictException(conflictType, conflictItemMap);
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