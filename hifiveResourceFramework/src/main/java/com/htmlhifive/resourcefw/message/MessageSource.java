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
package com.htmlhifive.resourcefw.message;

/**
 * {@link MessageKeyInfo MessageKeyInfo}に指定されるリクエスト、レスポンスメッセージの情報源を表す列挙型.
 *
 * @author kishigam
 */
public enum MessageSource {

	// 原則としてrequestはこの順に優先度低、responseは逆
	RESOURCE, PROCESSOR, CONTROLLER, URL_PARAM, URL_PATH, BODY, HEADER, HTTP_METHOD, COOKIE,
}
