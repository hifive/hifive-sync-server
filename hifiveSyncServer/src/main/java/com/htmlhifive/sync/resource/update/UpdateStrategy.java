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
package com.htmlhifive.sync.resource.update;

import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.SyncAction;

/**
 * リソースアイテムの競合解決ロジッククラスのインターフェース.<br>
 *
 * @author kishigam
 */
public interface UpdateStrategy {

	/**
	 * 競合しているリソースアイテムから戦略に従って解決を試み、解決結果としての更新状態を表す{@link SyncAction SyncActionStatus}を返します.<br/>
	 * 更新しようとしているデータを、この解決結果で更新することが基本的な対応になりますが、 例えば{@link SyncAction#NONE NONE}
	 * が返された場合、サーバデータの状態を維持する形で解決したことを表しているため、更新を行う必要はありません.<br/>
	 * {@link SyncAction#DUPLICATE DUPLICATE}や{@link SyncAction#CONFLICT CONFLICT}
	 * が返された場合は競合が解決できていないため、例外をスローするなどして対処する必要があります.<br/>
	 *
	 * @param clientCommon 更新しようとしているリソースアイテムの共通データ
	 * @param clientItem 更新しようとしているリソースアイテム
	 * @param serverCommon サーバで保持しているリソースアイテムの共通データ
	 * @param serverItem サーバで保持しているリソースアイテム
	 * @return 競合解決の結果となるSyncActionStatus
	 */
	public SyncAction resolveConflict(ResourceItemCommonData clientCommon, Object clientItem,
			ResourceItemCommonData serverCommon, Object serverItem);
}
