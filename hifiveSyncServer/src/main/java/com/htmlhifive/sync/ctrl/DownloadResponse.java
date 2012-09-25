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
package com.htmlhifive.sync.ctrl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.service.SyncStatus;

/**
 * クライアントからの下り更新リクエストに対するレスポンスデータクラス.<br>
 * このクラスが保持するリソースアイテムリスト内には、リソースの重複およびリソースアイテムの重複はありません.
 *
 * @author kishigam
 */
public class DownloadResponse {

	/**
	 * レスポンスに含むストレージID.<br>
	 * このフィールドは初回下り更新のときのみレスポンスに含みます.<br>
	 * これを実現するため、このフィールドのgetterメソッドに{@link JsonSerialize}を追加しています)
	 */
	private String storageId;

	/**
	 * 下り更新処理を実行した時刻.
	 */
	private long lastDownloadTime;

	/**
	 * 下り更新結果のリソースアイテムリスト. <br>
	 * リソース別にリストを保持します.
	 */
	private Map<String, List<ResourceItemWrapper>> resourceItems;

	/**
	 * 同期ステータスオブジェクトから下り更新レスポンスを生成します.
	 *
	 * @param statusAfterDownload 同期ステータスオブジェクト
	 */
	public DownloadResponse(SyncStatus statusAfterDownload) {
		this.lastDownloadTime = statusAfterDownload.getLastDownloadTime();
		this.resourceItems = statusAfterDownload.getResourceItems();
	}

	/**
	 * 同期ステータスオブジェクト、およびストレージIDから下り更新レスポンスを生成します.<br>
	 *
	 * @param storageId ストレージID(初回下り更新時)
	 * @param statusAfterDownload 同期ステータスオブジェクト
	 */
	public DownloadResponse(String storageId, SyncStatus statusAfterDownload) {

		this(statusAfterDownload);
		this.storageId = storageId;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof DownloadResponse))
			return false;

		DownloadResponse req = (DownloadResponse) obj;

		return new EqualsBuilder().append(this.lastDownloadTime, req.lastDownloadTime)
				.append(this.resourceItems, req.resourceItems).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.lastDownloadTime).append(this.resourceItems).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * nullでない場合のみレスポンスに含むため、{@link JsonSerialize}を設定しています.
	 *
	 * @return storageId
	 */
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getStorageId() {
		return storageId;
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
	public Map<String, List<ResourceItemWrapper>> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(Map<String, List<ResourceItemWrapper>> resourceItems) {
		this.resourceItems = resourceItems;
	}
}