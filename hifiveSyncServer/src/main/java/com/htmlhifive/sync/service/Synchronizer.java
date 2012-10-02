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
import com.htmlhifive.sync.service.lock.LockRequest;
import com.htmlhifive.sync.service.lock.LockResponse;
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

	/**
	 * ロックの取得を実行します.<br>
	 * リソースごとに、指定されたクエリでリソースアイテムを検索、ロックを行います.<br>
	 * 全ての対象リソースアイテムがロックできた場合のみ、各リソースアイテムを上り更新するためのロックトークンを返します.<br>
	 * 1件でもロックに失敗した場合、LockExceptionがスローされ、他の全てのリソースアイテムもロックされません.<br>
	 * ロックを取得していない場合、ロックの取得をサポートしないロック方式の場合は空の結果が返ります.
	 *
	 * @param request ロックリクエストデータ
	 * @return ロック取得レスポンスデータ
	 */
	public LockResponse getLock(LockRequest request);

	/**
	 * ロックの開放を実行します.<br>
	 * ロックが開放できない場合、LockExceptionがスローされます.<br>
	 * ロックを取得していない場合、ロックの取得をサポートしないロック方式の場合は何も起こりません.
	 *
	 * @param request ロックリクエストデータ
	 */
	public void releaseLock(LockRequest request);
}