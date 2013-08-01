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

import java.util.Deque;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * リクエストメッセージの共通情報を参照するためのコンテキスト情報クラス.<br>
 * {@link RequestMessage RequestMessage}あるいは{@link RequestMessageContainer RequestMessageContainer}を経由して利用することを想定しています.
 *
 * @author kishigam
 */
public class RequestMessageContext extends AbstractMessageContext {

	/**
	 * リクエストスコープで保持されるコンテキスト情報の実体を参照するための属性名
	 */
	static final String REQUEST_MESSAGE_CONTEXT_ATTRIBUTE = "__requestMessageContextData";

	/**
	 * リクエストスコープからリクエストのコンテキスト情報を取得します.
	 *
	 * @return コンテキスト情報
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Deque<MessageKeyInfo>> getMessageContextData() {

		return (Map<String, Deque<MessageKeyInfo>>) RequestContextHolder.currentRequestAttributes().getAttribute(
				REQUEST_MESSAGE_CONTEXT_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
	}
}
