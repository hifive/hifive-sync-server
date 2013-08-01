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
package com.htmlhifive.resourcefw.file.auth;

import java.util.List;

/**
 * ユーザ情報を格納するコンテキストクラス
 *
 * @author kawaguch
 */
public class UrlTreeContext {

	/** 認証中のユーザ名 */
	private String userName = "nobody";

	/** 認証中のユーザのプライマリグループ */
	private String primaryGroup = "nogroup";

	/** グループの一覧 */
	private List<String> groups;

	/** ロックトークン */
	private String lockToken = "";

	/** デフォルトのパーミッション */
	private String defaultPermission = "rwx------";

	/**
	 * @return userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName セットする userName
	 */
	public void setUserName(String userName) {
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("cannot set to null or blank");
		}

		this.userName = userName;
	}

	/**
	 * @return primaryGroup
	 */
	public String getPrimaryGroup() {
		return primaryGroup;
	}

	/**
	 * @param primaryGroup セットする primaryGroup
	 */
	public void setPrimaryGroup(String primaryGroup) {
		this.primaryGroup = primaryGroup;
	}

	/**
	 * @return groups
	 */
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * @param groups セットする groups
	 */
	public void setGroups(List<String> groups) {
		if (groups == null) {
			throw new IllegalArgumentException("cannot set to null");
		}

		this.groups = groups;
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
		if (lockToken == null) {
			throw new IllegalArgumentException("cannot set to null");
		}

		this.lockToken = lockToken;
	}

	/**
	 * @return defaultPermission
	 */
	public String getDefaultPermission() {
		return defaultPermission;
	}

	/**
	 * @param defaultPermission セットする defaultPermission
	 */
	public void setDefaultPermission(String defaultPermission) {
		this.defaultPermission = defaultPermission;
	}

}
