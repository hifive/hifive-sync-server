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

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * リソースへの同期リクエストに対するレスポンスに必要な共通的データを保持するヘッダクラス.<br>
 * TODO: 上り(GET以外共通または個別)/下り(GET)の分割
 *
 * @author kishigam
 */
public class SyncResponseHeader {

	/**
	 * リクエスト発行元クライアントのストレージID.
	 */
	private String storageId;

	/**
	 * リクエストの同期メソッド.
	 */
	private SyncMethod syncMethod;

	/**
	 * サーバ側でリソースエレメントごとに管理するリソースID文字列.<br>
	 */
	private String resourceIdStr;

	/**
	 * データモデル名.<br>
	 */
	private String dataModelName;

	/**
	 * クエリMap. <br>
	 * 上り更新リクエストに対するレスポンスの場合のみ設定します.
	 */
	private Map<String, String[]> queryMap;

	/**
	 * 同期データID.<br>
	 */
	private String syncDataId;

	/**
	 * 最終更新時刻.<br>
	 */
	private long lastModified;

	/**
	 * クライアントで設定されたストレージローカルID.<br>
	 * 新規登録リクエストに対するレスポンスの場合のみ設定します.
	 */
	private String storageLocalId;

	/**
	 * リソースID文字列を指定してレスポンスヘッダオブジェクトを生成します.
	 *
	 * @param resourceIdStr このレスポンスが対象とするリソースのID文字列
	 */
	public SyncResponseHeader(String resourceIdStr) {

		this.resourceIdStr = resourceIdStr;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof SyncResponseHeader))
			return false;

		SyncResponseHeader header = (SyncResponseHeader) obj;

		return EqualsBuilder.reflectionEquals(this, header);
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
	 * @return storageId
	 */
	public String getStorageId() {
		return storageId;
	}

	/**
	 * @param storageId セットする storageId
	 */
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	/**
	 * @return syncMethod
	 */
	public SyncMethod getSyncMethod() {
		return syncMethod;
	}

	/**
	 * @param syncMethod セットする syncMethod
	 */
	public void setSyncMethod(SyncMethod syncMethod) {
		this.syncMethod = syncMethod;
	}

	/**
	 * @return resourceIdStr
	 */
	public String getResourceIdStr() {
		return resourceIdStr;
	}

	/**
	 * @return dataModelName
	 */
	public String getDataModelName() {
		return dataModelName;
	}

	/**
	 * @param dataModelName セットする dataModelName
	 */
	public void setDataModelName(String dataModelName) {
		this.dataModelName = dataModelName;
	}

	/**
	 * @return queryMap
	 */
	public Map<String, String[]> getQueryMap() {
		return queryMap;
	}

	/**
	 * @param queryMap セットする queryMap
	 */
	public void setQueryMap(Map<String, String[]> queryMap) {
		this.queryMap = queryMap;
	}

	/**
	 * @return syncDataId
	 */
	public String getSyncDataId() {
		return syncDataId;
	}

	/**
	 * @param syncDataId セットする syncDataId
	 */
	public void setSyncDataId(String syncDataId) {
		this.syncDataId = syncDataId;
	}

	/**
	 * @return lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified セットする lastModified
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return storageLocalId
	 */
	public String getStorageLocalId() {
		return storageLocalId;
	}

	/**
	 * @param storageLocalId セットする storageLocalId
	 */
	public void setStorageLocalId(String storageLocalId) {
		this.storageLocalId = storageLocalId;
	}
}
