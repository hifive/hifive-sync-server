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
package com.htmlhifive.resourcefw.exception;

import com.htmlhifive.resourcefw.message.FailureResponseMessage;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.resource.ResourceActionStatus;

/**
 * フレームワーク例外の抽象基底クラス.<br>
 * 200番台以外のHTTP Statusに対応するフレームワーク例外は、このクラスを継承して実装します.
 *
 * @author kishigam
 */
public abstract class AbstractResourceException extends Exception {

	private static final long serialVersionUID = 813695747589355580L;

	/**
	 * HTTP Statusに対応したエラーステータス.<br>
	 */
	private ResourceActionStatus errorStatus;

	/**
	 * エラー情報を含むレスポンスMessageオブジェクト.<br>
	 */
	private FailureResponseMessage failureResponseMessage;

	/**
	 * @see Exception
	 */
	public AbstractResourceException(RequestMessage requestMessage, ResourceActionStatus errorStatus) {
		super();
		init(requestMessage, errorStatus);
	}

	/**
	 * @see Exception
	 */
	public AbstractResourceException(String message, RequestMessage requestMessage, ResourceActionStatus errorStatus) {
		super(message);
		init(requestMessage, errorStatus);
	}

	/**
	 * @see Exception
	 */
	public AbstractResourceException(Throwable cause, RequestMessage requestMessage, ResourceActionStatus errorStatus) {
		super(cause);
		init(requestMessage, errorStatus);
	}

	/**
	 * @see Exception
	 */
	public AbstractResourceException(String message, Throwable cause, RequestMessage requestMessage,
			ResourceActionStatus errorStatus) {
		super(message, cause);
		init(requestMessage, errorStatus);
	}

	/**
	 * @see Exception
	 */
	protected AbstractResourceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, RequestMessage requestMessage, ResourceActionStatus errorStatus) {
		super(message, cause, enableSuppression, writableStackTrace);
		init(requestMessage, errorStatus);
	}

	/**
	 * この例外のフィールドを設定し、初期化します.<br/>
	 * この例外は、この例外の情報を保持している{@link FailureResponseMessage FailureResponseMessage}を取得することができます.<br/>
	 * 多重化リクエストにおいて例外時に処理を継続する設定である場合、このメッセージを使用します.<br>
	 * 単一リクエストの場合や処理を継続しない場合、例外をキャッチした時にこのメッセージ取り出し、使用できます.<br/>
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param errorStatus この例外が表すステータス
	 */
	private void init(RequestMessage requestMessage, ResourceActionStatus errorStatus) {

		this.errorStatus = errorStatus;

		this.failureResponseMessage = new FailureResponseMessage(requestMessage, this, MessageSource.RESOURCE);
	}

	/**
	 * この例外が保持している{@link FailureResponseMessage FailureResponseMessage}オブジェクトを編集します.<br>
	 * サブクラスでは、レスポンスボディに設定される任意の情報を含めることができます.
	 *
	 * @param failureResponseMessage
	 * @return 編集後の{@link FailureResponseMessage}
	 */
	protected FailureResponseMessage editFailureResponse(FailureResponseMessage failureResponseMessage) {

		// no edit
		return failureResponseMessage;
	}

	/**
	 * @return the errorStatus
	 */
	public ResourceActionStatus getErrorStatus() {
		return errorStatus;
	}

	/**
	 * @return the failureResponseMessage
	 */
	public FailureResponseMessage getFailureResponseMessage() {

		return editFailureResponse(this.failureResponseMessage);
	}
}
