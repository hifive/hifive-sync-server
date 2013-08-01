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
 * レスポンスメッセージを保持するAbstractMessageContainer実装.<br>
 * HTTPレスポンスヘッダに設定すべきデータなど、各メッセージの情報とは独立した場所で保持したいデータは、このクラスを通じてレスポンスのコンテキスト情報に設定することができます.
 *
 * @author kishigam
 */
public class ResponseMessageContainer extends AbstractMessageContainer<ResponseMessage> {

	/**
	 * 各レスポンスメッセージから共通で参照されるコンテキスト情報(への参照).
	 */
	private ResponseMessageContext responseMessageContext = new ResponseMessageContext();

	/**
	 * 多重化リクエストに対するレスポンスかどうかを指定してインスタンスを生成します.
	 *
	 * @param multiplexed　多重化リクエストに対するレスポンスである場合true
	 */
	public ResponseMessageContainer(boolean multiplexed) {
		super(multiplexed);
	}

	/**
	 * コンテキスト情報を返します.
	 *
	 * @return このコンテナから参照できるコンテキスト情報(への参照)
	 */
	@Override
	protected AbstractMessageContext getContext() {
		return responseMessageContext;
	}
}
