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

import javax.persistence.Entity;
import javax.persistence.IdClass;

/**
 * urlTreeリソースのエントリを表現するEntity.
 *
 * @author kawaguch
 */
@IdClass(UrlTreeNodePrimaryKey.class)
@Entity
public class UrlTreeEntry extends UrlTreeNode {

	private static final long serialVersionUID = 9043426769039416987L;

	/** エントリが保持する値 */
	private String value;

	/**
	 * urlTreeのエントリを生成します.
	 */
	public UrlTreeEntry() {
		super();
	}

	/**
	 * キー情報、対応する値(URL形式の文字列)、親となるディレクトリを指定してurlTreeエントリを生成します.
	 *
	 * @param key
	 * @param value
	 * @param parent
	 */
	public UrlTreeEntry(String key, String value, String parent) {
		super(key, parent);
		this.value = value;
	}

	/**
	 * 指定レイヤー以下の識別子と値の一覧の文字列表現を取得します.
	 *
	 * @param layer レイヤー
	 */
	public String toString(int layer) {
		StringBuilder sb = new StringBuilder();
		String tab = repeatStr("\t", layer);

		if (!value.toString().contains("\n")) {
			sb.append(tab).append(this.getName()).append(" = ").append(value.toString());
		} else {
			sb.append(tab).append(this.getName());
		}
		sb.append("\n");
		return sb.toString();
	}

	public String toString() {
		return this.toString(0);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
