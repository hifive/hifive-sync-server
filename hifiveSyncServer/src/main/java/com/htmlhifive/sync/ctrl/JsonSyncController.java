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
import com.htmlhifive.sync.service.Synchronizer;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * 同期リクエストを処理するコントローラクラス.<br>
 * 4つの機能をクライアントに提供します.<br>
 * <ul>
 * <li>リソースアイテムの下り更新(download) ： /download
 * <li>リソースアイテムの上り更新(upload) ： /upload
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
	@Resource
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

		// 下り更新共通データがない場合、ストレージIDを生成、新規生成の下り更新共通データにセット
		if (request.getDownloadCommonData() == null) {
			isInitialDownload = true;

			request.setDownloadCommonData(new DownloadCommonData(null));
		}

		DownloadCommonData requestCommon = request.getDownloadCommonData();

		// 下り更新共通データにストレージIDが含まれていない場合、
		// ストレージIDを生成、リクエストの下り更新共通データにセット
		if (requestCommon.getStorageId() == null) {
			isInitialDownload = true;

			requestCommon.setStorageId(generateNewStorageId());
			request.setDownloadCommonData(requestCommon);
		}

		// 下り更新サービスを呼び出し
		DownloadResponse response = synchronizer.download(request);

		// 初回アクセス以外はストレージIDをレスポンスデータから除外するためnullをセット
		if (!isInitialDownload) {
			response.getDownloadCommonData().setStorageId(null);
		}

		// HTTPレスポンスをリターン
		return createHttpResponseEntity(response, HttpStatus.OK);
	}

	/**
	 * 新規ストレージIDを採番します.<br>
	 * ランダムなUUID(タイプ4)を使用します.
	 *
	 * @return
	 */
	protected String generateNewStorageId() {

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

			// レスポンスデータを生成し、リターン
			return createHttpResponseEntity(response, HttpStatus.OK);

		} catch (ConflictException e) {
			// 競合発生時は409レスポンスをリターンする
			return createHttpResponseEntity(e.getResponse(), HttpStatus.CONFLICT);
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
}