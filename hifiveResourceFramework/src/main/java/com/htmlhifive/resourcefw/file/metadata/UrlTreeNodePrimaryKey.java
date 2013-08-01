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

import org.apache.commons.lang3.StringUtils;

import com.htmlhifive.resourcefw.file.UrlTreeUtil;

/**
 * urlTreeリソースにおけるノードの主キークラス.
 *
 * @author kawaguch
 */
public class UrlTreeNodePrimaryKey implements Serializable {

	private static final long serialVersionUID = 6339835309299986761L;

	/**
	 * キー情報
	 */
	private String key;

	/**
	 * 親ノード
	 */
	private String parent;

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof UrlTreeNodePrimaryKey)) {
			return false;
		}

		UrlTreeNodePrimaryKey other = (UrlTreeNodePrimaryKey) obj;
		return (parent.equals(other.parent) && key.equals(other.key));
	}

	@Override
	public int hashCode() {
		return parent.hashCode() + 5 * key.hashCode();
	}

	@Override
	public String toString() {
		if (StringUtils.isBlank(parent)) {
			return key;
		} else {
			return UrlTreeUtil.normalizePath(parent + UrlTreeMetaDataManager.PATH_SEPARATOR + key);
		}
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getName() {
		return key;
	}

	public void setName(String key) {
		this.key = key;
	}
}