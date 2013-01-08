/*
 * Copyright (C) 2012-2013 NS Solutions Corporation
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
package com.htmlhifive.sync.resource.common;

import java.util.List;

/**
 * リソースアイテム共通データを取得、更新するためのサービス実装.<br>
 *
 * @author kishigam
 */
public interface ResourceItemCommonDataService {

	/**
	 * リソースアイテム共通データのIDオブジェクトで共通データエンティティを検索し、返します.<br>
	 *
	 * @param id リソースアイテム共通データID
	 * @return 共通データエンティティ
	 */
	ResourceItemCommonData currentCommonData(ResourceItemCommonDataId id);

	/**
	 * リソース名、そのリソースにおけるアイテムのIDで共通データエンティティを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemId リソースアイテムごとのID
	 * @return 共通データエンティティ
	 */
	ResourceItemCommonData currentCommonData(String resourceName, String targetItemId);

	/**
	 * リソース名、そのリソースにおけるアイテムのIDで共通データエンティティを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemIds リソースアイテムごとのID(複数可能)
	 * @return 共通データエンティティ
	 */
	List<ResourceItemCommonData> currentCommonData(String resourceName, List<String> targetItemIds);

	/**
	 * リソースアイテム共通データのIDオブジェクトで共通データエンティティを"for update"で検索し、返します.<br>
	 *
	 * @param id リソースアイテム共通データID
	 * @return 共通データエンティティ
	 */
	ResourceItemCommonData currentCommonDataForUpdate(ResourceItemCommonDataId id);

	/**
	 * 指定されたリソースで、指定された時刻以降に更新されたリソースアイテムの共通データを検索し、返します.
	 *
	 * @param resourceName リソース名
	 * @param lastDownloadTime 前回下り更新時刻
	 * @return 共通データエンティティのリスト
	 */
	List<ResourceItemCommonData> modifiedCommonData(String resourceName, long lastDownloadTime);

	/**
	 * 指定されたリソースで、指定された時刻以降に更新されたリソースアイテムの共通データを検索し、返します.<br>
	 * 検索結果は、指定された対象リソースアイテムIDに該当するものに限定されます.
	 *
	 * @param resourceName リソース名
	 * @param lastDownloadTime 前回下り更新時刻
	 * @param targetItemIds リソースアイテムごとのID(複数可能)
	 * @return 共通データエンティティのリスト
	 */
	List<ResourceItemCommonData> modifiedCommonData(String resourceName, long lastDownloadTime,
			List<String> targetItemIds);

	/**
	 * 新規リソースに対応する共通データを保存します.
	 *
	 * @param common リソースアイテム共通データ
	 * @return 保存された共通データ
	 */
	ResourceItemCommonData saveNewCommonData(ResourceItemCommonData common);

	/**
	 * リソースに対応する共通データを保存します.<br>
	 *
	 * @param common リソースアイテム共通データ
	 * @return 保存された共通データ
	 */
	ResourceItemCommonData saveUpdatedCommonData(ResourceItemCommonData common);
}
