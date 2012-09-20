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
package com.htmlhifive.sync.status;

/**
 * 同期ステータスを管理するサービスインターフェース.<br>
 * 二重送信制御など、同期処理のステータスを管理するためのインターフェースを規定します.
 *
 * @param <T>
 * @author kishigam
 */
public interface SyncStatusService<T> {

	/**
	 * ストレージIDで指定されたクライアントについて、保存している同期ステータスを取得します.
	 *
	 * @param storageId クライアントのストレージID
	 * @return ステータスオブジェクト
	 */
	T currentStatus(String storageId);

	/**
	 * 指定された同期ステータスを保存します.<br>
	 * まだストレージIDに対して保存している同期ステータスがない場合は、新規に保存します.
	 *
	 * @param status ステータスオブジェクト
	 */
	void updateStatus(T status);
}
