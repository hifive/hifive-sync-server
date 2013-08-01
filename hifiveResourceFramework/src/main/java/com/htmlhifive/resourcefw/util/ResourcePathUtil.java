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
package com.htmlhifive.resourcefw.util;

/**
 * リソースアイテムを識別するためのパスを操作するユーティリティ.
 *
 * @author kishigam
 */
public class ResourcePathUtil {

	private ResourcePathUtil() {
	}

	/**
	 * パス区切り文字.
	 */
	public static final String RESOURCE_PATH_SEPARATOR = "/";

	/**
	 * 指定されたパス文字列であらわされる階層を一つ降り、元の階層を示す名称と残りのパス文字列を配列で返します.<br/>
	 *
	 * @param path パス文字列
	 * @return 元の階層を示す名前と残りのパス文字列
	 */
	public static String[] down(String path) {
		// 先頭がセパレータであれば除去
		if (path.startsWith(RESOURCE_PATH_SEPARATOR)) {
			path = path.substring(1);
		}

		String name = path.split(RESOURCE_PATH_SEPARATOR)[0];
		String remainingPath = path.replaceFirst(name, "");

		// 先頭がセパレータであれば除去
		if (remainingPath.startsWith(RESOURCE_PATH_SEPARATOR)) {
			remainingPath = remainingPath.substring(1);
		}

		remainingPath = remainingPath.endsWith(RESOURCE_PATH_SEPARATOR) ? remainingPath.substring(0,
				remainingPath.length() - 1) : remainingPath;

		return new String[] { name, remainingPath };
	}
}
