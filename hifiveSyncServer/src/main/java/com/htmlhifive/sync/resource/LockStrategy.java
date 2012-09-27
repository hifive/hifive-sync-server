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
import java.util.Map;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.service.SyncCommonData;

/**
 * リソースごとのロック取得ロジックを実装するクラスのインタフェース.
 *
 * @author kawaguch
 */
public interface LockStrategy {

	/**
	 * 1件のリソースアイテムを指定されたモードでロックします.
	 *
	 * @param syncCommon
	 * @param commonData
	 * @param item
	 * @param lockMode
	 * @throws LockException ロックが取得できなかったとき
	 */
	public <T> void lock(SyncCommonData syncCommon, ResourceItemCommonData itemCommon, T item,
			ResourceLockModeType lockMode) throws LockException;

	/**
	 * Mapで指定されたすべてのリソースアイテムを指定されたモードでロックします.
	 *
	 * @param syncCommon
	 * @param itemMap
	 * @param lockMode
	 * @throws LockException ロックが取得できなかったとき
	 */
	public <T> void lock(SyncCommonData syncCommon, Map<T, ResourceItemCommonData> itemMap,
			ResourceLockModeType lockMode) throws LockException;

	/**
	 * リソースアイテム共通データのリストに含まれる対象アイテムのうち、クエリの条件を満たすリソースアイテムを、指定されたモードでロックします.
	 *
	 * @param syncCommon
	 * @param commonDataList
	 * @param query
	 * @param lockMode
	 * @return クエリの条件を満たすリソースアイテム共通データのリスト
	 * @throws LockException ロックが取得できなかったとき
	 */
	public <T> List<ResourceItemCommonData> lock(SyncCommonData syncCommon,
			List<ResourceItemCommonData> commonDataList, List<ResourceQueryConditions> query,
			ResourceLockModeType lockMode) throws LockException;

	/**
	 * 指定された1件のリソースアイテムが指定されたモードでロックされている場合trueを返します.
	 *
	 * @param syncCommon
	 * @param commonData
	 * @param item
	 * @param lockMode
	 * @return
	 */
	public <T> boolean isLocked(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item,
			ResourceLockModeType lockMode);

	/**
	 * 指定された1件のリソースアイテムが排他ロックモードでロックされている場合はfalseを返し、それ以外の場合はtrueを返します.
	 *
	 * @param syncCommon
	 * @param commonData
	 * @param item
	 * @return
	 */
	public <T> boolean canRead(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item);

	/**
	 * 指定された1件のリソースアイテムがいずれのモードでもロックされていない場合trueを返します.
	 *
	 * @param syncCommon
	 * @param commonData
	 * @param item
	 * @return
	 */
	public <T> boolean canWrite(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item);

	/**
	 * 指定されたリソースアイテムのロックを解放します.
	 *
	 * @param syncCommon
	 * @param commonData
	 * @param item
	 */
	public <T> void unlock(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item);
}
