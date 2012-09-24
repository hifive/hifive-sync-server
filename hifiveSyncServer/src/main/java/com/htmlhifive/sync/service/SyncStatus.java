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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.SyncResultType;

/**
 * 各クライアントからの同期状態に関する情報保持するエンティティ.<br>
 *
 * @author kishigam
 */
@Entity
@Table(name = "SYNC_STATUS")
public class SyncStatus {

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
	 * 前回下り更新時刻.
	 */
	@Transient
	private long lastDownloadTime;

	/**
	 * 更新結果タイプ.<br>
	 */
	@Transient
	private SyncResultType resultType;

	/**
	 * 同期データのリソースアイテムリスト.<br>
	 * 下り更新時に同期時点のサーバデータを保持します.<br>
	 * 上り更新ではデータは保持されません.
	 */
	@Transient
	private List<ResourceItemsContainer> resourceItems;

	/**
	 * プライベートのデフォルトコンストラクタ. <br>
	 * 永続マネージャーが使用するため、実装する必要があります.
	 */
	@SuppressWarnings("unused")
	private SyncStatus() {
	}

	/**
	 * ストレージIDを使用して、新規エンティティインスタンスを生成します.
	 *
	 * @param storageId クライアントのストレージID
	 */
	public SyncStatus(String storageId) {
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
	 * 指定された前回上り更新時刻が、この同期ステータスが保持している前回上り更新時刻よりも前のとき、二重送信と判断し、trueを返します.
	 *
	 * @param clientLastUploadTime クライアントごとの前回上り更新時刻
	 * @return 前回上り更新時刻より前であればtrue
	 */
	public boolean isNewerStatusThanClient(long clientLastUploadTime) {

		return this.lastUploadTime > clientLastUploadTime;
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

	/**
	 * @return resultType
	 */
	public SyncResultType getResultType() {
		return resultType;
	}

	/**
	 * @param resultType セットする resultType
	 */
	public void setResultType(SyncResultType resultType) {
		this.resultType = resultType;
	}

	/**
	 * @return resourceItems
	 */
	public List<ResourceItemsContainer> getResourceItems() {
		return resourceItems;
	}

	/**
	 * @param resourceItems セットする resourceItems
	 */
	public void setResourceItems(List<ResourceItemsContainer> resourceItems) {
		this.resourceItems = resourceItems;
	}
}
