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
package com.htmlhifive.sync.service;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * syncリクエストに関する共通情報を保持するデータクラス(エンティティ).<br/>
 *
 * @author kishigam
 */
@Entity
@Table(name = "SYNC_REQUEST_COMMON_DATA")
public class SyncRequestCommonData implements Serializable {

	private static final long serialVersionUID = 1545406007910362417L;

	/**
	 * クライアントのストレージID.
	 */
	@Id
	private String storageId;

	/**
	 * 前回上り更新時刻.<br/>
	 * 上り更新リクエストの時に設定する必要があります.
	 */
	private Long lastUploadTime = null;

	/**
	 * この更新リクエストが実行される時刻.<br>
	 * 永続化の対象外です.
	 */
	@Transient
	private Long syncTime = null;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof SyncRequestCommonData))
			return false;

		SyncRequestCommonData uploadCommon = (SyncRequestCommonData) obj;

		return new EqualsBuilder().append(this.storageId, uploadCommon.storageId)
				.append(this.lastUploadTime, uploadCommon.lastUploadTime).append(this.syncTime, uploadCommon.syncTime)
				.isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.storageId).append(this.lastUploadTime).append(this.syncTime)
				.hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * この共通データが前回上り更新時刻を保持しているかどうかを判定します.<br>
	 * 時刻"0"は、保持していると判定されます.
	 *
	 * @return 前回上り更新時刻を保持しているときtrue
	 */
	public boolean hasLastUploadTime() {
		return this.lastUploadTime != null;
	}

	/**
	 * この共通データが保持している前回上り更新時刻が、指定された共通データの前回上り更新時刻よりも後のときtrueを返します.
	 *
	 * @param uploadCommonData 上り更新共通データ
	 * @return 前回上り更新時刻が後の場合true
	 */
	public boolean isLaterUploadThan(SyncRequestCommonData uploadCommonData) {
		return this.lastUploadTime > uploadCommonData.getLastUploadTime();
	}

	/**
	 * @return the storageId
	 */
	public String getStorageId() {
		return storageId;
	}

	/**
	 * @param storageId the storageId to set
	 */
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	/**
	 * @return the lastUploadTime
	 */
	public Long getLastUploadTime() {
		return lastUploadTime;
	}

	/**
	 * @param lastUploadTime the lastUploadTime to set
	 */
	public void setLastUploadTime(Long lastUploadTime) {
		this.lastUploadTime = lastUploadTime;
	}

	/**
	 * @return the syncTime
	 */
	public Long getSyncTime() {
		return syncTime;
	}

	/**
	 * @param syncTime the syncTime to set
	 */
	public void setSyncTime(Long syncTime) {
		this.syncTime = syncTime;
	}
}
