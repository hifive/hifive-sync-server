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
package com.htmlhifive.sync.resource.update;

import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;

/**
 * リソースアイテムの競合解決ロジッククラスのインターフェース.<br>
 *
 * @author kishigam
 */
public interface UpdateStrategy {

	/**
	 * 競合しているリソースアイテムから戦略に従って解決を行います.<br>
	 * 競合が解決できた場合、更新を行うリソースアイテムを返します.<br>
	 * 競合が解決できない場合、ItemUpdatedExceptionをスローします.
	 *
	 * @param itemCommon 更新しようとしているリソースアイテムの共通データ
	 * @param item 更新しようとしているリソースアイテム
	 * @param serverCommon サーバで保持しているリソースアイテムの共通データ
	 * @param serverItem サーバで保持しているリソースアイテム
	 * @return 競合解決済リソースアイテム
	 * @throws ItemUpdatedException 競合が解決できない場合
	 */
	public <T> T resolveConflict(ResourceItemCommonData itemCommon, T item, ResourceItemCommonData serverCommon,
			T serverItem) throws ItemUpdatedException;
}
