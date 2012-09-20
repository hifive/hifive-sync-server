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
package com.htmlhifive.sync.status;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 各クライアントの前回上り更新処理に関するステータスを保持するエンティティ.<br>
 *
 * @author kishigam
 */
@Entity
@Table(name = "LAST_UPLOAD_STATUS")
public class LastUploadStatus {

	/**
	 * クライアントのストレージID.<br>
	 */
	@Id
	private String storageId;

	/**
	 * 前回上り更新時刻.
	 */
	private long lastUploadTime;

	/**
	 * プライベートのデフォルトコンストラクタ. <br>
	 * 永続マネージャーが使用するため、実装する必要があります.
	 */
	@SuppressWarnings("unused")
	private LastUploadStatus() {
	}

	/**
	 * ストレージIDを使用して、新規エンティティインスタンスを生成します.
	 *
	 * @param storageId クライアントのストレージID
	 */
	public LastUploadStatus(String storageId) {
		this.storageId = storageId;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		return EqualsBuilder.reflectionEquals(this, obj);
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
	 * 指定された時刻が、保持している前回上り更新時刻より前であればtrueを返します.
	 *
	 * @param currentSyncTime
	 * @return 前回上り更新時刻より前であればtrue
	 */
	public boolean isPassed(long currentSyncTime) {

		return currentSyncTime <= this.lastUploadTime;
	}

	/**
	 * @return storageId
	 */
	public String getStorageId() {
		return storageId;
	}

	/**
	 * @return lastUploadTime
	 */
	public long getLastUploadTime() {
		return lastUploadTime;
	}

	/**
	 * @param lastUploadTime セットする lastUploadTime
	 */
	public void setLastUploadTime(long lastUploadTime) {
		this.lastUploadTime = lastUploadTime;
	}
}
