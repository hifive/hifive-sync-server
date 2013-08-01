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
package com.htmlhifive.resourcefw.file;

import org.springframework.beans.BeanUtils;

import com.htmlhifive.resourcefw.file.metadata.UrlTreeDTO;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeDirectory;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeEntry;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeMetaDataManager;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeNode;
import com.htmlhifive.resourcefw.file.metadata.UrlTreeNodePrimaryKey;

/**
 * UrlTreeリソースで使用するユーティリティクラス.
 *
 * @author kawaguch
 */
public class UrlTreeUtil {

	/** パスの区切り文字 */
	private static final String PATH_SEPARATOR = UrlTreeMetaDataManager.PATH_SEPARATOR;

	/** ルートエントリの名前 */
	private static final String ROOT_NAME = UrlTreeMetaDataManager.ROOT_NAME;

	private UrlTreeUtil() {
		// Do nothing.
	}

	/**
	 * ノードを表すオブジェクトをUrlTreeDTOに変換します.
	 *
	 * @param targetNode 対象となるUrlTreeNoodeオブジェクト
	 * @return UrlTreeDTOオブジェクト
	 */
	public static UrlTreeDTO convertToDto(UrlTreeNode targetNode) {

		if (targetNode == null) {
			return null;
		}

		UrlTreeDTO result = new UrlTreeDTO();
		BeanUtils.copyProperties(targetNode, result);

		if (targetNode instanceof UrlTreeDirectory) {
			result.setDirectory(true);
		} else if (targetNode instanceof UrlTreeEntry) {
			result.setDirectory(false);
			result.setValue(((UrlTreeEntry) targetNode).getValue());
		}

		//result.setParent(result.getParent().replaceFirst(ROOT_NAME, ""));

		return result;
	}

	/**
	 * パス文字列の正規化を行います.<br/>
	 * <ul>
	 * <li>空文字はルートを表します
	 * <li>パスセパレータ１文字はルートを表します
	 * <li>2個以上並んだセパレータは１個にまとめます
	 * <li>最初は必ずルート名で始まります
	 * <li>最後のパスセパレータは取り除きます
	 * </ul>
	 *
	 * @param path パス
	 * @return 正規化したパス文字列
	 */
	public static String normalizePath(String path) {

		if (path == null) {
			// 変にrootを返されてもおかしな動作になるので
			return null;
		} else if (path.isEmpty()) {
			return ROOT_NAME;
		}

		String normalizedPath = path;
		// パスセパレータの取り扱い
		// - ２個以上並んでいる奴は１個にまとめる
		// - 最初は必ずROOT_NAMEで始まる
		// - 最後はセパレータはなし
		// - パスセパレータ１文字＝ルートを表す
		normalizedPath = normalizedPath.replaceAll(PATH_SEPARATOR + "{2,}", PATH_SEPARATOR);

		if (normalizedPath.equals(PATH_SEPARATOR)) {
			return ROOT_NAME;
		}

		if (!normalizedPath.startsWith(PATH_SEPARATOR)) {
			normalizedPath = PATH_SEPARATOR + normalizedPath;
		}

		if (normalizedPath.endsWith(PATH_SEPARATOR)) {
			normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
		}

		return normalizedPath;
	}

	/**
	 * パス文字列からUrlTreeNodePrimaryKeyを生成します.<br/>
	 * 内部でパスを正規化しているので、事前の正規化処理は不要です.
	 *
	 * @param paramPath パス
	 * @return 対象のパスに対応するUrlTreeNodePrimaryKey
	 */
	public static UrlTreeNodePrimaryKey generateUrlTreeNodePrimaryKey(String paramPath) {

		if (paramPath == null) {
			throw new IllegalArgumentException("Designated PATH is null");
		}

		String normPath = UrlTreeUtil.normalizePath(paramPath);

		UrlTreeNodePrimaryKey utnp = new UrlTreeNodePrimaryKey();
		if (normPath.equals(ROOT_NAME)) {
			utnp.setName(ROOT_NAME);
			utnp.setParent(" ");
			return utnp;
		}

		int lastSeparator = normPath.lastIndexOf(PATH_SEPARATOR);
		String parentPath = normPath.substring(0, lastSeparator);
		String childPath = normPath.substring(lastSeparator + PATH_SEPARATOR.length());

		utnp.setParent(parentPath);
		utnp.setName(childPath);

		if (utnp.getParent().isEmpty()) {
			utnp.setParent(ROOT_NAME);
		}

		return utnp;
	}

	/**
	 * 指定されたパス(文字列)から親となるパス文字列を返します.
	 *
	 * @param paramPath パス(URL)
	 * @return 親となるパスの文字列表現
	 */
	public static String getParentName(String paramPath) {
		UrlTreeNodePrimaryKey utnpk = generateUrlTreeNodePrimaryKey(paramPath);
		return utnpk.getParent();
	}

	/**
	 * 指定されたパスにおけるchildで指定された情報を取得します.
	 *
	 * @param parent パスURL
	 * @param child 配下のパス(ディレクトリ)
	 * @return childのパス文字列
	 */
	public static String getChildPath(String parent, String child) {
		if (child == null || child.isEmpty() || child.contains(PATH_SEPARATOR)) {
			throw new IllegalArgumentException("Illegal child name ");
		}

		String normPath = normalizePath(parent);

		return normPath + PATH_SEPARATOR + child;
	}
}
