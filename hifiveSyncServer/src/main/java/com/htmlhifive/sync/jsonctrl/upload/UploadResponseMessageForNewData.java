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
package com.htmlhifive.sync.jsonctrl.upload;

import com.htmlhifive.sync.jsonctrl.JsonDataConvertor;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * 新規登録データの同期結果を表現する上り更新レスポンスメッセージのデータクラス.<br>
 *
 * @author kishigam
 */
public class UploadResponseMessageForNewData extends UploadResponseMessage {

	/**
	 * 新規登録データに対してクライアントで設定されていたストレージローカルID.
	 */
	private String storageLocalId;

	/**
	 * 新規登録データの同期結果を表現する上り更新レスポンスメッセージを生成します.
	 *
	 * @param response 同期レスポンスオブジェクト
	 */
	public UploadResponseMessageForNewData(SyncResponse<?> response) {

		super(response.getCommon().getSyncDataId(), JsonDataConvertor.convertSyncMethodToAction(response.getCommon()
				.getSyncMethod()));

		//        this.storageLocalId = response.getCommon().getStorageLocalId();
	}

	/**
	 * @return storageLocalId
	 */
	public String getStorageLocalId() {
		return storageLocalId;
	}

	/**
	 * @param storageLocalId セットする storageLocalId
	 */
	public void setStorageLocalId(String storageLocalId) {
		this.storageLocalId = storageLocalId;
	}
}