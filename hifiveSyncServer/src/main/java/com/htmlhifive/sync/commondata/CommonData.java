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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceItemWrapper;
import com.htmlhifive.sync.resource.SyncAction;

/**
 * リソースを同期するために必要な共通データを管理するエンティティ.<br>
 * リソースアイテムごとに1つの共通データが生成されます.
 *
 * @author kishigam
 */
@Entity
@Table(name = "COMMON_DATA")
public class CommonData {

	/**
	 * IDオブジェクト.<br>
	 */
	@EmbeddedId
	private CommonDataId id;

	/**
	 * この共通データが対象とするリソースアイテムのID.<br>
	 */
	private String targetItemId;

	/**
	 * クライアント(ストレージ)のID.<br>
	 */
	private String storageId;

	/**
	 * このリソースアイテムを現在の状態に登録したアクション.<br>
	 */
	private SyncAction action;

	/**
	 * リソースアイテムの最終更新時刻(ミリ秒)
	 */
	private long lastModified;

	/**
	 * 悲観ロック時のロックキー(将来の実装のため).
	 */
	private String lockKey;

	/**
	 * IDオブジェクト、対象リソースアイテムのIDを指定して共通データを生成します.
	 *
	 * @param syncDataId 同期データID
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @param targetResourceIdStr リソースID文字列
	 */
	public CommonData(CommonDataId id, String targetItemId) {

		this.id = id;
		this.targetItemId = targetItemId;
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

		return EqualsBuilder.reflectionEquals(this, ((CommonData) obj));
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
	 * リソースアイテムの更新内容をこのオブジェクトに反映します.
	 *
	 * @param itemWrapper リソースアイテムのラッパー
	 */
	public void modifiy(ResourceItemWrapper itemWrapper) {

		this.action = itemWrapper.getAction();
		this.lastModified = itemWrapper.getUploadTime();
	}

	/**
	 * このオブジェクトの内容を設定してリソースアイテムのラッパーオブジェクトを生成します.
	 */
	public ResourceItemWrapper generateItemWrapper() {

		ResourceItemWrapper wrapper = new ResourceItemWrapper(this.id.getResourceItemId());
		wrapper.setAction(this.action);
		wrapper.setLastModified(lastModified);

		return wrapper;
	}

	/**
	 * @return id
	 */
	public CommonDataId getId() {
		return id;
	}

	/**
	 * @return targetItemId
	 */
	public String getTargetItemId() {
		return targetItemId;
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
	 * @return action
	 */
	public SyncAction getAction() {
		return action;
	}

	/**
	 * @param action セットする action
	 */
	public void setAction(SyncAction action) {
		this.action = action;
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
