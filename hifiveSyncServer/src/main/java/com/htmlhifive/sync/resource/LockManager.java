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

import com.htmlhifive.sync.commondata.CommonData;
import com.htmlhifive.sync.exception.LockException;

/**
 * リソースのロック方式を制御するロジックを規定したインタフェース.
 *
 * @author kawaguch
 */
public interface LockManager {

	/**
	 * ロックを取得します.<br>
	 * 共通データの状態からロック可否を判断し、可能であればロックします.
	 *
	 * @param commonData 共通データ
	 * @return ロックを取得できた場合true.
	 */
	public boolean lock(CommonData commonData) throws LockException;

	/**
	 * ロック取得状況に応じて、リソースの更新が実行できるか判定します.<br>
	 * 更新対象リソースアイテムの情報と、現在の共通データの情報を用いて判定を行い、更新できる場合trueを返します.
	 *
	 * @param updateItem 更新対象リソースアイテムのラッパー
	 * @param commonBeforUpdate 同期レスポンスヘッダ
	 * @return update(/delete)できる場合true.
	 */
	public boolean canUpdate(ResourceItemWrapper updateItem, CommonData commonBeforUpdate);

	/**
	 * ロックを解放します.
	 *
	 * @param commonData 共通データ
	 */
	public void release(CommonData commonData);
}
