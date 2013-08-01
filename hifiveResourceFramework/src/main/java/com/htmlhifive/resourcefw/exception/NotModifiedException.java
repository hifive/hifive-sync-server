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

import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.resource.ResourceActionStatus;

/**
 * "304 Not Modified"に対応したフレームワーク例外.
 *
 * @author kishigam
 */
public class NotModifiedException extends AbstractResourceException {

	private static final long serialVersionUID = 6992066187015405882L;

	private static final ResourceActionStatus STATUS = ResourceActionStatus.NOT_MODIFIED;

	/**
	 * @see AbstractResourceException
	 */
	public NotModifiedException(RequestMessage requestMessage) {
		super(requestMessage, STATUS);
	}

	/**
	 * @see AbstractResourceException
	 */
	public NotModifiedException(String message, RequestMessage requestMessage) {
		super(message, requestMessage, STATUS);
	}

	/**
	 * @see AbstractResourceException
	 */
	public NotModifiedException(Throwable cause, RequestMessage requestMessage) {
		super(cause, requestMessage, STATUS);
	}

	/**
	 * @see AbstractResourceException
	 */
	public NotModifiedException(String message, Throwable cause, RequestMessage requestMessage) {
		super(message, cause, requestMessage, STATUS);
	}

	/**
	 * @see AbstractResourceException
	 */
	protected NotModifiedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace, RequestMessage requestMessage) {
		super(message, cause, enableSuppression, writableStackTrace, requestMessage, STATUS);
	}
}
