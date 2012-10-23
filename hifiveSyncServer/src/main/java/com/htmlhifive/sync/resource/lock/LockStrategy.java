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
package com.htmlhifive.sync.resource.lock;

import java.util.List;

import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonLockData;
import com.htmlhifive.sync.service.SyncCommonData;

/**
 * リソースごとのロック取得ロジックを実装するクラスのインタフェース.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Deprecated
public interface LockStrategy {

	/**
	 * リソースアイテムの現在のロック状態をチェックし、指定されたロックトークンで指定されたロックを持っていない場合{@link LockException}をスローします.<br>
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @return リソースアイテム共通ロックデータ
	 */
	void checkLockStatus(SyncCommonData syncCommon, ResourceItemCommonData itemCommon, ResourceLockStatusType required);

	/**
	 * 1件のリソースアイテムを指定されたロック状態でロックします.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param lockStatus ロック状態
	 * @return リソースアイテム共通ロックデータ
	 * @throws LockException ロックが取得できなかったとき
	 */
	ResourceItemCommonLockData lock(SyncCommonData syncCommon, ResourceItemCommonData itemCommon,
			ResourceLockStatusType lockStatus) throws LockException;

	/**
	 * Mapで指定されたすべてのリソースアイテムを指定されたロック状態でロックします.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @param lockStatus ロック状態
	 * @return リソースアイテム共通ロックデータ
	 * @throws LockException ロックが取得できなかったとき
	 */
	ResourceItemCommonLockData lock(SyncCommonData syncCommon, List<ResourceItemCommonData> itemCommonList,
			ResourceLockStatusType lockStatus) throws LockException;

	/**
	 * リソースアイテム共通データのリストに含まれる対象アイテムのうち、クエリの条件を満たすリソースアイテムを、指定されたロック状態でロックします.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @param query クエリ
	 * @param lockStatus ロック状態
	 * @return クエリの条件を満たすリソースアイテム共通ロックデータのリスト
	 * @throws LockException ロックが取得できなかったとき
	 */
	List<ResourceItemCommonLockData> lock(SyncCommonData syncCommon, List<ResourceItemCommonData> itemCommonList,
			List<ResourceQueryConditions> query, ResourceLockStatusType lockStatus) throws LockException;

	/**
	 * 指定されたリソースアイテムのロックを開放します.<br>
	 * ロック状態は{@link ResourceLockStatusType#UNLOCK}になります.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 */
	void unlock(SyncCommonData syncCommon, ResourceItemCommonData itemCommon);
}
