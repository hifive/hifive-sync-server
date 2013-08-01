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

/**
 * @author kawaguch
 */
public class UrlTreeUsersPrimaryKey implements Serializable {

	private static final long serialVersionUID = -6458859102415368864L;

	/**
	 * ユーザーID
	 */
	private String userId;

	/**
	 * グループID
	 */
	private String groupId;

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof UrlTreeUsersPrimaryKey)) {
			return false;
		}

		UrlTreeUsersPrimaryKey other = (UrlTreeUsersPrimaryKey) obj;
		return userId.equals(other.getUserId()) && groupId.equals(other.getGroupId());
	}

	@Override
	public int hashCode() {
		return userId.hashCode() + 13 * groupId.hashCode();
	}

	@Override
	public String toString() {
		return userId + ":" + groupId;
	}

	/**
	 * @return userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId セットする userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
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
}
