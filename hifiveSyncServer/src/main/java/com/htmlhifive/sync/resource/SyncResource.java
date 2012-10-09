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
import com.htmlhifive.sync.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.download.DownloadCommonData;
import com.htmlhifive.sync.service.lock.LockCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * 「リソース」を表すインターフェース.<br>
 * このフレームワークにおけるリソースとは、 ある形を持ったデータの集合を表します.<br>
 * このデータはリソースアイテムと呼ばれ、データ同期の最小単位になります.
 *
 * @param <I> アイテムのデータ型
 */
public interface SyncResource<I> {

	/**
	 * 指定されたリソースアイテム共通データに対応するリソースアイテムをそれぞれ取得します.
	 *
	 * @param downloadCommon 下り更新共通データ
	 * @param itemCommonDataList リソースアイテム共通データのリスト
	 * @return リソースアイテムのラッパーオブジェクトのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	List<ResourceItemWrapper<I>> get(DownloadCommonData downloadCommon, List<ResourceItemCommonData> itemCommonDataList);

	/**
	 * クエリの条件に合致する全リソースアイテムを取得します.
	 *
	 * @param downloadCommon 共通データ
	 * @param query クエリオブジェクト
	 * @return 条件に合致するリソースアイテムのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	List<ResourceItemWrapper<I>> getByQuery(SyncCommonData syncCommon, ResourceQueryConditions query);

	/**
	 * リソースアイテムを新規登録します.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 新規登録されたアイテムの情報を含むラッパーオブジェクト
	 */
	ResourceItemWrapper<I> create(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon, I item);

	/**
	 * リソースアイテムを指定されたアイテムの内容で更新します. <br>
	 * 競合が発生し、その解決方法によってはアイテムが新規登録、削除される可能性があります.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param item アイテム
	 * @return 更新されたアイテムの情報を含むラッパーオブジェクト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	ResourceItemWrapper<I> update(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon, I item);

	/**
	 * リソースアイテムを削除します. 競合が発生し、その解決方法によってはアイテムが新規登録、更新される可能性があります.
	 *
	 * @param uploadCommon 上り更新共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @return 更新されたアイテムの情報を含むラッパーオブジェクト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	ResourceItemWrapper<I> delete(UploadCommonData uploadCommon, ResourceItemCommonData itemCommon);

	/**
	 * 指定されたリソースアイテムをロックします.
	 *
	 * @param lockCommon ロック取得共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @throws LockException ロックできなかった場合
	 */
	void lock(LockCommonData lockCommon, ResourceItemCommonData itemCommon);

	/**
	 * 指定されたリソースアイテムのロックを開放します.
	 *
	 * @param lockCommonData ロック取得共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	void releaseLock(LockCommonData lockCommonData, ResourceItemCommonData itemCommon);

	/**
	 * ロックされている全リソースアイテムの共通データを返します.<br>
	 *
	 * @param lockCommonData ロック取得共通データ
	 * @return リソースアイテム共通データのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	List<ResourceItemCommonData> lockedItemInfo(LockCommonData lockCommonData);

	/**
	 * 指定されたリソースアイテムのアクセス権を確保します.<br>
	 * 更新やロックの対象とする全てのリソースアイテムのアクセス権を正しい順序で確保することで、デッドロックによる処理失敗の可能性をなくすことができます.
	 *
	 * @param id リソースアイテム共通データID
	 * @return アクセス権を取得したリソースアイテム共通データ
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	ResourceItemCommonData forUpdate(ResourceItemCommonDataId id);

	/**
	 * このリソースのリソース名を返します.
	 *
	 * @return リソース名
	 */
	String name();

	/**
	 * このリソースのアイテム型を返します.
	 *
	 * @return アイテムの型を表すClassオブジェクト
	 */
	Class<I> itemType();

	/**
	 * このリソースのアイテム型に変換するためのコンバータオブジェクトを返します.
	 *
	 * @return アイテムの型を表すClassオブジェクト
	 */
	ResourceItemConverter<I> itemConverter();

	/**
	 * このリソースが要求するロック状態を設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param requiredLockStatus セットする requiredLockStatus
	 */
	void setRequiredLockStatus(ResourceLockStatusType requiredLockStatus);

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
