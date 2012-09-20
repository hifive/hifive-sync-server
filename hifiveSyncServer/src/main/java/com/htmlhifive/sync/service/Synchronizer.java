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

import java.util.List;

import com.htmlhifive.sync.jsonctrl.download.DownloadRequestMessage;
import com.htmlhifive.sync.jsonctrl.upload.UploadRequestMessage;

/**
 * リソースに対する同期処理を実行するサービスインタフェース.
 *
 * @author kishigam
 */
public interface Synchronizer {

	/**
	 * 下り更新を実行します.<br>
	 * 前回同期時刻以降、messageで指定したリソースにおける更新データをGETします.
	 *
	 * @param storageId クライアントのストレージID
	 * @param requestMessages 下り更新のリクエストメッセージのリスト
	 * @return 下り更新結果オブジェクト
	 */
	public SyncDownloadResult syncDownload(final String storageId,
			final List<? extends DownloadRequestMessage> requestMessages);

	/**
	 * 上り更新を実行します.<br>
	 * 対象のリソースを判断し、リソースエレメントの更新内容に応じてリクエストを発行します.
	 *
	 * @param storageId クライアントのストレージID
	 * @param requestMessages 上り更新のリクエストメッセージのリスト
	 * @return 上り更新結果オブジェクト
	 */
	public SyncUploadResult syncUpload(String storageId, List<? extends UploadRequestMessage<?>> requestMessages);
}