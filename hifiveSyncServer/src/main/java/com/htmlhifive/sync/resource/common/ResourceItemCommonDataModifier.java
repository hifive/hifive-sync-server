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

/**
 * リソースアイテム共通データを更新するためのサービスインターフェース.<br>
 *
 * @author kishigam
 */
public interface ResourceItemCommonDataModifier {

	/**
	 * リソースアイテム共通クラスを指定された情報で更新します.<br>
	 * 指定されたアクションが{@link SyncAction#CREATE}の場合は新規登録されます.
	 *
	 * @param resourceName リソース名
	 * @param resourceItemId リソースアイテムID
	 * @param targetItemId ターゲットアイテムID
	 * @param action アクション
	 * @param updateTime 更新時刻
	 */
	void modify(String resourceName, String resourceItemId, String targetItemId, SyncAction action, long updateTime);
}
