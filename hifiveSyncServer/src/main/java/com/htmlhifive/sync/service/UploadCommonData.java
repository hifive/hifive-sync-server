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
package com.htmlhifive.sync.service;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.htmlhifive.sync.resource.SyncConflictType;

/**
 * 上り更新に関する共通情報を保持するデータクラス(エンティティ).
 *
 * @author kishigam
 */
@Entity
@Table(name = "UPLOAD_COMMON_DATA")
public class UploadCommonData {

	/**
	 * クライアントのストレージID.<br>
	 * このフィールドはクライアントへのレスポンスに含みません.<br>
	 * レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonIgnore}を追加しています)
	 */
	@Id
	private String storageId;

	/**
	 * 最終上り更新時刻.<br>
	 * このフィールドは競合がない場合のみクライアントへのレスポンスに含みます.<br>
	 * nullのときレスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonInclude}を追加しています)
	 */
	private long lastUploadTime;

	/**
	 * 競合発生時の同期結果タイプ.<br>
	 * このフィールドは競合発生時のみクライアントへのレスポンスに含みます.<br>
	 * nullのときレスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonInclude}を追加しています).<br>
	 * また、永続化の対象外です.
	 */
	@Transient
	private SyncConflictType conflictType;

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
	 * この共通データが保持している最終上り更新時刻が、指定された上り更新共通データの最終上り更新時刻よりも後のときtrueを返します.
	 *
	 * @param uploadCommonData 上り更新共通データ
	 * @return 最終上り更新時刻が後の場合true
	 */
	public boolean isLaterUploadThan(UploadCommonData uploadCommonData) {
		return this.lastUploadTime > uploadCommonData.getLastUploadTime();
	}

	/**
	 * クライアントへのレスポンスから除外するため、{@link JsonIgnore}を追加しています)
	 *
	 * @return storageId
	 */
	@JsonIgnore
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
	 * nullのときクライアントへのレスポンスから除外するため、{@link JsonInclude}を追加しています).
	 *
	 * @return lastUploadTime
	 */
	@JsonInclude(Include.NON_NULL)
	public long getLastUploadTime() {
		return lastUploadTime;
	}

	/**
	 * @param lastUploadTime セットする lastUploadTime
	 */
	public void setLastUploadTime(long lastUploadTime) {
		this.lastUploadTime = lastUploadTime;
	}

	/**
	 * nullのときクライアントへのレスポンスから除外するため、{@link JsonInclude}を追加しています).
	 *
	 * @return conflictType
	 */
	@JsonInclude(Include.NON_NULL)
	public SyncConflictType getConflictType() {
		return conflictType;
	}

	/**
	 * @param conflictType セットする conflictType
	 */
	public void setConflictType(SyncConflictType conflictType) {
		this.conflictType = conflictType;
	}
}
