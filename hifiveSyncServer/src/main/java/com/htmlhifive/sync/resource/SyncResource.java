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

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.service.DownloadCommonData;
import com.htmlhifive.sync.service.UploadCommonData;

/**
 * 「リソース」を表すインターフェース.<br>
 * このフレームワークにおける「リソース」とは、 ある形を持ったデータの集合を表します.<br>
 * リソースはリソースアイテムを持ちます。リソースアイテムはデータ同期の最小単位です.
 *
 * @param <T> アイテムのデータ型
 */
public interface SyncResource<T> {

	/**
	 * 指定されたリソースアイテム共通データに対応するリソースアイテムを取得します.<br>
	 * 渡される共通データには、リソース名が含まれていない可能性があります.<br>
	 * リソース名は各リソース実装において{@link SyncResourceService}を参照し、設定する必要があります.
	 *
	 * @param itemCommonData リソースアイテム共通データ
	 * @return リソースアイテムのラッパーオブジェクト
	 */
	public ResourceItemWrapper<T> getResourceItem(ResourceItemCommonData itemCommonData);

	/**
	 * クエリの条件に合致する全リソースアイテムを取得します.<br>
	 * ロックは考慮しません.
	 *
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 */
	List<ResourceItemWrapper<T>> executeQuery(ResourceQueryConditions query);

	/**
	 * 指定されたリソースアイテム共通データに対応するリソースアイテムをロックし、取得します.<br>
	 * 渡される共通データには、リソース名が含まれていない可能性があります.<br>
	 * リソース名は各リソース実装において{@link SyncResourceService}を参照し、設定する必要があります.
	 *
	 * @param downloadCommon 下り更新共通データ
	 * @param itemCommonData リソースアイテム共通データ
	 * @return リソースアイテムのラッパーオブジェクト
	 */
	ResourceItemWrapper<T> getResourceItemWithLock(DownloadCommonData downloadCommon,
			ResourceItemCommonData itemCommonData);

	/**
	 * クエリの条件に合致する全リソースアイテムをロックし、取得します.
	 *
	 * @param downloadCommon 下り更新共通データ
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 */
	List<ResourceItemWrapper<T>> executeQueryWithLock(DownloadCommonData downloadCommon, ResourceQueryConditions query);

	/**
	 * リソースアイテムを新規登録します.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 新規登録されたアイテムの情報を含むラッパーオブジェクト
	 */
	ResourceItemWrapper<T> create(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon, T item);

	/**
	 * リソースアイテムを指定されたアイテムの内容で更新します.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 更新されたアイテムの情報を含むラッパーオブジェクト
	 */
	ResourceItemWrapper<T> update(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon, T item);

	/**
	 * リクエストヘッダが指定するリソースアイテムを削除します.<br>
	 * 競合の解決方法によって、実際にはアイテムが更新される可能性があります.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @return 削除、あるいは更新されたアイテムの情報を含むラッパーオブジェクト
	 */
	ResourceItemWrapper<T> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon);

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
	 * このリソースのアイテム型に変換するためのコンバータオブジェクトを返します.
	 *
	 * @return アイテムの型を表すClassオブジェクト
	 */
	ResourceItemConverter<T> getResourceItemConverter();

	/**
	 * リソースのロックを管理するマネージャを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param lockManager セットする lockManager
	 */
	void setLockStrategy(LockStrategy lockManager);

	/**
	 * ロックエラー発生時の更新方法を指定するストラテジーオブジェクトを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param updateStrategy セットする updateStrategy
	 */
	void setUpdateStrategy(UpdateStrategy updateStrategy);
}
