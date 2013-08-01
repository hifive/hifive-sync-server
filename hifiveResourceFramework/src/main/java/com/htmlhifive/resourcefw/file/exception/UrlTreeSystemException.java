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
 * urlTreeリソースで発生する例外の汎用基底クラス.
 *
 * @author kawaguch
 */
public abstract class UrlTreeSystemException extends Exception {

	private static final long serialVersionUID = -4924400705948928035L;

	public UrlTreeSystemException() {
		super();
	}

	public UrlTreeSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UrlTreeSystemException(String message, Throwable cause) {
		super(message, cause);
	}

	public UrlTreeSystemException(String message) {
		super(message);
	}

	public UrlTreeSystemException(Throwable cause) {
		super(cause);
	}
}
