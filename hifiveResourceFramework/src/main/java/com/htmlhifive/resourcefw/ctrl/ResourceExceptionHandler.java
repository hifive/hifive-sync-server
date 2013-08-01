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
package com.htmlhifive.resourcefw.ctrl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.message.FailureResponseMessage;

/**
 * フレームワーク例外をハンドリンクするExceptionHandler.
 *
 * @author kishigam
 */
@ControllerAdvice
public class ResourceExceptionHandler {

	/**
	 * メッセージメタデータオブジェクト.
	 */
	private MessageMetadata messageMetadata;

	/**
	 * フレームワーク動作設定パラメータオブジェクト
	 */
	private ResourceConfigurationParameter resourceConfigurationParameter;

	/**
	 * {@link GenericResourceException GenericResourceException}にラップされてスローされたフレームワーク例外(
	 * {@link AbstractResourceException AbstractResourceException})をハンドリングし、HTTP ResponseEntityを生成します.
	 * フレームワーク例外以外はそのままリスローされます.
	 *
	 * @param e 例外オブジェクト
	 * @return　フレームワーク例外ごとの内容を含むResponseEntity
	 * @throws GenericResourceException
	 */
	@ExceptionHandler
	public ResponseEntity<?> handleGenericResourceException(GenericResourceException e)
			throws AbstractResourceException {

		if (e.getCause() instanceof AbstractResourceException) {
			AbstractResourceException cause = (AbstractResourceException) e.getCause();

			Map<String, Object> failureMessageBody = createFailureMessageBody(cause);
			if (!failureMessageBody.isEmpty()) {
				return new ResponseEntity<>(failureMessageBody, cause.getErrorStatus().getHttpStatus());
			}

			return new ResponseEntity<>(cause.getErrorStatus().getHttpStatus());
		}

		throw e;
	}

	/**
	 * フレームワーク例外がスローされたときのレスポンスボディに含む情報を返します.<br>
	 * 例外が保持している{@link FailureResponseMessage FailureResponseMessage} にbody情報が保持されていればそれが使用されます.<br>
	 * また、フレームワーク設定に応じてエラー詳細情報を追加します.
	 *
	 * @param cause フレームワーク例外
	 * @return レスポンスボディに含む情報(Map形式)
	 */
	private Map<String, Object> createFailureMessageBody(AbstractResourceException cause) {

		FailureResponseMessage message = cause.getFailureResponseMessage();

		Map<String, Object> failureMessageBody = new HashMap<>();

		// body情報があれば追加
		if (message.get(messageMetadata.RESPONSE_BODY) != null) {
			failureMessageBody.put(messageMetadata.RESPONSE_BODY, message.get(messageMetadata.RESPONSE_BODY));
		}

		// エラー詳細情報を含める場合は追加
		if (resourceConfigurationParameter.RESPONSE_WITH_ERROR_DETAIL) {
			failureMessageBody.put(messageMetadata.ERROR_CAUSE, message.get(messageMetadata.ERROR_CAUSE));
			failureMessageBody.put(messageMetadata.ERROR_DETAIL_INFO, message.get(messageMetadata.ERROR_DETAIL_INFO));
			failureMessageBody.put(messageMetadata.ERROR_STACK_TRACE, message.get(messageMetadata.ERROR_STACK_TRACE));
		}

		return failureMessageBody;
	}

	/**
	 * Multipart形式のファイルアップロードにおいて、最大容量を超過した場合に発生する例外のハンドラ.<br/>
	 *
	 * @param e　例外({@link MaxUploadSizeExceededException MaxUploadSizeExceededException})
	 * @return 例外情報を含むレスポンスボディ(Map)
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(value = HttpStatus.REQUEST_ENTITY_TOO_LARGE)
	@ResponseBody
	public Map<String, Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {

		Map<String, Object> failureMessageBody = new HashMap<>();

		failureMessageBody.put(messageMetadata.ERROR_CAUSE, HttpStatus.REQUEST_ENTITY_TOO_LARGE.toString());
		failureMessageBody.put(messageMetadata.ERROR_DETAIL_INFO, "File upload max size exceeded. ("
				+ resourceConfigurationParameter.MULTIPART_MAX_UPLOAD_SIZE + "bytes)");
		failureMessageBody.put(messageMetadata.ERROR_STACK_TRACE, e.getStackTrace());

		return failureMessageBody;
	}

	/**
	 * @return the messageMetadata
	 */
	protected MessageMetadata getMessageMetadata() {
		return messageMetadata;
	}

	/**
	 * @param messageMetadata the messageMetadata to set
	 */
	public void setMessageMetadata(MessageMetadata messageMetadata) {
		this.messageMetadata = messageMetadata;
	}

	/**
	 * @return the resourceConfigurationParameter
	 */
	protected ResourceConfigurationParameter getResourceConfigurationParameter() {
		return resourceConfigurationParameter;
	}

	/**
	 * @param resourceConfigurationParameter the resourceConfigurationParameter to set
	 */
	public void setResourceConfigurationParameter(ResourceConfigurationParameter resourceConfigurationParameter) {
		this.resourceConfigurationParameter = resourceConfigurationParameter;
	}
}
