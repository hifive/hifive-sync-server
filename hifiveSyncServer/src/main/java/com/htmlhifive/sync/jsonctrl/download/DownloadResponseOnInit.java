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
package com.htmlhifive.sync.jsonctrl.download;

import com.htmlhifive.sync.service.AbstractSyncResult;

/**
 * 初めて同期を行うクライアントからの下り更新リクエストに対するレスポンスデータクラス.
 *
 * @author kishigam
 */
public class DownloadResponseOnInit extends DownloadResponse {

	private String storageId;

	/**
	 * 下り更新結果オブジェクトの内容から、クライアントに返すレスポンスデータを生成します.<br>
	 * 新規に採番されたリクエスト発行元クライアントのストレージIDを含みます.
	 *
	 * @param downloadResult
	 */
	public DownloadResponseOnInit(AbstractSyncResult downloadResult) {

		super(downloadResult.getCurrentSyncTime(), createResponseMessageList(downloadResult.getResultDataSet()));
		this.storageId = downloadResult.getStorageId();
	}

	/**
	 * @return storageId
	 */
	public String getStorageId() {
		return storageId;
	}

	/**
	 * @param storageId セットする storageId
	 */
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
}
