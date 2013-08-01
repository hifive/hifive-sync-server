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
 * リクエストメッセージを保持するAbstractMessageContainer実装.
 *
 * @author kishigam
 */
public class RequestMessageContainer extends AbstractMessageContainer<RequestMessage> {

	/**
	 * 各リクエストメッセージから共通で参照されるコンテキスト情報(への参照).
	 */
	private RequestMessageContext requestMessageContext = new RequestMessageContext();

	/**
	 * 多重化リクエストかどうかを指定してインスタンスを生成します.
	 *
	 * @param multiplexed 多重化リクエストの時true
	 */
	public RequestMessageContainer(boolean multiplexed) {
		super(multiplexed);
	}

	/**
	 * コンテキスト情報を返します.
	 *
	 * @return このコンテナから参照できるコンテキスト情報(への参照)
	 */
	@Override
	protected AbstractMessageContext getContext() {
		return requestMessageContext;
	}
}
