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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.htmlhifive.resourcefw.config.MessageMetadata;

/**
 * レスポンス情報を保持するAbstractMessage実装.
 *
 * @author kishigam
 */
public class ResponseMessage extends AbstractMessage<ResponseMessageContext> {

	/**
	 * レスポンスの元となるリクエストメッセージを指定してインスタンスを生成します.<br/>
	 * またレスポンスメッセージ用のコンテキスト情報への参照が設定されます.<br/>
	 * 加えて、{@link MessageMetadata#ACCEPT ACCEPT}メタデータの情報をリクエストから引き継ぎます.
	 *
	 * @param requestMessage リクエストメッセージ
	 */
	public ResponseMessage(RequestMessage requestMessage) {
		super(requestMessage.getMessageMetadata());

		setMessageContext(new ResponseMessageContext());

		String accept = (String) requestMessage.get(getMessageMetadata().ACCEPT);
		if (accept != null) {
			put(getMessageMetadata().ACCEPT, accept);
		}
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof ResponseMessage))
			return false;

		return super.equals(obj);
	}

	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(super.hashCode()).append(this.getClass()).toHashCode();
	}
}
