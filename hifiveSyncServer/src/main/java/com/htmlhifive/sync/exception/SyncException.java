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
 * 同期の各処理において発生する基底実行時例外.
 *
 * @author kishigam
 */
public class SyncException extends RuntimeException {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = 4598815832929604945L;

	/**
	 * @see RuntimeException
	 */
	public SyncException() {
		super();
	}

	/**
	 * @see RuntimeException
	 */
	public SyncException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @see RuntimeException
	 */
	public SyncException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see RuntimeException
	 */
	public SyncException(String message) {
		super(message);
	}

	/**
	 * @see RuntimeException
	 */
	public SyncException(Throwable cause) {
		super(cause);

	}
}
