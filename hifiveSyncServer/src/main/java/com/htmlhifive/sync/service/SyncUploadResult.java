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
package com.htmlhifive.sync.service;

import java.util.Date;

/**
 * クライアントからの上り更新リクエストに対する同期結果を保持するデータオブジェクト.
 *
 * @author kishigam
 */
public class SyncUploadResult extends AbstractSyncResult {
	/**
	 * ストレージIDを指定して同期結果オブジェクトを生成します.
	 *
	 * @param storageId クライアントのストレージID
	 */
	public SyncUploadResult(String storageId) {
		super(storageId);
	}

	/**
	 * 上り更新リクエストに対してクライアントに返す実行時刻を導出し、設定します.<br>
	 * 上り更新においては、このメソッドが実行された時点のシステム時刻が設定されます.
	 */
	@Override
	public void setGeneratedSyncTime() {

		setCurrentSyncTime(new Date().getTime());
	}
}
