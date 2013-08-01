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
package com.htmlhifive.sync.resource.common;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonUnwrapped;

/**
 * リソースアイテムを同期するために必要な共通データを管理するエンティティ.<br>
 * リソースアイテムごとに1つの共通データが生成されます.
 *
 * @author kishigam
 */
@Entity
@Table(name = "RESOURCE_ITEM_COMMON_DATA")
public class ResourceItemCommonData implements Serializable, Comparable<ResourceItemCommonData> {

	private static final long serialVersionUID = -9200797333301417852L;

	/**
	 * IDオブジェクト.<br>
	 */
	@JsonUnwrapped
	@EmbeddedId
	private ResourceItemCommonDataId id;

	/**
	 * この共通データが対象とするリソースアイテムのID.<br>
	 */
	private String targetItemId;

	/**
	 * このリソースアイテムを現在の状態に登録したアクション.<br>
	 */
	@Enumerated(EnumType.STRING)
	private SyncAction syncAction;

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
	 * @param id 同期共通データID
	 */
	public ResourceItemCommonData(ResourceItemCommonDataId id) {

		this.id = id;
	}

	/**
	 * 共通データIDの順序で比較します.
	 */
	@Override
	public int compareTo(ResourceItemCommonData o) {

		return this.id.compareTo(o.getId());
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

		ResourceItemCommonData common = (ResourceItemCommonData) obj;

		// 一時的な状態を保持するフィールド以外を比較
		return new EqualsBuilder().append(this.id, common.id).append(this.targetItemId, common.targetItemId)
				.append(this.syncAction, common.syncAction).append(this.lastModified, common.lastModified).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(id).append(this.targetItemId).append(this.syncAction)
				.append(this.lastModified).hashCode();
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
	 * @param syncAction 更新アクション
	 * @param uploadTime 更新時刻
	 */
	public void modify(SyncAction syncAction, long uploadTime) {

		this.syncAction = syncAction;
		this.lastModified = uploadTime;
	}

	/**
	 * @return the id
	 */
	public ResourceItemCommonDataId getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ResourceItemCommonDataId id) {
		this.id = id;
	}

	/**
	 * @return the targetItemId
	 */
	public String getTargetItemId() {
		return targetItemId;
	}

	/**
	 * @param targetItemId the targetItemId to set
	 */
	public void setTargetItemId(String targetItemId) {
		this.targetItemId = targetItemId;
	}

	/**
	 * @return the syncAction
	 */
	public SyncAction getSyncAction() {
		return syncAction;
	}

	/**
	 * @param syncAction the syncAction to set
	 */
	public void setSyncAction(SyncAction syncAction) {
		this.syncAction = syncAction;
	}

	/**
	 * @return the lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}
