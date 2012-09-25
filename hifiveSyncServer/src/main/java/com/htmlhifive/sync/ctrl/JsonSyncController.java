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
package com.htmlhifive.sync.ctrl;

import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.service.SyncStatus;
import com.htmlhifive.sync.service.Synchronizer;

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
	 * 初めて同期処理を行うクライアントからのリクエストを受け付け、下り更新処理のレスポンスを返します.<br>
	 *
	 * @param request JSON形式の同期リクエストデータ(初回下り更新用)
	 * @return JSON形式の同期レスポンスデータ(初回下り更新用)
	 */
	@RequestMapping(value = "/download", method = RequestMethod.POST, params = {}, headers = {
			"Accept=application/json", "Content-Type=application/json" })
	public ResponseEntity<DownloadResponse> syncInit(final @RequestBody DownloadRequest request) {

		// ストレージIDを新規採番し、下り更新サービスを呼び出し
		SyncStatus statusAfterDownload = synchronizer.download(generateNewStorageId(), request.getQueries());

		// レスポンスデータ(初回用)を生成、HTTPレスポンスをリターン
		return createResponseEntity(new DownloadResponse(statusAfterDownload.getStorageId(), statusAfterDownload),
				HttpStatus.OK);
	}

	/**
	 * 同期処理を行うクライアントからのリクエストを受け付け、下り更新処理のレスポンスを返します.<br>
	 * クエリパラメータとして、クライアントのストレージIDが指定されています.
	 *
	 * @param request JSON形式の同期リクエストデータ(下り更新用)
	 * @return JSON形式の同期レスポンスデータ(下り更新用)
	 */
	@RequestMapping(value = "/download", method = RequestMethod.POST, params = { "storageid" }, headers = {
			"Accept=application/json", "Content-Type=application/json" })
	public ResponseEntity<DownloadResponse> syncDownload(final @RequestParam("storageid") String storageId,
			final @RequestBody DownloadRequest request) {

		// ストレージIDを渡し、下り更新サービスを呼び出し
		SyncStatus statusAfterDownload = synchronizer.download(storageId, request.getQueries());

		// レスポンスデータを生成、HTTPレスポンスをリターン
		return createResponseEntity(new DownloadResponse(statusAfterDownload), HttpStatus.OK);
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
	 * クエリパラメータとして、クライアントのストレージIDが指定されています.
	 *
	 * @param request JSON形式の同期リクエストデータ(上り更新用)
	 * @return JSON形式の同期レスポンスデータ(上り更新用)
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = { "storageid" }, headers = {
			"Accept=application/json", "Content-Type=application/json" })
	public ResponseEntity<?> syncUpload(final @RequestParam("storageid") String storageId,
			final @RequestBody UploadRequest request) {

		try {
			// ストレージIDを渡し、下り更新サービスを呼び出し
			SyncStatus statusAfterUpload = synchronizer.upload(storageId, request.getLastUploadTime(),
					request.getResourceItems());

			// レスポンスデータを生成し、リターン
			return createResponseEntity(new UploadResponse(statusAfterUpload), HttpStatus.OK);

		} catch (ConflictException e) {

			// 競合時用のレスポンスデータを生成し、リターン
			UploadConflictResponse responseBodyOnConflict = new UploadConflictResponse(e);

			return createResponseEntity(responseBodyOnConflict, HttpStatus.CONFLICT);
		}
	}

	/**
	 * HTTPレスポンスボディとステータスコードからレスポンスエンティティを返します.
	 *
	 * @param body リクエストボディ
	 * @param status ステータスコードオブジェクト
	 * @return HTTPレスポンスエンティティ
	 */
	private <T> ResponseEntity<T> createResponseEntity(T body, HttpStatus status) {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json;charset=utf-8");

		return new ResponseEntity<>(body, responseHeaders, status);
	}
}