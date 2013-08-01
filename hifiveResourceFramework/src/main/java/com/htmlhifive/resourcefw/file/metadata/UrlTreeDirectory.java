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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Transient;

/**
 * urlTreeリソースアイテムにおけるディレクトリノードを表現するEntity.
 *
 * @author kawaguch
 */
@IdClass(UrlTreeNodePrimaryKey.class)
@Entity
public class UrlTreeDirectory extends UrlTreeNode {

	private static final long serialVersionUID = 6953554984292038000L;

	/** 子ノードマップ */
	@Transient
	private Map<String, UrlTreeNode> children;

	/**
	 * 子ノードを保持するMapを生成してディレクトリノードを生成します.
	 */
	public UrlTreeDirectory() {
		super();
		children = new HashMap<String, UrlTreeNode>();
	}

	/**
	 * このノードの識別子(キー情報)と親ノードのキー情報を指定してディレクトリノードを生成します.
	 *
	 * @param key このインスタンスの識別子
	 * @param parent 親ノード
	 */
	public UrlTreeDirectory(String key, String parent) {
		super(key, parent);
		children = new HashMap<String, UrlTreeNode>();
	}

	/**
	 * 指定レイヤー以下のキー情報一覧を文字列として返します.
	 *
	 * @param layer レイヤー
	 */
	public String toString(int layer) {
		StringBuilder sb = new StringBuilder();
		String tab = repeatStr("\t", layer);
		sb.append(tab).append(this.getName()).append("\n");

		for (UrlTreeNode n : children.values()) {
			sb.append(n.toString(layer + 1));
		}

		return sb.toString();
	}

	/**
	 * 文字列表現を返します.
	 */
	public String toString() {
		return this.toString(0);
	}
}
