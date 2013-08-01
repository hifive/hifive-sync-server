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
 * リクエスト情報を保持するAbstractMessage実装.
 *
 * @author kishigam
 */
public class RequestMessage extends AbstractMessage<RequestMessageContext> {

	/**
	 * メッセージメタデータを指定してインスタンスを生成します.<br>
	 * またリクエストメッセージ用のコンテキスト情報への参照が設定されます.
	 *
	 * @param messageMetadata
	 */
	public RequestMessage(MessageMetadata messageMetadata) {
		super(messageMetadata);
		setMessageContext(new RequestMessageContext());
	}

	/**
	 * HTTPリクエストのパス情報を取得するコンビニエンスメソッド.<br>
	 * 同じくリクエストメッセージのメタデータである{@link MessageMetadata#REQUEST_PATH}は処理の経過で変更されていくため、元のURLに含まれるパス情報は
	 * {@link MessageMetadata#REQUEST_PATH_ORG}で参照する必要があります.
	 *
	 * @return 元のURLに含まれるパス情報
	 */
	public String getOriginalPath() {
		return (String) get(getMessageMetadata().REQUEST_PATH_ORG);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof RequestMessage))
			return false;

		return super.equals(obj);
	}

	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(super.hashCode()).append(this.getClass()).toHashCode();
	}
}
