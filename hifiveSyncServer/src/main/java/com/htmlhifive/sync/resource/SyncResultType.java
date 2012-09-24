/*
 * Copyright (C) 2012 NS Solutions Corporation
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
package com.htmlhifive.sync.resource;

/**
 * 同期処理において、他のクライアントとの競合状態が発生したとき、その競合の種類に応じて設定されます.<br>
 *
 * @author kishigam
 */
public enum SyncResultType {

	/**
	 * 同期が成功したことを表します.
	 */
	OK,

	/**
	 * 一部のリソースアイテムに対して他のクライアントが更新を行っていたため、同期が失敗したことを表します.
	 */
	UPDATED,

	/**
	 * 他のクライアントが既に登録したリソースアイテムを二重に登録しようとしたため、同期が失敗したことを表します.
	 */
	DUPLICATEDID,

	/**
	 * ロックが取得できない、または必要なロックが取得されていないため、同期が失敗したことを表します.
	 */
	LOCKERROR,
}
