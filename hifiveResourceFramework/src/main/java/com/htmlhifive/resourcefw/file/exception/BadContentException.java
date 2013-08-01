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
package com.htmlhifive.resourcefw.file.exception;

/**
 * ストレージ上のファイルの状態に不具合があり、ロードできない場合にスローされる例外.
 *
 * @author kishigam
 */
public class BadContentException extends UrlTreeSystemException {

	private static final long serialVersionUID = -7063647127393728706L;

	public BadContentException() {
		super();
	}

	public BadContentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BadContentException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadContentException(String message) {
		super(message);
	}

	public BadContentException(Throwable cause) {
		super(cause);
	}

}
