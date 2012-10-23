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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.service.DefaultSynchronizer;
import com.htmlhifive.sync.service.Synchronizer;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.lock.LockRequest;
import com.htmlhifive.sync.service.lock.LockResponse;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * 同期リクエストを処理するコントローラクラス.<br>
 * 4つの機能をクライアントに提供します.<br>
 * <ul>
 * <li>リソースアイテムの下り更新(download) ： /download
 * <li>リソースアイテムの上り更新(upload) ： /upload
 * <li>リソースアイテムのロック(getLock) ： /getlock
 * <li>リソースアイテムのロック開放(releaseLock) ： /releaselock
 * </ul>
 * いずれの機能も、JSONによってデータを送受信します.
 *
 * @author kishigam
 */
@Controller
public class JsonSyncController {

	/**
	 * 同期処理を実行するシンクロナイザー.
	 */
	@Resource(type = DefaultSynchronizer.class)
	private Synchronizer synchronizer;

	/**
	 * 下り更新処理を行うクライアントからのリクエストを処理し、結果をレスポンスとして返します.<br>
	 *
	 * @param request 下り更新リクエストデータ
	 * @return 下り更新レスポンスデータ(JSON形式)
	 */
	@RequestMapping(value = "/download", method = RequestMethod.POST, params = {}, headers = {
			"Content-Type=application/json", "Accept=application/json" })
	public ResponseEntity<DownloadResponse> download(@RequestBody DownloadRequest request) {

		boolean isInitialDownload = false;

		// 下り更新共通データがない場合、およびストレージIDが含まれていない場合は初回アクセス
		// ストレージIDを生成、リクエストの下り更新共通データにセット
		if (request.getDownloadCommonData() == null) {
			isInitialDownload = true;
			request.setDownloadCommonData(new DownloadCommonData(generateNewStorageId()));
		}
		if (request.getDownloadCommonData().getStorageId() == null) {
			isInitialDownload = true;
			request.getDownloadCommonData().setStorageId(generateNewStorageId());
		}

		try {
			// 下り更新サービスを呼び出し
			DownloadResponse response = synchronizer.download(request);

			// 初回アクセス以外はストレージIDをレスポンスデータから除外するためnullをセット
			if (!isInitialDownload) {
				response.getDownloadCommonData().setStorageId(null);
			}

			// ロックトークンをレスポンスデータから除外するためnullをセット
			response.getDownloadCommonData().setLockToken(null);

			// HTTPレスポンスをリターン
			return createHttpResponseEntity(response, HttpStatus.OK);

		} catch (LockException e) {
			// 1件でもロックエラーが発生した場合、423レスポンスをリターンする
			return createHttpResponseEntity(HttpStatus.LOCKED);
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
	 * 上り更新処理を行うクライアントからのリクエストを処理し、結果をレスポンスとして返します.<br>
	 *
	 * @param request 上り更新リクエストデータ
	 * @return 上り更新レスポンスデータ(JSON形式)
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST, params = {}, headers = {
			"Content-Type=application/json", "Accept=application/json" })
	public ResponseEntity<UploadResponse> upload(@RequestBody UploadRequest request) {

		try {
			// 上り更新サービスを呼び出し
			UploadResponse response = synchronizer.upload(request);

			// ストレージID、競合タイプ(NONE)、ロックトークンをレスポンスデータから除外するためnullをセット
			response.getUploadCommonData().setStorageId(null);
			response.getUploadCommonData().setConflictType(null);
			response.getUploadCommonData().setLockToken(null);

			// レスポンスデータを生成し、リターン
			return createHttpResponseEntity(response, HttpStatus.OK);

		} catch (ConflictException e) {
			// 競合発生時は409レスポンスをリターンする
			return createHttpResponseEntity(e.getResponse(), HttpStatus.CONFLICT);
		} catch (LockException e) {
			// 1件でもロックエラーが発生した場合、423レスポンスをリターンする
			return createHttpResponseEntity(HttpStatus.LOCKED);
		}
	}

	/**
	 * リソースアイテムをロックするクライアントからのリクエストを処理し、結果をレスポンスとして返します.<br>
	 * TODO 次期バージョンにて実装予定
	 *
	 * @param request ロック取得リクエストデータ
	 * @return ロックレスポンスデータ(JSON形式)
	 */
	@Deprecated
	@RequestMapping(value = "/getlock", method = RequestMethod.POST, params = {}, headers = {
			"Content-Type=application/json", "Accept=application/json" })
	public ResponseEntity<LockResponse> getLock(@RequestBody LockRequest request) {

		try {
			// ロック取得サービスを呼び出し
			@SuppressWarnings("deprecation")
			LockResponse response = synchronizer.getLock(request);

			// ストレージIDをレスポンスデータから除外するためnullをセット
			response.getLockCommonData().setStorageId(null);

			// レスポンスデータを生成し、リターン
			return createHttpResponseEntity(response, HttpStatus.OK);

		} catch (LockException e) {
			// 1件でもロックエラーが発生した場合、423レスポンスをリターンする
			return createHttpResponseEntity(HttpStatus.LOCKED);
		}
	}

	/**
	 * ロックを開放するクライアントからのリクエストを処理し、結果をレスポンスとして返します.<br>
	 * TODO 次期バージョンにて実装予定
	 *
	 * @param request ロックリクエストデータ
	 * @return ロックレスポンスデータ(JSON形式)
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	@RequestMapping(value = "/releaselock", method = RequestMethod.POST, params = {}, headers = {
			"Content-Type=application/json", "Accept=application/json" })
	public ResponseEntity<Void> releaseLock(@RequestBody LockRequest request) {

		try {
			// ロック開放サービスを呼び出し
			synchronizer.releaseLock(request);

			// ボディなしのレスポンスデータを生成し、リターン
			return createHttpResponseEntity(HttpStatus.OK);

		} catch (LockException e) {
			// 1件でもロックエラーが発生した場合、423レスポンスをリターンする
			return createHttpResponseEntity(HttpStatus.LOCKED);
		}
	}

	/**
	 * レスポンスボディとステータスコードからHTTPレスポンスエンティティを返します.
	 *
	 * @param body リクエストボディ
	 * @param status ステータスコードオブジェクト
	 * @return HTTPレスポンスエンティティ
	 */
	private <T> ResponseEntity<T> createHttpResponseEntity(T body, HttpStatus status) {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);

		return new ResponseEntity<>(body, responseHeaders, status);
	}

	/**
	 * ステータスコードからボディを含まないHTTPレスポンスエンティティを返します.
	 *
	 * @param status ステータスコードオブジェクト
	 * @return HTTPレスポンスエンティティ
	 */
	private <T> ResponseEntity<T> createHttpResponseEntity(HttpStatus status) {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		//		responseHeaders.add("Content-Type", "application/json;charset=utf-8");

		return new ResponseEntity<>(null, responseHeaders, status);
	}
}