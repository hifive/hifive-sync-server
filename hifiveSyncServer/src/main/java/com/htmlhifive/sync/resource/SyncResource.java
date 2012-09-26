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
package com.htmlhifive.sync.resource;

import java.util.List;

/**
 * 「リソース」を表すインターフェース.<br>
 * このフレームワークにおける「リソース」とは、 ある形を持ったデータの集合を表します.<br>
 * リソースはリソースアイテムを持ちます。リソースアイテムはデータ同期の最小単位です.
 *
 * @param <T> アイテムのデータ型
 */
public interface SyncResource<T> {

	/**
	 * 指定されたリソースアイテムIDを持つリソースアイテムを取得します.<br>
	 *
	 * @param resourceItemId リソースアイテムID
	 * @return リソースアイテムのラッパーオブジェクト
	 */
	ResourceItemWrapper read(String resourceItemId);

	/**
	 * クエリの条件に合致する全リソースアイテムを取得します.<br>
	 * ロックは考慮しません.
	 *
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 */
	List<ResourceItemWrapper> readByQuery(ResourceQueryConditions query);

	/**
	 * リソースアイテムを新規追加登録します.
	 *
	 * @param itemWrapper 登録リソースアイテム
	 * @return 登録されたリソースアイテム
	 */
	ResourceItemWrapper create(ResourceItemWrapper itemWrapper);

	/**
	 * リクエストヘッダが指定するリソースアイテムを更新します.<br>
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param item 更新後のリソースアイテム
	 * @return 更新結果を含む同期レスポンスのリスト
	 */
	ResourceItemWrapper update(ResourceItemWrapper itemWrapper);

	/**
	 * リクエストヘッダが指定するリソースアイテムを削除します.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @return 削除結果を含む同期レスポンスのリスト
	 */
	ResourceItemWrapper delete(ResourceItemWrapper itemWrapper);

	/**
	 * このリソースのリソース名を返します.
	 *
	 * @return リソース名
	 */
	String getResourceName();

	/**
	 * このリソースのアイテム型を返します.
	 *
	 * @return アイテムの型を表すClassオブジェクト
	 */
	Class<T> getItemType();

	/**
	 * リソースのロックを管理するマネージャを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param lockManager セットする lockManager
	 */
	void setLockManager(LockManager lockManager);

	/**
	 * ロックエラー発生時の更新方法を指定するストラテジーオブジェクトを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param updateStrategy セットする updateStrategy
	 */
	void setUpdateStrategy(UpdateStrategy updateStrategy);
}
