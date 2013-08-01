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

import java.util.HashMap;
import java.util.Map;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.ConflictException;
import com.htmlhifive.resourcefw.message.FailureResponseMessage;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.resource.common.SyncAction;

/**
 * resource frameworkの{@link ConflictException}に対してsync機能において必要となる機能を追加したサブクラス.<br/>
 * 競合の種類、サーバで管理されている競合対象のリソースアイテムを{@link FailureResponseMessage}に対して設定します.
 *
 * @author kishigam
 */
public abstract class SyncConflictException extends ConflictException {

	private static final long serialVersionUID = 8751114469722489096L;

	/**
	 * sync機能の動作設定パラメータオブジェクト
	 */
	private SyncConfigurationParameter syncConfigurationParameter;

	/**
	 * 競合の種類を表す{@link SyncAction}オブジェクト
	 */
	private SyncAction conflictType;

	/**
	 * サーバで管理されている競合対象のリソースアイテム
	 */
	private Object conflictedItem;

	/**
	 * この例外から生成される{@link FailureResponseMessage FailureResponseMessage}に対してレスポンスボディのための情報を追加します.
	 */
	@Override
	protected FailureResponseMessage editFailureResponse(FailureResponseMessage failureResponseMessage) {

		Map<String, Object> body = new HashMap<>();
		body.put(syncConfigurationParameter.CONFLICT_TYPE, conflictType);
		body.put(syncConfigurationParameter.RESOURCE_ITEM, conflictedItem);

		failureResponseMessage.put(failureResponseMessage.getMessageMetadata().RESPONSE_BODY, body);
		return failureResponseMessage;
	}

	/**
	 * @see AbstractResourceException
	 */
	public SyncConflictException(SyncAction conflictType, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(requestMessage);
		this.syncConfigurationParameter = syncConfigurationParameter;
		this.conflictType = conflictType;
		this.conflictedItem = conflictedItem;
	}

	/**
	 * @see AbstractResourceException
	 */
	public SyncConflictException(String message, SyncAction conflictType, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(message, requestMessage);
		this.syncConfigurationParameter = syncConfigurationParameter;
		this.conflictType = conflictType;
		this.conflictedItem = conflictedItem;
	}

	/**
	 * @see AbstractResourceException
	 */
	public SyncConflictException(Throwable cause, SyncAction conflictType, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(cause, requestMessage);
		this.syncConfigurationParameter = syncConfigurationParameter;
		this.conflictType = conflictType;
		this.conflictedItem = conflictedItem;
	}

	/**
	 * @see AbstractResourceException
	 */
	public SyncConflictException(String message, Throwable cause, SyncAction conflictType, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(message, cause, requestMessage);
		this.syncConfigurationParameter = syncConfigurationParameter;
		this.conflictType = conflictType;
		this.conflictedItem = conflictedItem;
	}

	/**
	 * @see AbstractResourceException
	 */
	protected SyncConflictException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, SyncAction conflictType, Object conflictedItem,
			SyncConfigurationParameter syncConfigurationParameter, RequestMessage requestMessage) {
		super(message, cause, enableSuppression, writableStackTrace, requestMessage);
		this.syncConfigurationParameter = syncConfigurationParameter;
		this.conflictType = conflictType;
		this.conflictedItem = conflictedItem;
	}
}
