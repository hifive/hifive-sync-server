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
 * クライアントに返す上り更新結果のデータ型.<br>
 *
 * @author kishigam
 */
public class UploadResponseMessageOnConflict<E> extends UploadResponseMessage {

	/**
	 * 競合が発生したことを示すフラグ.
	 */
	private final boolean conflicted = true;

	/**
	 * データモデル名.
	 */
	private String dataModelName;

	/**
	 * 最終更新時刻.<br>
	 * 競合の原因となった更新が実行された時刻.
	 */
	private long lastModified;

	/**
	 * 競合原因となった更新によって登録されたリソースエレメント.
	 */
	private E element;

	/**
	 * 上り更新の競合を表現する上り更新レスポンスメッセージを生成します.
	 *
	 * @param response 同期レスポンスオブジェクト
	 */
	public UploadResponseMessageOnConflict(SyncResponse<E> response) {

		super(response.getCommon().getSyncDataId(), JsonDataConvertor.convertSyncMethodToAction(response.getCommon()
				.getSyncMethod()));

		this.dataModelName = response.getCommon().getDataModelName();
		this.lastModified = response.getCommon().getLastModified();

		this.element = response.getElement();
	}

	/**
	 * @return conflicted
	 */
	public boolean isConflicted() {
		return conflicted;
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