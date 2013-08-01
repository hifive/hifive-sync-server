/*
 * Copyright (C) 2012-2013 NS Solutions Corporation
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
package com.htmlhifive.resourcefw.exception;

import com.htmlhifive.resourcefw.ctrl.ResourceExceptionHandler;

/**
 * フレームワーク標準の実行時例外.<br>
 * フレームワーク例外({@link AbstractResourceException AbstractResourceException})をラップし、{@link ResourceExceptionHandler
 * ResourceExceptionHandler}にハンドリングさせるためにも使用します.<br>
 *
 * @author kishigam
 */
public class GenericResourceException extends RuntimeException {

	private static final long serialVersionUID = 7860339653427945186L;

	/**
	 * @see RuntimeException
	 */
	public GenericResourceException() {
		super();
	}

	/**
	 * @see RuntimeException
	 */
	public GenericResourceException(String message) {
		super(message);
	}

	/**
	 * @see RuntimeException
	 */
	public GenericResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see RuntimeException
	 */
	public GenericResourceException(Throwable cause) {
		super(cause);
	}

	/**
	 * @see RuntimeException
	 */
	protected GenericResourceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
