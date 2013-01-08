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
package com.htmlhifive.sync.service.upload;

import com.htmlhifive.sync.service.Synchronizer;

/**
 * {@link Synchronizer}における上り更新の同期制御方式を表す列挙型.
 *
 * @author kishigam
 */
public enum UploadControlType {

	/**
	 * 上り更新対象のリソースアイテムに対して「予約」を行い、更新を実行します.<br>
	 * 具体的には、"for update"操作によるロックを実行します.
	 */
	RESERVE,

	/**
	 * 上り更新対象のリソースアイテムの更新実行順をソートにより変更します.<br>
	 * 更新順が一定になるためデッドロックが回避できます.<br>
	 * 更新実行順を変更可能なアプリケーションのみ使用できます.
	 */
	AVOID_DEADLOCK,

	/**
	 * 上り更新において同期制御を行いません.
	 */
	NONE,
}
