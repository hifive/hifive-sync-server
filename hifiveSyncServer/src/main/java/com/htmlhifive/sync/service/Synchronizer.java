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

import com.htmlhifive.sync.service.download.DownloadRequest;
import com.htmlhifive.sync.service.download.DownloadResponse;
import com.htmlhifive.sync.service.upload.UploadRequest;
import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * リソースの同期処理を実行するサービスインタフェース.
 *
 * @author kishigam
 */
public interface Synchronizer {

	/**
	 * 下り更新を実行します.<br>
	 * リソースごとに、指定されたクエリでリソースアイテムを検索、取得します.
	 *
	 * @param request 下り更新リクエストデータ
	 * @return 下り更新レスポンスデータ
	 */
	public DownloadResponse download(DownloadRequest request);

	/**
	 * 上り更新を実行します.<br>
	 * 対象のリソースを判断し、リソースアイテムの更新内容に応じた更新処理を呼び出します.
	 *
	 * @param request 上り更新リクエストデータ
	 * @return 上り更新レスポンスデータ
	 */
	public UploadResponse upload(UploadRequest request);
}