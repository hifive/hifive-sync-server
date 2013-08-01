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

/**
 * urlTreeリソースで扱うファイルデータの認可権限（パーミッション）を示す列挙型.
 *
 * @author kawaguch
 */
public enum AccessMode {
	READ("r"), WRITE("w"), EXECUTE("x");

	/**
	 * 内部での管理値
	 */
	private String flag;

	private AccessMode(String str) {
		this.flag = str;
	}

	public String getFlag() {
		return flag;
	}
}
