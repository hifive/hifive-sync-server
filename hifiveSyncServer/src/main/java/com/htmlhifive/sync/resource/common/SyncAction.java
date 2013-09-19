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
package com.htmlhifive.sync.resource.common;

import java.util.EnumSet;

/**
 * 同期アクションの結果を表す列挙型.
 *
 * @author kishigam
 */
public enum SyncAction {

	/**
	 * リソースアイテムに対するアクションは行われなかった
	 */
	NONE,

	/**
	 * 新規リソースアイテムが生成された
	 */
	CREATE,

	/**
	 * 既存リソースアイテムが更新された
	 */
	UPDATE,

	/**
	 * 既存リソースアイテムが削除された
	 */
	DELETE,

	/**
	 * リソースアイテムが重複した
	 */
	DUPLICATE,

	/**
	 * リソースアイテムの更新(削除)が競合した
	 */
	CONFLICT;

	/**
	 * 使用可能なSyncActionを表す文字列であればtrueを返します.
	 *
	 * @param syncActionStr syncActionを表す文字列
	 * @return SyncActionであればtrue
	 */
	public static boolean isSyncAction(String syncActionStr) {

		if (syncActionStr == null)
			return false;

		for (SyncAction syncAction : EnumSet.allOf(SyncAction.class)) {
			if (syncAction.toString().equals(syncActionStr))
				return true;
		}

		return false;
	}

}
