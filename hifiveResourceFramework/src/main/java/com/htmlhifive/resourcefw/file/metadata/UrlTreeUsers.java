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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * urlTreeノードのオーナー、アクセスユーザーとなるユーザーを表すEntity.
 *
 * @author kawaguch
 */
@Entity
public class UrlTreeUsers {

	/**
	 * 主キーオブジェクト
	 */
	private UrlTreeUsersPrimaryKey primaryKey;

	/**
	 * 主グループを表すフラグ.
	 */
	private boolean isPrimaryGroup;

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof UrlTreeUsers))
			return false;

		UrlTreeUsers other = (UrlTreeUsers) obj;

		return other.primaryKey.equals(this.primaryKey);
	}

	@Override
	public int hashCode() {
		return primaryKey.hashCode();
	}

	@Override
	public String toString() {
		return new StringBuilder().append(primaryKey).append(isPrimaryGroup ? " : primary group" : "").toString();
	}

	@EmbeddedId
	/**
	 * @return primaryKey
	 */
	public UrlTreeUsersPrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey セットする primaryKey
	 */
	public void setPrimaryKey(UrlTreeUsersPrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return isPrimaryGroup
	 */
	public boolean isPrimaryGroup() {
		return isPrimaryGroup;
	}

	/**
	 * @param isPrimaryGroup セットする isPrimaryGroup
	 */
	public void setPrimaryGroup(boolean isPrimaryGroup) {
		this.isPrimaryGroup = isPrimaryGroup;
	}
}
