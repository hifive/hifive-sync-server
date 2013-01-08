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
package com.htmlhifive.sync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 同期の対象となるリソースが存在しない場合にスローされる例外.<br>
 * この例外がスローされた場合、ステータスコード404のHTTPレスポンスが返されることを想定しています.
 *
 * @author kishigam
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such content")
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = -1751829506458796410L;

	/**
	 * @see RuntimeException
	 */
	public NotFoundException() {
		super();
	}

	/**
	 * @see RuntimeException
	 */
	public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @see RuntimeException
	 */
	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see RuntimeException
	 */
	public NotFoundException(String message) {
		super(message);
	}

	/**
	 * @see RuntimeException
	 */
	public NotFoundException(Throwable cause) {
		super(cause);
	}

}
