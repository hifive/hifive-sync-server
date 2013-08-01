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
package com.htmlhifive.sync.resource;

import java.util.List;

import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncRequestCommonData;

/**
 * リソースアイテムの同期操作を実行するSynchronizerインターフェース.<br>
 */
public interface Synchronizer {

	/**
	 * 指定されたIDを持つだけの新規リソースアイテム共通データを生成します.<br/>
	 * このメソッドにより共通データを生成した後、{@link Synchronizer#modify(ResourceItemCommonData)}メソッドを使用して、内容を更新する必要があります.
	 *
	 * @param itemCommonId リソースアイテム共通データのIDオブジェクト
	 * @return 生成された共通データ
	 */
	ResourceItemCommonData getNew(ResourceItemCommonDataId itemCommonId);

	/**
	 * 指定されたIDを持つリソースアイテム共通データを悲観的ロック("for update")を用いて取得します.<br/>
	 *
	 * @param itemCommonId リソースアイテム共通データのIDオブジェクト
	 * @return リソースアイテム共通データ
	 */
	ResourceItemCommonData getForUpdate(ResourceItemCommonDataId itemCommonId);

	/**
	 * 指定された対象リソースアイテムのID値を持ち、指定時刻以降に更新されているリソースアイテム共通データを取得します.
	 *
	 * @param resourceName リソース名
	 * @param targetItemIdList 対象リソースアイテムのID値のリスト
	 * @param modifiedSince 検索に用いる時刻
	 * @return 更新されているリソースアイテム共通データ
	 */
	List<ResourceItemCommonData> getModified(String resourceName, List<String> targetItemIdList, long modifiedSince);

	/**
	 * 指定された対象リソースアイテムのID値を持ち、指定時刻以降に更新されているリソースアイテム共通データを悲観的ロック("for update")を用いて取得します.<br/>
	 * ID値の順にソートして実行されるため、返されるリストは元のID値リストの順とは異なる場合があります.
	 *
	 * @param resourceName リソース名
	 * @param targetItemIdList 対象リソースアイテムのID値のリスト
	 * @param modifiedSince 検索に用いる時刻
	 * @return 更新されているリソースアイテム共通データ
	 */
	List<ResourceItemCommonData> getModifiedForUpdate(String resourceName, List<String> targetItemIdList,
			long modifiedSince);

	/**
	 * リソースアイテム共通データのバージョン比較により、リソースアイテムの更新競合が発生しているときtrueを返します.
	 *
	 * @param clientItemCommon update(/delete)対象リソースアイテムの最終更新時刻
	 * @param currentItemCommon サーバで保持している現在の共通データ
	 * @return 競合が発生している場合true.
	 */
	boolean isConflicted(ResourceItemCommonData clientItemCommon, ResourceItemCommonData currentItemCommon,
			SyncRequestCommonData requestCommon);

	/**
	 * リソースアイテム共通データを指定されたアイテムの内容で更新します.
	 *
	 * @param itemCommon リソースアイテム共通データ
	 * @return 更新後のリソースアイテム共通データ
	 */
	ResourceItemCommonData modify(ResourceItemCommonData itemCommon);

	SyncConfigurationParameter getSyncConfigurationParameter();

	void setSyncConfigurationParameter(SyncConfigurationParameter syncConfigurationParameter);

	UpdateStrategy getDefaultUpdateStrategy();

	void setDefaultUpdateStrategy(UpdateStrategy defaultUpdateStrategy);
}
