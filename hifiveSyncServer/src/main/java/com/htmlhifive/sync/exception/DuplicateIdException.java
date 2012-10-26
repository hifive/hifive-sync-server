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

/**
 * リソースの生成時、すでに対象リソースアイテムが存在する場合にスローされる例外.<br>
 * サーバ側で管理しているリソースアイテムとその対象リソースアイテムのIDを保持しており、 クライアントに返す情報の生成に使用することができます.
 *
 * @author kishigam
 */
public class DuplicateIdException extends Exception {

	private static final long serialVersionUID = -1708837543557342515L;

	/**
	 * 競合対象のサーバ側対象リソースアイテムのID.
	 */
	private String duplicatedTargetItemId;

	/**
	 * 競合対象のサーバ側リソースアイテム.
	 */
	private Object currentItem;

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param duplicateTargetItemId 対象リソースアイテムのID
	 * @param duplicateItem リソースアイテム
	 * @see Exception
	 */
	public DuplicateIdException(String duplicateTargetItemId, Object duplicateItem) {

		super();
		this.duplicatedTargetItemId = duplicateTargetItemId;
		this.currentItem = duplicateItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 * @param duplicateItem リソースアイテム
	 * @see Exception
	 */
	public DuplicateIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			String duplicateTargetItemId, Object duplicateItem) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.duplicatedTargetItemId = duplicateTargetItemId;
		this.currentItem = duplicateItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param duplicateItem リソースアイテム
	 * @see Exception
	 */
	public DuplicateIdException(String message, Throwable cause, String duplicateTargetItemId, Object duplicateItem) {
		super(message, cause);
		this.duplicatedTargetItemId = duplicateTargetItemId;
		this.currentItem = duplicateItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param duplicateItem リソースアイテム
	 * @see Exception
	 */
	public DuplicateIdException(String message, String duplicateTargetItemId, Object duplicateItem) {
		super(message);
		this.duplicatedTargetItemId = duplicateTargetItemId;
		this.currentItem = duplicateItem;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param cause
	 * @param duplicateItem リソースアイテム
	 * @see Exception
	 */
	public DuplicateIdException(Throwable cause, String duplicateTargetItemId, Object duplicateItem) {
		super(cause);
		this.duplicatedTargetItemId = duplicateTargetItemId;
		this.currentItem = duplicateItem;
	}

	/**
	 * この例外が保持する、競合対象のサーバ側リソースアイテムを返します.
	 *
	 * @return リソースアイテム
	 */
	public Object getCurrentItem() {
		return currentItem;
	}

	/**
	 * この例外が保持する、競合対象のサーバ側対象リソースアイテムのIDを返します.
	 *
	 * @return 対象リソースアイテムのID
	 */
	public String getDuplicatedTargetItemId() {
		return duplicatedTargetItemId;
	}
}
