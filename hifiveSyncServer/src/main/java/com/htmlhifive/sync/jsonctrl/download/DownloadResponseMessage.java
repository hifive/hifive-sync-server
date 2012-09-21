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
package com.htmlhifive.sync.jsonctrl.download;

import com.htmlhifive.sync.jsonctrl.JsonDataConvertor;
import com.htmlhifive.sync.jsonctrl.SyncAction;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * 上り更新、および下り更新結果におけるリソースアイテム1件の情報を保持するデータ.<br>
 * サーバ上の1つのリソースアイテムに関する共通情報、およびリソースアイテム本体を保持します.
 *
 * @param <E> リソースアイテムの型
 * @author kishigam
 */
public class DownloadResponseMessage<E> {

	/**
	 * リソースアイテムID.
	 */
	private String resourceItemId;

	/**
	 * リソースアイテムの最終更新アクション.
	 */
	private SyncAction action;

	/**
	 * このデータの最終更新時刻
	 */
	private long lastModified;

	/**
	 * このデータが表現するリソースエレメントの本体.
	 */
	private E element;

	/**
	 * 同期レスポンスオブジェクトからクライアントに返す下り更新レスポンスを生成します.
	 *
	 * @param response 同期レスポンスオブジェクト
	 */
	public DownloadResponseMessage(SyncResponse<E> response) {

		action = JsonDataConvertor.convertSyncMethodToAction(response.getCommon().getSyncMethod());
		resourceItemId = response.getCommon().getSyncDataId();
		lastModified = response.getCommon().getLastModified();
		element = response.getElement();
	}

	/**
	 * @return syncDataId
	 */
	public String getSyncDataId() {
		return resourceItemId;
	}

	/**
	 * @param syncDataId セットする syncDataId
	 */
	public void setSyncDataId(String syncDataId) {
		this.resourceItemId = syncDataId;
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
}
