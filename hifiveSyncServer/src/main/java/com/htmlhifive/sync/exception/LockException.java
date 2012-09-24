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
 * リソースに対するリクエストが、必要なロック処理が行われていないために失敗した場合にスローされる例外.<br>
 *
 * @author kishigam
 */
public class LockException extends Exception {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = 1621213380727697249L;

	/**
	 * @see RuntimeException
	 */
	public LockException() {
		super();
	}

	/**
	 * @see RuntimeException
	 */
	public LockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @see RuntimeException
	 */
	public LockException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see RuntimeException
	 */
	public LockException(String message) {
		super(message);
	}

	/**
	 * @see RuntimeException
	 */
	public LockException(Throwable cause) {
		super(cause);
	}
}
