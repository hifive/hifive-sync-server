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
package com.htmlhifive.sync.jsonctrl;

import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.sync.jsonctrl.download.DownloadRequest;
import com.htmlhifive.sync.jsonctrl.download.DownloadResponseOnInit;
import com.htmlhifive.sync.jsonctrl.download.DownloadResponseOrdinary;
import com.htmlhifive.sync.jsonctrl.upload.UploadRequest;
import com.htmlhifive.sync.jsonctrl.upload.UploadResponse;
import com.htmlhifive.sync.jsonctrl.upload.UploadResponseOnConflict;
import com.htmlhifive.sync.jsonctrl.upload.UploadResponseOrdinary;
import com.htmlhifive.sync.service.AbstractSyncResult;
import com.htmlhifive.sync.service.SyncDownloadResult;
import com.htmlhifive.sync.service.SyncResultType;
import com.htmlhifive.sync.service.SyncUploadResult;
import com.htmlhifive.sync.service.Synchronizer;
import com.htmlhifive.sync.status.SyncStatusService;

/**
 * JSON形式の同期リクエストを処理するコントローラクラス.
 *
 * @author kishigam
 */
@Controller
public class JsonSyncController {

	/**
	 * 同期処理を実行するシンクロナイザー.
	 */
	@Resource
	private Synchronizer synchronizer;

	/**
	 * 二重送信発生時の処理を効率化するレスポンスキャッシュサービス.
	 */
	@Resource
	private SyncStatusService statusService;

	/**
	 * 初めて同期処理を行うクライアントからのリクエストを受け付け、下り更新処理のレスポンスを返します.<br>
	 *
	 * @param request JSON形式の同期リクエストデータ(初回下り更新用)
	 * @return JSON形式の同期レスポンスデータ(初回下り更新用)
	 */
	@Transactional
	@RequestMapping(value = "/download", method = RequestMethod.POST, params = {}, headers = {
			"Accept=application/json", "Content-Type=application/json" })
	public ResponseEntity<DownloadResponseOnInit> syncInit(final @RequestBody DownloadRequest request) {

		try {
			// ストレージIdを新規採番
			String newStorageId = generateNewStorageId();

			AbstractSyncResult downloadResult = synchronizer.syncDownload(newStorageId, request.getResources());

			// レスポンスデータ(初回用)の生成
			DownloadResponseOnInit responseBody = new DownloadResponseOnInit(downloadResult);

			return createResponseEntity(responseBody, HttpStatus.OK);

		} catch (Exception e) {
			// TODO: スタックトレースのロギング
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 同期処理を行うクライアントからのリクエストを受け付け、下り更新処理のレスポンスを返します.<br>
	 * クエリパラメータとして、クライアントのストレージIDを指定します.
	 *
	 * @param request JSON形式の同期リクエストデータ(下り更新用)
	 * @return JSON形式の同期レスポンスデータ(下り更新用)
	 */
	@Transactional
	@RequestMapping(value = "/download", method = RequestMethod.POST, params = { "storageid" }, headers = {
			"Accept=application/json", "Content-Type=application/json" })
	public ResponseEntity<DownloadResponseOrdinary> syncDownload(final @RequestParam("storageid") String storageId,
			final @RequestBody DownloadRequest request) {

		try {

			SyncDownloadResult downloadResult = synchronizer.syncDownload(storageId, request.getResources());

			// レスポンスデータの生成
			DownloadResponseOrdinary responseBody = new DownloadResponseOrdinary(downloadResult);

			return createResponseEntity(responseBody, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 新規ストレージIDを採番します.<br>
	 * ランダムなUUID(タイプ4)を使用します.
	 *
	 * @return
	 */
	private String generateNewStorageId() {

		return UUID.randomUUID().toString();
	}

	/**
	 * 同期処理を行うクライアントからのリクエストを受け付け、上り更新処理のレスポンスを返します.<br>
	 * クエリパラメータとして、クライアントのストレージIDを指定します.
	 *
	 * @param request JSON形式の同期リクエストデータ(上り更新用)
	 * @return JSON形式の同期レスポンスデータ(上り更新用)
	 */
	@Transactional
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = { "storageid" }, headers = {
			"Accept=application/json", "Content-Type=application/json" })
	public ResponseEntity<? extends UploadResponse> syncUpload(final @RequestParam("storageid") String storageId,
			final @RequestBody UploadRequest request) {

		try {

			// 二重送信の場合は前回処理結果を戻す
			// そうでない場合、上り更新処理を実行
			SyncUploadResult uploadResult = statusService.isDuplicatedRequest(storageId, request.getDataList()
					.hashCode()) ? reversionLastResult(storageId) : synchronizer.syncUpload(storageId,
					request.getDataList());

			// 上り更新結果ごとにレスポンスデータを生成

			switch (uploadResult.getResultType()) {
				case OK:

					// 上り更新処理成功の場合、今回の処理結果をキャッシュ
					statusService.applyUploadResult(storageId, request.getDataList().hashCode(),
							uploadResult.getResultDataSet());

					UploadResponseOrdinary responseBody = new UploadResponseOrdinary(uploadResult);

					return createResponseEntity(responseBody, HttpStatus.OK);

				case UPDATED:
				case DUPLICATEDID:

					// 競合時はリソースの更新が行われず、次回の二重送信判定に意味がないためキャッシュを削除する
					statusService.removeClientAccess(storageId);

					UploadResponseOnConflict responseBodyOnConflict = new UploadResponseOnConflict(uploadResult);

					return createResponseEntity(responseBodyOnConflict, HttpStatus.CONFLICT);

				default:
					throw new RuntimeException("illegal upload result");
			}

		} catch (Exception e) {

			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 前回上り更新成功時のレスポンスデータをキャッシュから取得します.<br>
	 *
	 * @param storageId クライアントのストレージID
	 * @return キャッシュの上り更新結果オブジェクト
	 */
	private SyncUploadResult reversionLastResult(String storageId) {

		SyncUploadResult result = new SyncUploadResult(storageId);
		result.setResultDataSet(statusService.reversionResponseSet(storageId));

		result.setResultType(SyncResultType.OK);

		return result;
	}

	/*
     */

	/**
	 * HTTPレスポンスボディとステータスコードからレスポンスエンティティを返します.
	 *
	 * @param body リクエストボディ
	 * @param status ステータスコードオブジェクト
	 * @return HTTPレスポンスエンティティ
	 */
	private <T extends ResponseBody> ResponseEntity<T> createResponseEntity(T body, HttpStatus status) {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");

		return new ResponseEntity<>(body, responseHeaders, status);
	}
}