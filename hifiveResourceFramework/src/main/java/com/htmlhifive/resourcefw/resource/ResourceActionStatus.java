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
package com.htmlhifive.resourcefw.resource;

import org.springframework.http.HttpStatus;

/**
 * リソースアクションの結果を表す列挙型.<br>
 * HTTP Statusに対応したステータスを表現しています.
 *
 * @author kishigam
 */
public enum ResourceActionStatus {

	OK(200), //
	CREATED(201), //
	SEE_OTHER(303), //
	NOT_MODIFIED(304), //
	TEMPORARY_REDIRECT(307), //
	BAD_REQUEST(400), //
	FORBIDDEN(403), //
	NOT_FOUND(404), //
	METHOD_NOT_ALLOWED(405), //
	NOT_ACCEPTABLE(406), //
	CONFLICT(409), //
	GONE(410), //
	UNSUPPORTED_MEDIA_TYPE(415), //
	LOCKED(423), //
	INTERNAL_SERVER_ERROR(500), //
	NOT_IMPLEMENTED(501), //
	SERVICE_UNAVAILABLE(503), ;

	/**
	 * ステータスコード
	 */
	private final int code;

	/**
	 * 対応するHttpStatusオブジェクト
	 */
	private final HttpStatus httpStatus;

	/**
	 * ステータスコードを指定してリソースアクションステータスのインスタンスを生成します.
	 *
	 * @param code ステータスコード
	 */
	private ResourceActionStatus(int code) {
		this.code = code;
		this.httpStatus = HttpStatus.valueOf(code);
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @return the httpStatus
	 */
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
