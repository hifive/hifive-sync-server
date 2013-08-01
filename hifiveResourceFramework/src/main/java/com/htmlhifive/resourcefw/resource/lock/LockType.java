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
package com.htmlhifive.resourcefw.resource.lock;

/**
 * リソースのロック種類を表す列挙型.<br>
 *
 * @author kishigam
 */
public enum LockType {

	/**
	 * 共有ロック.<br/>
	 * ロック取得に使用したロックトークンを持たない書き込みをロックします.
	 */
	SHARED,

	/**
	 * 排他ロック.<br/>
	 * ロック取得に使用したロックトークンを持たない読み込みをロックします.
	 */
	EXCLUSIVE,
}
