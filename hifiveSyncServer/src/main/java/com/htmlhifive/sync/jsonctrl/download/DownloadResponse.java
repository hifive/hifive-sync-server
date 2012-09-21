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
package com.htmlhifive.sync.jsonctrl.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.htmlhifive.sync.jsonctrl.ResponseBody;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * クライアントからの下り更新リクエストに対する結果データの抽象クラス.
 *
 * @author kishigam
 */
public abstract class DownloadResponse implements ResponseBody {

	/**
	 * 下り更新処理を実行した時刻.
	 */
	private long lastDownloadTime;

	/**
	 * 下り更新結果のリソースアイテムリスト(リソース名別ListのMap)
	 */
	private Map<String, List<? extends DownloadResponseMessage<?>>> resourceItems = new HashMap<>();

	/**
	 * 下り更新レスポンスを生成します.
	 *
	 * @param lastDownloadTime 同期時刻
	 */
	public DownloadResponse(long lastDownloadTime) {
		this.lastDownloadTime = lastDownloadTime;
	}

	/**
	 * 同期処理レスポンスから、クライアントに返す下り更新レスポンスメッセージを生成します.<br>
	 *
	 * @param responseSet 同期処理のレスポンスオブジェクトのセット
	 * @return 下り更新レスポンスメッセージのリスト
	 */
	protected static List<DownloadResponseMessage<?>> createResponseMessageList(Set<SyncResponse<?>> responseSet) {

		List<DownloadResponseMessage<?>> responseMessages = new ArrayList<>();

		for (SyncResponse<?> response : responseSet) {
			responseMessages.add(new DownloadResponseMessage<>(response));
		}

		return responseMessages;
	}

	/**
	 * @return syncTime
	 */
	public long getSyncTime() {
		return lastDownloadTime;
	}

	/**
	 * @param syncTime セットする syncTime
	 */
	public void setSyncTime(long syncTime) {
		this.lastDownloadTime = syncTime;
	}

	/**
	 * @return resourceItems
	 */
	public Map<String, List<? extends DownloadResponseMessage<?>>> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(Map<String, List<? extends DownloadResponseMessage<?>>> resourceItems) {
		this.resourceItems = resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void addResourceItems(String resourceName, List<? extends DownloadResponseMessage<?>> resourceItemList) {
		this.resourceItems.put(resourceName, resourceItemList);
	}
}