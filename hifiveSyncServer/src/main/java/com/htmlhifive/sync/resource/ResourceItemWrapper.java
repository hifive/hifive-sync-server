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

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * リソースアイテム1件の情報を保持するデータラッパー.<br>
 * サーバ上の1つのリソースアイテムに関する共通情報、およびリソースアイテムそのもののオブジェクトを保持します.
 *
 * @author kishigam
 */
public class ResourceItemWrapper {

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
	 * このデータが表現するリソースアイテムの本体.
	 */
	private Object item;

	/**
	 * リソースアイテムの同期結果.<br>
	 * OKを初期値とする.
	 */
	private SyncResultType resultType = SyncResultType.OK;

	/**
	 * IDを指定してリソースアイテムを生成します.
	 */
	public ResourceItemWrapper(String resourceItemId) {

		this.resourceItemId = resourceItemId;
	}

	/**
	 * @return resourceItemId
	 */
	public String getResourceItemId() {
		return resourceItemId;
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
	 * @return item
	 */
	public Object getItem() {
		return item;
	}

	/**
	 * @param item セットする item
	 */
	public void setItem(Object item) {
		this.item = item;
	}

	/**
	 * @return resultType
	 */
	@JsonIgnore
	public SyncResultType getResultType() {
		return resultType;
	}

	/**
	 * @param resultType セットする resultType
	 */
	public void setResultType(SyncResultType resultType) {
		this.resultType = resultType;
	}
}
