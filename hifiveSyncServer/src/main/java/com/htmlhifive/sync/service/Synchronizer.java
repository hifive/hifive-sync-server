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
import java.util.Map;

import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.ResourceQuery;

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
	 * @param storageId クライアントのストレージID
	 * @param queries クエリリスト(リソース別Map)
	 * @return 下り更新結果を含む同期ステータスオブジェクト
	 */
	public SyncStatus download(String storageId, Map<String, List<ResourceQuery>> queries);

	/**
	 * 上り更新を実行します.<br>
	 * 対象のリソースを判断し、リソースアイテムの更新内容に応じた更新処理を呼び出します.
	 *
	 * @param storageId クライアントのストレージID
	 * @param lastUploadTime クライアントごとの前回上り更新時刻
	 * @param resourceItems リソースアイテムリスト
	 * @return 上り更新結果を含む同期ステータスオブジェクト
	 */
	public SyncStatus upload(String storageId, long lastUploadTime, List<ResourceItemWrapper> resourceItems);
}