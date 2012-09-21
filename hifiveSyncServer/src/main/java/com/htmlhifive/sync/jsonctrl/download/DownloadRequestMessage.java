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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.SyncMethod;
import com.htmlhifive.sync.resource.SyncRequestHeader;

/**
 * JSON形式の下り更新リクエストで同期対象を表現するメッセージデータクラス.
 *
 * @author kishigam
 */
public class DownloadRequestMessage {

	/**
	 * リソース名.
	 */
	private String resourceName;

	/**
	 * リソースごとにダウンロード対象アイテムを絞り込むクエリ情報. <br>
	 * 対称データとクエリ条件(配列形式)のMapで表現されます.
	 */
	private Map<String, String[]> query = new HashMap<>();

	/**
	 * 前回ダウンロード時刻.<br>
	 * この時刻以降のデータを取得するリクエストとなる.
	 */
	private long lastDownloadTime = 0L;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof DownloadRequestMessage))
			return false;

		DownloadRequestMessage msg = (DownloadRequestMessage) obj;

		return EqualsBuilder.reflectionEquals(this, msg);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * このメッセージの内容をもとにリソースへのGETりクエストのためのリクエストヘッダを生成します.<br>
	 *
	 * @param storageId クライアントのストレージID
	 * @param requestTime 同期実行時刻
	 * @return 同期リクエストヘッダ
	 */
	public SyncRequestHeader createHeader(String storageId, long requestTime) {

		SyncRequestHeader requestHeader = new SyncRequestHeader(SyncMethod.GET, storageId, requestTime);
		requestHeader.setResourceName(resourceName);
		requestHeader.setQuery(query);
		requestHeader.setLastSyncTime(lastDownloadTime);

		return requestHeader;
	}

	/**
	 * @return resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceName セットする resourceName
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return query
	 */
	public Map<String, String[]> getQuery() {
		return query;
	}

	/**
	 * @param query セットする query
	 */
	public void setQuery(Map<String, String[]> query) {
		this.query = query;
	}

	/**
	 * @return lastDownloadTime
	 */
	public long getLastDownloadTime() {
		return lastDownloadTime;
	}

	/**
	 * @param lastDownloadTime セットする lastDownloadTime
	 */
	public void setLastDownloadTime(long lastDownloadTime) {
		this.lastDownloadTime = lastDownloadTime;
	}
}
