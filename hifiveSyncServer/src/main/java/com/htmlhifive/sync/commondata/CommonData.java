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
package com.htmlhifive.sync.commondata;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.SyncMethod;
import com.htmlhifive.sync.resource.SyncRequestHeader;

/**
 * リソースを同期するために必要な共通データを管理するエンティティ.<br>
 * リソースエレメントごとに1つの共通データが生成されます.
 *
 * @author kishigam
 */
@Entity
@Table(name = "COMMON_DATA")
public class CommonData {

	/**
	 * このエンティティのID(同期データID).
	 */
	@Id
	private String syncDataId;

	/**
	 * クライアント(ストレージ)のID.<br>
	 */
	private String storageId;

	/**
	 * リソースのデータモデル名.<br>
	 */
	private String dataModelName;

	/**
	 * リソースエレメントを識別するためのID.<br>
	 */
	private String resourceIdStr;

	/**
	 * リソースエレメントの登録形態.<br>
	 */
	private SyncMethod syncMethod;

	/**
	 * リソースエレメントの最終更新時刻(ミリ秒)
	 */
	private long lastModified;

	/**
	 * 悲観ロック時のロックキー.
	 */
	private String lockKey;

	/**
	 * 共通データエンティティを生成します.
	 */
	public CommonData() {
	}

	/**
	 * 同期データIDを指定して共通データエンティティを生成します.
	 *
	 * @param syncDataId 同期データID
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @param targetResourceIdStr リソースID文字列
	 */
	public CommonData(String syncDataId, SyncRequestHeader requestHeader, String resourceIdStr) {

		this.syncDataId = syncDataId;
		this.dataModelName = requestHeader.getDataModelName();
		this.storageId = requestHeader.getStorageId();
		this.syncMethod = requestHeader.getSyncMethod();
		this.lastModified = requestHeader.getRequestTime();
		this.resourceIdStr = resourceIdStr;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof CommonData))
			return false;

		return EqualsBuilder.reflectionEquals(this, ((CommonData) obj), "syncDataId");
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return HashCodeBuilder.reflectionHashCode(this, "syncDataId");
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * リソースエレメントが更新された場合など、その登録形態と更新時刻を更新します.
	 *
	 * @param requestHeader 同期リクエストのヘッダ
	 */
	public void modifiy(SyncRequestHeader requestHeader) {

		this.syncMethod = requestHeader.getSyncMethod();
		this.lastModified = requestHeader.getRequestTime();
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
	 * @return resourceIdStr
	 */
	public String getResourceIdStr() {
		return resourceIdStr;
	}

	/**
	 * @param resourceIdStr セットする resourceIdStr
	 */
	public void setResourceIdStr(String resourceIdStr) {
		this.resourceIdStr = resourceIdStr;
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
	 * @return lockKey
	 */
	public String getLockKey() {
		return lockKey;
	}

	/**
	 * @param lockKey セットする lockKey
	 */
	public void setLockKey(String lockKey) {
		this.lockKey = lockKey;
	}

}
