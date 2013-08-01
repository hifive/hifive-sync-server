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

import com.htmlhifive.resourcefw.exception.AbstractResourceException;

/**
 * リクエストに対する処理が失敗したときに使用される{@link ResponseMessage ResponseMessage}.<br>
 * リソースが処理失敗時にスローする{@link AbstractResourceException AbstractResourceException}から生成できます.<br>
 * エラー情報として、例外の名前、詳細情報、スタックトレースを保持します.
 *
 * @author kishigam
 */
public class FailureResponseMessage extends ResponseMessage {

	/**
	 * リクエストメッセ―ジ、失敗原因となったフレームワーク例外、このレスポンスメッセージの情報源から、例外情報を含むレスポンスメッセージを生成します.<br>
	 * 情報源は、通常フレームワーク例外をハンドリングしたコンポーネントになります.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param resourceException　フレームワーク例外
	 * @param source 情報源
	 */
	public FailureResponseMessage(RequestMessage requestMessage, AbstractResourceException resourceException,
			MessageSource source) {

		super(requestMessage);

		put(getMessageMetadata().RESPONSE_STATUS, resourceException.getErrorStatus(), source);
		put(getMessageMetadata().ERROR_CAUSE, resourceException.getClass().getName(), source);
		put(getMessageMetadata().ERROR_DETAIL_INFO, resourceException.getMessage(), source);
		put(getMessageMetadata().ERROR_STACK_TRACE, resourceException.getStackTrace(), source);
	}
}
