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

import com.htmlhifive.resourcefw.message.ResponseMessageContainer;

/**
 * sync上り更新リクエストの二重送信が検知された時にスローされる実行時例外.<br/>
 * 二重送信とは、すでにサーバに反映された上り更新リクエストが再度送信されてきたことを示します.
 *
 * @author kishigam
 */
public class SyncUploadDuplicatedException extends RuntimeException {

	private static final long serialVersionUID = 2892303404492379783L;

	/**
	 * 前回上り更新リクエストの共通データを含むレスポンスメッセージコンテナ
	 */
	private final ResponseMessageContainer responseMessageContainer;

	/**
	 * 前回上り更新リクエストの共通データを保持する例外インスタンスを生成します.
	 *
	 * @param responseMessageContainer 前回上り更新リクエストの共通データを含むレスポンスメッセージコンテナ
	 */
	public SyncUploadDuplicatedException(ResponseMessageContainer responseMessageContainer) {
		this.responseMessageContainer = responseMessageContainer;
	}

	/**
	 * @return the responseMessageContainer
	 */
	public ResponseMessageContainer getResponseMessageContainer() {
		return responseMessageContainer;
	}
}
