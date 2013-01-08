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
package com.htmlhifive.sync.exception;

import com.htmlhifive.sync.resource.SyncConflictType;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;

/**
 * リソースの更新時、すでに対象リソースアイテムが他のクライアントに更新され、解決できない場合にスローされる例外.<br>
 * サーバ側で管理しているリソースアイテムとその対象リソースアイテム共通データを保持しており、 クライアントに返す情報の生成に使用することができます.
 *
 * @author kishigam
 */
public class ItemUpdatedException extends Exception {

	private static final long serialVersionUID = -3612660489368099360L;

	/**
	 * 競合対象のサーバ側対象リソースアイテム共通データ.
	 */
	private ResourceItemCommonData conflictedCommonData;

	/**
	 * 競合対象のサーバ側リソースアイテム.
	 */
	private Object currentItem;

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @see Exception
	 */
	public ItemUpdatedException(ResourceItemCommonData conflictedCommonData, Object currentItem) {

		super();
		this.conflictedCommonData = conflictedCommonData;
		this.conflictedCommonData.setConflictType(SyncConflictType.UPDATED);
		this.currentItem = currentItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @see Exception
	 */
	public ItemUpdatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			ResourceItemCommonData conflictedCommonData, Object currentItem) {

		super(message, cause, enableSuppression, writableStackTrace);
		this.conflictedCommonData = conflictedCommonData;
		this.conflictedCommonData.setConflictType(SyncConflictType.UPDATED);
		this.currentItem = currentItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @see Exception
	 */
	public ItemUpdatedException(String message, Throwable cause, ResourceItemCommonData conflictedCommonData,
			Object currentItem) {
		super(message, cause);
		this.conflictedCommonData = conflictedCommonData;
		this.conflictedCommonData.setConflictType(SyncConflictType.UPDATED);
		this.currentItem = currentItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @see Exception
	 */
	public ItemUpdatedException(String message, ResourceItemCommonData conflictedCommonData, Object currentItem) {
		super(message);
		this.conflictedCommonData = conflictedCommonData;
		this.conflictedCommonData.setConflictType(SyncConflictType.UPDATED);
		this.currentItem = currentItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @see Exception
	 */
	public ItemUpdatedException(Throwable cause, ResourceItemCommonData conflictedCommonData, Object currentItem) {
		super(cause);
		this.conflictedCommonData = conflictedCommonData;
		this.conflictedCommonData.setConflictType(SyncConflictType.UPDATED);
		this.currentItem = currentItem;
	}

	/**
	 * @return conflictedCommonData
	 */
	public ResourceItemCommonData getConflictedCommonData() {
		return conflictedCommonData;
	}

	/**
	 * @return currentItem
	 */
	public Object getCurrentItem() {
		return currentItem;
	}
}
