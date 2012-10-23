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

import com.htmlhifive.sync.service.upload.UploadResponse;

/**
 * 同期(上り更新)において競合が発生したことを示す例外.<br>
 * サーバ側で管理しているリソースアイテムを保持しており、クライアントに返す情報の生成に使用することができます.
 *
 * @author kishigam
 */
public class ConflictException extends RuntimeException {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = 6836676332372951677L;

	/**
	 * 上り更新レスポンスオブジェクト.
	 */
	private UploadResponse response;

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param response 競合タイプ
	 * @param resourceItems 上り更新レスポンスオブジェクト
	 * @see RuntimeException
	 */
	public ConflictException(UploadResponse response) {
		super();
		this.response = response;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 * @param response 競合タイプ
	 * @param resourceItems 上り更新レスポンスオブジェクト
	 * @see RuntimeException
	 */
	public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
			UploadResponse response) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.response = response;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param cause
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(リソース別Map)
	 * @see RuntimeException
	 */
	public ConflictException(String message, Throwable cause, UploadResponse response) {
		super(message, cause);
		this.response = response;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param message
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(リソース別Map)
	 * @see RuntimeException
	 */
	public ConflictException(String message, UploadResponse response) {
		super(message);
		this.response = response;
	}

	/**
	 * 指定された情報をもとに例外オブジェクトを生成します.
	 *
	 * @param cause
	 * @param conflictType 競合タイプ
	 * @param resourceItems 競合したリソースアイテムリスト(リソース別Map)
	 * @see RuntimeException
	 */
	public ConflictException(Throwable cause, UploadResponse response) {
		super(cause);
		this.response = response;
	}

	/**
	 * @return response
	 */
	public UploadResponse getResponse() {
		return response;
	}
}
