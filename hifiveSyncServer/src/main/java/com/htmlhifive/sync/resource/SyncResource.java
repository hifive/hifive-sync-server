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
import java.util.Properties;

import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.lock.LockStrategy;
import com.htmlhifive.sync.resource.lock.ResourceLockStatusType;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncCommonData;
import com.htmlhifive.sync.service.lock.LockCommonData;
import com.htmlhifive.sync.service.upload.UploadCommonData;

/**
 * 「リソース」を表すインターフェース.<br>
 * このフレームワークにおけるリソースとは、 ある形を持ったデータの集合を表します.<br>
 * このデータはリソースアイテムと呼ばれ、データ同期の最小単位になります.
 *
 * @param <I> アイテムのデータ型
 */
@SuppressWarnings("deprecation")
public interface SyncResource<I> {

	/**
	 * 指定されたリソースアイテム共通データに対応するリソースアイテムをそれぞれ取得します.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommonDataList リソースアイテム共通データのリスト
	 * @return リソースアイテムのラッパーオブジェクトのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	List<ResourceItemWrapper<I>> get(SyncCommonData syncCommon, List<ResourceItemCommonData> itemCommonDataList);

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
	 * 指定されたリソースアイテムをロックします.<br>
	 * TODO: 次期バージョンにて実装予定
	 *
	 * @param lockCommon ロック取得共通データ
	 * @param itemCommonDataList リソースアイテム共通データのリスト
	 * @return ロックしたアイテムの情報を含むラッパーオブジェクトのリスト
	 * @throws LockException ロックできなかった場合
	 */
	@Deprecated
	List<ResourceItemWrapper<I>> lock(LockCommonData lockCommon, List<ResourceItemCommonData> itemCommonDataList);

	/**
	 * 指定されたリソースアイテムのロックを開放します.<br>
	 * TODO: 次期バージョンにて実装予定
	 *
	 * @param lockCommon ロック取得共通データ
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @throws LockException ロックの開放に失敗した場合
	 */
	@Deprecated
	void releaseLock(LockCommonData lockCommon, List<ResourceItemCommonData> itemCommonList);

	/**
	 * ロックされている全リソースアイテムの共通データを返します.<br>
	 * TODO: 次期バージョンにて実装予定
	 *
	 * @param lockCommonData ロック取得共通データ
	 * @return リソースアイテム共通データのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	@Deprecated
	List<ResourceItemCommonData> lockedItemsList(LockCommonData lockCommonData);

	/**
	 * 他のリクエストの影響を防止するために、指定されたリソースアイテムを"for update"状態にします.<br>
	 * この操作は、常にリソースおよびリソースアイテムの順序で実行する必要があります.
	 *
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @return アクセス権を取得したリソースアイテム共通データのリスト
	 * @throws LockException 対象リソースアイテムがロックされていた場合
	 */
	List<ResourceItemCommonData> forUpdate(List<ResourceItemCommonData> itemCommonList);

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
	 * このリソースへアクセスする際に要求されるロック方式を返します.<br>
	 * TODO: 次期バージョンにて実装予定
	 *
	 * @return リソースロック状態タイプ
	 */
	@Deprecated
	ResourceLockStatusType requiredLockStatus();

	/**
	 * このリソースのアイテム型に変換するためのコンバータオブジェクトを返します.
	 *
	 * @return アイテムの型を表すClassオブジェクト
	 */
	ResourceItemConverter<I> itemConverter();

	/**
	 * 全てのリソースに共通の設定情報を適用します.<br>
	 * リソース実装クラス(あるいはその抽象スーパークラス)では、この設定情報を参照することができます. <br>
	 * 通常、アプリケーションから使用することはありません.<br>
	 *
	 * @param resourceConfigurations 適用する設定情報
	 */
	void applyResourceConfigurations(Properties resourceConfigurations);

	/**
	 * このリソースが要求するロック状態を設定します.<br>
	 * 通常、アプリケーションから使用することはありません.<br>
	 * TODO: 次期バージョンにて実装予定
	 *
	 * @param requiredLockStatus セットする requiredLockStatus
	 */
	@Deprecated
	void setRequiredLockStatus(ResourceLockStatusType requiredLockStatus);

	/**
	 * リソースのロックを管理するマネージャを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.<br>
	 * TODO: 次期バージョンにて実装予定
	 *
	 * @param lockManager セットする lockManager
	 */
	@Deprecated
	void setLockStrategy(LockStrategy lockManager);

	/**
	 * ロックエラー発生時の更新方法を指定するストラテジーオブジェクトを設定します.<br>
	 * 通常、アプリケーションから使用することはありません.
	 *
	 * @param updateStrategy セットする updateStrategy
	 */
	void setUpdateStrategy(UpdateStrategy updateStrategy);
}
