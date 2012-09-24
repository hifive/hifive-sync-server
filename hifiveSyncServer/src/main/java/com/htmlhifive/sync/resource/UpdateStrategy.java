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

import com.htmlhifive.sync.exception.ItemUpdatedException;

/**
 * 競合発生時の更新可否を判断する戦略オブジェクトのインターフェース.<br>
 *
 * @author kishigam
 */
public interface UpdateStrategy {

	/**
	 * 競合している更新前後のリソースアイテムから戦略に従って解決を行います.<br>
	 * 競合が解決できた場合、更新するリソースアイテムを返します.<br>
	 * 競合が解決できない場合、ItemUpdatedExceptionをスローします.
	 *
	 * @param clientItemWrapper クライアントのリソースアイテムのラッパー
	 * @param serverItemWrapper サーバのリソースアイテムのラッパー
	 * @param resourceItemType リソースアイテムの型
	 */
	public <T> T resolveConflict(ResourceItemWrapper clientItemWrapper, ResourceItemWrapper serverItemWrapper,
			Class<T> resourceItemType) throws ItemUpdatedException;
}
