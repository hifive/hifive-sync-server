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

import com.htmlhifive.resourcefw.message.FailureResponseMessage;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.resource.common.SyncAction;

/**
 * データ同期における更新競合を表す{@link SyncUpdateConflictException}のサブクラス.<br/>
 * サーバで管理されている競合対象のリソースアイテムを {@link FailureResponseMessage}に対して設定します.
 *
 * @author kishigam
 */
public class SyncUpdateConflictException extends SyncConflictException {

	private static final long serialVersionUID = -3514081921806355366L;

	/**
	 * @see SyncConflictException
	 */
	public SyncUpdateConflictException(Object conflictedItem, SyncConfigurationParameter syncConfigurationParameter,
			RequestMessage requestMessage) {
		super(SyncAction.CONFLICT, conflictedItem, syncConfigurationParameter, requestMessage);
	}

	/**
	 * @see SyncConflictException
	 */
	public SyncUpdateConflictException(String message, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(message, SyncAction.CONFLICT, conflictedItem, syncConfigurationParameter, requestMessage);
	}

	/**
	 * @see SyncConflictException
	 */
	public SyncUpdateConflictException(Throwable cause, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(cause, SyncAction.CONFLICT, conflictedItem, syncConfigurationParameter, requestMessage);
	}

	/**
	 * @see SyncConflictException
	 */
	public SyncUpdateConflictException(String message, Throwable cause, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(message, cause, SyncAction.CONFLICT, conflictedItem, syncConfigurationParameter, requestMessage);
	}

	/**
	 * @see SyncConflictException
	 */
	protected SyncUpdateConflictException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, Object conflictedItem, SyncConfigurationParameter syncConfigurationParameter,
			RequestMessage requestMessage) {
		super(message, cause, enableSuppression, writableStackTrace, SyncAction.CONFLICT, conflictedItem,
				syncConfigurationParameter, requestMessage);
	}
}
