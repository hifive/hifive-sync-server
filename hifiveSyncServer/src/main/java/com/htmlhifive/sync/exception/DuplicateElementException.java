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
 * リソースの生成時、すでに対象リソースエレメントが存在する場合にスローされる例外.<br>
 * サーバ側で管理しているリソースエレメントとそのリソースID文字列を保持しており、 クライアントに返す情報の生成に使用することができます.
 *
 * @author kishigam
 */
public class DuplicateElementException extends Exception {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = -1708837543557342515L;

	/**
	 * 競合対象のサーバ側リソースID文字列.
	 */
	private String duplicateResourceIdStr;

	/**
	 * 競合対象のサーバ側リソースエレメント.
	 */
	private Object duplicateElement;

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param duplicateResourceIdStr リソースID文字列
	 * @param duplicateElement リソースエレメント
	 * @see RuntimeException
	 */
	public DuplicateElementException(String duplicateResourceIdStr, Object duplicateElement) {

		super();
		this.duplicateResourceIdStr = duplicateResourceIdStr;
		this.duplicateElement = duplicateElement;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 * @param duplicateElement リソースエレメント
	 * @see RuntimeException
	 */
	public DuplicateElementException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, String duplicateResourceIdStr, Object duplicateElement) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.duplicateResourceIdStr = duplicateResourceIdStr;
		this.duplicateElement = duplicateElement;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param duplicateElement リソースエレメント
	 * @see RuntimeException
	 */
	public DuplicateElementException(String message, Throwable cause, String duplicateResourceIdStr,
			Object duplicateElement) {
		super(message, cause);
		this.duplicateResourceIdStr = duplicateResourceIdStr;
		this.duplicateElement = duplicateElement;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param duplicateElement リソースエレメント
	 * @see RuntimeException
	 */
	public DuplicateElementException(String message, String duplicateResourceIdStr, Object duplicateElement) {
		super(message);
		this.duplicateResourceIdStr = duplicateResourceIdStr;
		this.duplicateElement = duplicateElement;
	}

	/**
	 * 競合対象のサーバ側リソースの情報を指定して、例外オブジェクトを生成します.
	 *
	 * @param cause
	 * @param duplicateElement リソースエレメント
	 * @see RuntimeException
	 */
	public DuplicateElementException(Throwable cause, String duplicateResourceIdStr, Object duplicateElement) {
		super(cause);
		this.duplicateResourceIdStr = duplicateResourceIdStr;
		this.duplicateElement = duplicateElement;
	}

	/**
	 * この例外が保持する、競合対象のサーバ側リソースエレメントを返します.
	 *
	 * @return リソースエレメント
	 */
	@SuppressWarnings("unchecked")
	public <E> E getDuplicateElement() {
		return (E) duplicateElement;
	}

	/**
	 * この例外が保持する、競合対象のサーバ側リソースID文字列を返します.
	 *
	 * @return リソースID文字列
	 */
	public String getDuplicateResourceIdStr() {
		return duplicateResourceIdStr;
	}
}
