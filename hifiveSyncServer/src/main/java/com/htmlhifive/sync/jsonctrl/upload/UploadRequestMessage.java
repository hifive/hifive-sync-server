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
import com.htmlhifive.sync.jsonctrl.SyncAction;
import com.htmlhifive.sync.resource.SyncRequestHeader;

/**
 * JSON形式の上り更新リクエストで更新するリソースエレメントを表現するメッセージのデータクラス.
 *
 * @author kishigam
 * @param <E> リソースエレメントの型
 */
public class UploadRequestMessage<E> {

	/**
	 * データモデル名
	 */
	private String dataModelName;

	/**
	 * このデータが生成された契機となった同期アクション.
	 */
	private SyncAction action;

	/**
	 * このデータが表現するリソースエレメントの本体.
	 */
	private E element;

	/**
	 * 同期データID.
	 */
	private String syncDataId;

	/**
	 * このデータの最終更新時刻
	 */
	private long lastModified;

	/**
	 * このメッセージの内容をもとにリソースへのリクエストのためのリクエストヘッダを生成します.<br>
	 *
	 * @param storageId クライアントのストレージID
	 * @param requestTime 同期実行時刻
	 * @return 同期リクエストヘッダ
	 */
	public SyncRequestHeader createtHeader(String storageId, long requestTime) {

		SyncRequestHeader header = new SyncRequestHeader(JsonDataConvertor.convertActionToSyncMethod(action),
				storageId, requestTime);

		header.setStorageId(storageId);
		header.setDataModelName(dataModelName);
		header.setSyncDataId(syncDataId);
		header.setLastModified(lastModified);
		header.setRequestTime(requestTime);

		return header;
	}

	/**
	 * @return dataModelName
	 */
	public String getDataModelName() {
		return dataModelName;
	}

	/**
	 * @param dataModelName セットする dataModelName
	 */
	public void setDataModelName(String dataModelName) {
		this.dataModelName = dataModelName;
	}

	/**
	 * @return action
	 */
	public SyncAction getAction() {
		return action;
	}

	/**
	 * @param action セットする action
	 */
	public void setAction(SyncAction action) {
		this.action = action;
	}

	/**
	 * @return element
	 */
	public E getElement() {
		return element;
	}

	/**
	 * @param element セットする element
	 */
	public void setElement(E element) {
		this.element = element;
	}

	/**
	 * @return syncDataId
	 */
	public String getSyncDataId() {
		return syncDataId;
	}

	/**
	 * @param syncDataId セットする syncDataId
	 */
	public void setSyncDataId(String syncDataId) {
		this.syncDataId = syncDataId;
	}

	/**
	 * @return lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified セットする lastModified
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}
