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
package com.htmlhifive.resourcefw.file.metadata;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * urlTreeノードの属性情報としてディレクトリ構成、認可関連パラメータを含めて保持するトランスファーオブジェクト.
 *
 * @author kawaguch
 */
public class UrlTreeDTO implements Serializable {

	private static final long serialVersionUID = -5719229985414561012L;

	// ディレクトリの構成に関する情報
	/** 親ノードのID */
	private String parent;

	/** 識別子 */
	private String name;

	// その他のメタデータ
	/** 所有者 */
	private String ownerId;

	/** グループ */
	private String groupId;

	/** パーミッション文字列 */
	private String permission;

	/** ロックトークン. */
	@JsonIgnore
	private String lockToken;

	/** ロック開始時間 */
	private long lockStartTime = -1;

	/** ロックの有効期限. */
	private long lockExpiredTime = -1;

	/** 作成日時 */
	private long createdTime = -1;
	/** 更新日時 */
	private long updatedTime = -1;
	/** アクセス日時 */
	private long accessedTime = -1;

	@JsonIgnore
	private List<UrlTreeDTO> childList;

	/** これがディレクトリかどうか */
	private boolean isDirectory;

	/**
	 * 識別子に対応する値.<br/>
	 * 具体的にはURL形式の持つ文字列です.
	 */
	@JsonIgnore
	private String value;

	/**
	 * @return parent
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * @param parent セットする parent
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	/**
	 * @return key
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param key セットする key
	 */
	public void setName(String key) {
		this.name = key;
	}

	/**
	 * @return ownerId
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * @param ownerId セットする ownerId
	 */
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * @return groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId セットする groupId
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return lockToken
	 */
	public String getLockToken() {
		return lockToken;
	}

	/**
	 * @param lockToken セットする lockToken
	 */
	public void setLockToken(String lockToken) {
		this.lockToken = lockToken;
	}

	/**
	 * @return lockExpiredTime
	 */
	public long getLockExpiredTime() {
		return lockExpiredTime;
	}

	/**
	 * @param lockExpiredTime セットする lockExpiredTime
	 */
	public void setLockExpiredTime(long lockExpiredTime) {
		this.lockExpiredTime = lockExpiredTime;
	}

	/**
	 * @return createdTime
	 */
	public long getCreatedTime() {
		return createdTime;
	}

	/**
	 * @param createdTime セットする createdTime
	 */
	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return updatedTime
	 */
	public long getUpdatedTime() {
		return updatedTime;
	}

	/**
	 * @param updatedTime セットする updatedTime
	 */
	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @return accessedTime
	 */
	public long getAccessedTime() {
		return accessedTime;
	}

	/**
	 * @param accessedTime セットする accessedTime
	 */
	public void setAccessedTime(long accessedTime) {
		this.accessedTime = accessedTime;
	}

	/**
	 * @return isDirectory
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * @param isDirectory セットする isDirectory
	 */
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	/**
	 * @return value
	 */
	@JsonIgnore
	public String getValue() {
		return value;
	}

	/**
	 * @param value セットする value
	 */
	@JsonIgnore
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return value
	 */
	public String getUrl() {
		return value;
	}

	/**
	 * @param value セットする value
	 */
	public void setUrl(String value) {
		this.value = value;
	}

	/**
	 * @return permission
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * @param permission セットする permission
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * @return childList
	 */
	public List<UrlTreeDTO> getChildList() {
		return childList;
	}

	/**
	 * @param childList セットする childList
	 */
	public void setChildList(List<UrlTreeDTO> childList) {
		this.childList = childList;
	}

	/**
	 * @return lockStartTime
	 */
	public long getLockStartTime() {
		return lockStartTime;
	}

	/**
	 * @param lockStartTime セットする lockStartTime
	 */
	public void setLockStartTime(long lockStartTime) {
		this.lockStartTime = lockStartTime;
	}

}
