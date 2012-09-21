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

/**
 * リソースのロック方式を制御するロジックを規定したインタフェース.
 *
 * @author kawaguch
 */
public interface LockManager {

	/**
	 * ロックを取得します.<br>
	 * 同期リクエストのヘッダオブジェクトと、同期対象をGETして得られたレスポンスヘッダオブジェクトの内容の比較によりロック取得の可否を判定します.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param commonBeforeUpdate 同期レスポンスヘッダ
	 * @return ロックを取得できた場合true.
	 */
	public boolean lock(SyncRequestHeader requestHeader, CommonData commonBeforeUpdate);

	/**
	 * ロック取得状況に応じて、リソースの更新が実行できるか判定します.<br>
	 * 同期リクエストのヘッダオブジェクトと、同期対象をGETして得られたレスポンスヘッダオブジェクトの内容を判定に使用します.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param commonBeforUpdate 同期レスポンスヘッダ
	 * @return update(/delete)できる場合true.
	 */
	public boolean canUpdate(SyncRequestHeader requestHeader, CommonData commonBeforUpdate);

	/**
	 * ロックを解除します.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param commonBeforUpdate 同期レスポンスヘッダ
	 */
	public void release(SyncRequestHeader requestHeader, CommonData commonBeforUpdate);
}
