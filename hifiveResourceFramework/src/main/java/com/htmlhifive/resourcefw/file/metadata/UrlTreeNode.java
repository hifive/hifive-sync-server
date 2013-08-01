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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * urlTreeリソースのノードを表現するEntity.<br/>
 * urlTreeリソース上の要素は全てこのクラスのインスタンスで表現されます.
 *
 * @author kawaguch
 */
@IdClass(UrlTreeNodePrimaryKey.class)
@Entity
public abstract class UrlTreeNode implements Serializable {

	private static final long serialVersionUID = 7447416461451576171L;

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

	/** パーミッション */
	private String permission;

	/** ロックトークン. */
	private String lockToken;

	/** ロックの有効期限. */
	private long lockExpiredTime = -1;

	/** 作成日時 */
	private long createdTime = -1;
	/** 更新日時 */
	private long updatedTime = -1;
	/** アクセス日時 */
	private long accessedTime = -1;

	/** 論理削除フラグ */
	private boolean deleted = false;

	/**
	 * urlTreeノードを生成します.
	 */
	public UrlTreeNode() {
	}

	/**
	 * キー情報と親となるノードのキーを指定してurlTreeノードを生成します.
	 *
	 * @param key このノードのキー情報
	 * @param parent 親となるノードのキー
	 */
	public UrlTreeNode(String key, String parent) {
		this.name = key;
		this.parent = parent;
	}

	/**
	 * 指定レイヤー以下の識別子-値の一覧の文字列表現
	 *
	 * @param layer レイヤー
	 * @return 識別子-値の一覧の文字列表現
	 */
	public abstract String toString(int layer);

	/**
	 * @return parent
	 */
	@Id
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
	@Id
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
	 * @return isDeleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param isDeleted セットする isDeleted
	 */
	public void setDeleted(boolean isDeleted) {
		this.deleted = isDeleted;
	}

	/**
	 * 指定された文字列を指定回数連結した文字列を返します.
	 *
	 * @param str 文字列
	 * @param times 連結回数
	 * @return 連結後の文字列
	 */
	protected String repeatStr(String str, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++) {
			sb.append(str);
		}

		return sb.toString();
	}
}
