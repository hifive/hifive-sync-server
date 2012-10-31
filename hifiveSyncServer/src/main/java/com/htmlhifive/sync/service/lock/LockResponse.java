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
package com.htmlhifive.sync.service.lock;

import java.util.List;
import java.util.Map;

import com.htmlhifive.sync.resource.ResourceItemWrapper;

/**
 * クライアントからのロック取得リクエストに対するレスポンスデータクラス.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Deprecated
public class LockResponse {

	/**
	 * ロック取得共通データ.<br>
	 * このレスポンスにおいてクライアントへ返す情報を保持しています.
	 */
	private LockCommonData lockCommonData;

	/**
	 * ロック取得結果のリソースアイテムリスト. <br>
	 * リソース別にリストを保持します.
	 */
	private Map<String, List<ResourceItemWrapper<?>>> resourceItems;

	/**
	 * このロック取得共通データを持つレスポンスデータを生成します.
	 *
	 * @param responseCommon ロック取得共通データ
	 */
	public LockResponse(LockCommonData lockCommonData) {

		this.lockCommonData = lockCommonData;
	}

	/**
	 * @return lockCommonData
	 */
	public LockCommonData getLockCommonData() {
		return lockCommonData;
	}

	/**
	 * @param lockCommonData セットする lockCommonData
	 */
	public void setLockCommonData(LockCommonData lockCommonData) {
		this.lockCommonData = lockCommonData;
	}

	/**
	 * @return resourceItems
	 */
	public Map<String, List<ResourceItemWrapper<?>>> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(Map<String, List<ResourceItemWrapper<?>>> resourceItems) {
		this.resourceItems = resourceItems;
	}
}
