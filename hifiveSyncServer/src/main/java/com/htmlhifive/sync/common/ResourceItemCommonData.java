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
package com.htmlhifive.sync.common;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.htmlhifive.sync.resource.SyncAction;
import com.htmlhifive.sync.resource.SyncConflictType;

/**
 * リソースを同期するために必要な共通データを管理するエンティティ.<br>
 * リソースアイテムごとに1つの共通データが生成されます.
 *
 * @author kishigam
 */
@Entity
@Table(name = "RESOURCE_ITEM_COMMON_DATA")
public class ResourceItemCommonData {

	/**
	 * IDオブジェクト.<br>
	 * このオブジェクトのプロパティを展開して使用するため、getterメソッドに{@link JsonUnwrapped}を付加しています.
	 */
	@EmbeddedId
	private ResourceItemCommonDataId id;

	/**
	 * この共通データが対象とするリソースアイテムのID.<br>
	 * このフィールドはクライアントと通信するリクエスト、レスポンスに含みません.<br>
	 * リクエスト、レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonIgnore}を追加しています.
	 */
	private String targetItemId;

	/**
	 * このリソースアイテムを現在の状態に登録したアクション.<br>
	 */
	@Enumerated(EnumType.STRING)
	private SyncAction action;

	/**
	 * このリソースアイテムのロックがリクエスト処理後も継続することを示すフラグ.<br>
	 * このフィールドはクライアントへのレスポンスに含みません.<br>
	 * レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonSerialize}を追加しています.<br>
	 * また、永続化の対象外です.
	 */
	@Transient
	private boolean keepLock;

	/**
	 * このリソースアイテムの競合状態.<br>
	 * このフィールドはクライアントと通信するリクエスト、レスポンスに含みません.<br>
	 * リクエスト、レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonIgnore}を追加しています.
	 */
	@Transient
	private SyncConflictType conflictType;

	/**
	 * リソースアイテムの最終更新時刻(ミリ秒)
	 */
	private long lastModified;

	/**
	 * フレームワーク、ライブラリが使用するプライベートデフォルトコンストラクタ.
	 */
	@SuppressWarnings("unused")
	private ResourceItemCommonData() {
	}

	/**
	 * IDオブジェクト、対象リソースアイテムのIDを指定して共通データを生成します.
	 *
	 * @param syncDataId 同期データID
	 * @param requestHeader リソースへのリクエストヘッダ
	 * @param targetResourceIdStr リソースID文字列
	 */
	public ResourceItemCommonData(ResourceItemCommonDataId id, String targetItemId) {

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
		if (!(obj instanceof ResourceItemCommonData))
			return false;

		return EqualsBuilder.reflectionEquals(this, ((ResourceItemCommonData) obj));
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
	 * @param action 更新アクション
	 * @param uploadTime 更新時刻
	 */
	public void modify(SyncAction actionToModify, long uploadTime) {

		this.action = actionToModify;
		this.lastModified = uploadTime;
	}

	//	/**
	//	 * このオブジェクトの内容を設定してリソースアイテムのラッパーオブジェクトを生成します.
	//	 */
	//	public ResourceItemWrapper generateItemWrapper() {
	//
	//		ResourceItemWrapper wrapper = new ResourceItemWrapper(this.id.getResourceItemId());
	//		wrapper.setAction(this.action);
	//		wrapper.setLastModified(lastModified);
	//
	//		return wrapper;
	//	}

	/**
	 * このオブジェクトのプロパティを展開して使用するため、@JsonUnwrappedを付加しています.
	 *
	 * @return id
	 */
	@JsonUnwrapped
	public ResourceItemCommonDataId getId() {
		return id;
	}

	/**
	 * リクエスト、レスポンスから除外するため、{@link JsonIgnore}を追加しています.
	 *
	 * @return targetItemId
	 */
	@JsonIgnore
	public String getTargetItemId() {
		return targetItemId;
	}

	/**
	 * @param targetItemId セットする targetItemId
	 */
	public void setTargetItemId(String targetItemId) {
		this.targetItemId = targetItemId;
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
	 * レスポンスから除外するため、{@link @JsonSerialize}を追加しています.
	 *
	 * @return keepLock
	 */
	@JsonSerialize(include = Inclusion.NON_EMPTY)
	public boolean isKeepLock() {
		return keepLock;
	}

	/**
	 * @param keepLock セットする keepLock
	 */
	public void setKeepLock(boolean keepLock) {
		this.keepLock = keepLock;
	}

	/**
	 * リクエスト、レスポンスから除外するため、{@link JsonIgnore}を追加しています.
	 *
	 * @return conflictType
	 */
	@JsonIgnore
	public SyncConflictType getConflictType() {
		return conflictType;
	}

	/**
	 * @param conflictType セットする conflictType
	 */
	public void setConflictType(SyncConflictType conflictType) {
		this.conflictType = conflictType;
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
}
