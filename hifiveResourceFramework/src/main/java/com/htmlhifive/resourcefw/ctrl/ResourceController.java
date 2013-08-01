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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.message.MessageContainerMethodProcessor;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.resourcefw.service.ResourceProcessor;

/**
 * フレームワークのController(Handler).<br/>
 * フレームワークの処理対象である全てのリクエストはこのクラスで処理されます.
 */
public class ResourceController {

	/**
	 * user-agentがIEであると判別するための文字列.
	 */
	protected static final String IE_UA_CONTAINS = "MSIE";

	/**
	 * user-agentがChromeであると判別するための文字列.
	 */
	protected static final String CHROME_UA_CONTAINS = "Chrome";

	/**
	 * user-agentがFirefoxであると判別するための文字列.
	 */
	protected static final String FIREFOX_UA_CONTAINS = "Firefox";

	/**
	 * user-agentがOperaであると判別するための文字列.
	 */
	protected static final String OPERA_UA_CONTAINS = "Opera";

	/**
	 * user-agentがSafariであると判別するための文字列.
	 */
	protected static final String SAFARI_UA_CONTAINS = "Safari";

	/**
	 * リソースプロセッサ
	 */
	private ResourceProcessor resourceProcessor;

	/**
	 * メッセージメタデータオブジェクト
	 */
	private MessageMetadata messageMetadata;

	/**
	 * フレームワーク動作設定パラメータオブジェクト
	 */
	private ResourceConfigurationParameter resourceConfigurationParameter;

	/**
	 * フレームワークのHandlerメソッド.<br>
	 * {@link RequestMappingHandlerAdapter RequestMappingHandlerAdapter}で処理されたリクエストはこのメソッドに渡されます.<br>
	 * 引数は{@link MessageContainerMethodProcessor MessageContainerMethodProcessor}でHTTP Requestから生成されます.<br>
	 * 戻り値もまた{@link MessageContainerMethodProcessor MessageContainerMethodProcessor}で処理され、HTTP Responseとしてクライアントに返されます.
	 *
	 * @param webRequest HTTPRequestのラッパー
	 * @param requestMessages リクエストデータを保持したRequestMessageのコンテナ
	 * @return ResponseMessageのコンテナ
	 */
	@ResourceHandler
	public ResponseMessageContainer handle(WebRequest webRequest, RequestMessageContainer requestMessages) {

		// UserPrincipalをContext情報として格納
		// webRequest.getPrincipal()はnullの可能性があるため、その場合はSpring SecurityのSecurityContextHolderから取得する
		Principal principal = webRequest.getUserPrincipal();
		if (principal == null) {
			principal = SecurityContextHolder.getContext().getAuthentication();
		}
		requestMessages.putContextData(messageMetadata.USER_PRINCIPAL, principal, MessageSource.CONTROLLER);

		// HTTP methodごとのデフォルトアクションの適用
		for (RequestMessage message : requestMessages.getMessages()) {
			applyDefaultAction(message);
		}

		// サブクラスで追加可能なリクエスト編集処理
		editRequest(webRequest, requestMessages);

		ResponseMessageContainer responseMessages = resourceProcessor.process(requestMessages);

		// サブクラスで追加可能なレスポンス編集処理
		editResponse(webRequest, responseMessages, requestMessages);

		// エラー詳細情報を含めない場合、削除
		// リソースプロセッサで例外がスローされた場合はExceptionHandlerでハンドリングされるためこの処理は実行されない
		// リソースで例外がスローされ、プロセッサが処理を継続した場合にここで編集が行われる.
		if (!resourceConfigurationParameter.RESPONSE_WITH_ERROR_DETAIL) {
			for (ResponseMessage message : responseMessages.getMessages()) {
				message.remove(getMessageMetadata().ERROR_CAUSE);
				message.remove(getMessageMetadata().ERROR_DETAIL_INFO);
				message.remove(getMessageMetadata().ERROR_STACK_TRACE);
			}
		}

		// 多重化リクエストでなければ、Content-Dispositionの要否判定と設定を行う
		if (!(responseMessages.isMultiplexed())) {
			putContentDisposition(requestMessages.getMessages().get(0), responseMessages.getMessages().get(0));
		}

		return responseMessages;
	}

	/**
	 * 条件を満たす場合、UserAgentに従ったContent-Dispositionヘッダを設定します.<br/>
	 * 条件は、単一リクエストのパラメータとして{@link MessageMetadata#REQUEST_FILE_DOWNLOAD　REQUEST_FILE_DOWNLOAD}がtrueの時です.<br/>
	 * また、その時はそのリクエストを処理するリソースは{@link MessageMetadata#RESPONSE_DOWNLOAD_FILE_NAME　RESPONSE_DOWNLOAD_FILE_NAME}
	 * をレスポンスメッセージに設定する必要があります.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param responseMessage レスポンスメッセージ
	 */
	@SuppressWarnings("unchecked")
	private void putContentDisposition(RequestMessage requestMessage, ResponseMessage responseMessage) {

		// リクエストメッセージからファイルのダウンロード要求を表すパラメータを取得
		Object downloadRequestObj = requestMessage.get(messageMetadata.REQUEST_FILE_DOWNLOAD);
		if (downloadRequestObj == null) {
			return;
		}

		// レスポンスメッセージにすでにRESPONSE_HEADERがあれば取得し、無ければMapをput
		Map<String, Object> headers;
		Object headersObj = responseMessage.get(messageMetadata.RESPONSE_HEADER);
		if (headersObj == null) {
			headers = new HashMap<>();
			responseMessage.put(messageMetadata.RESPONSE_HEADER, headers);
		} else {
			headers = (Map<String, Object>) headersObj;
		}

		try {

			// パラメータ値が"true"であれば、リクエストのUser-Agentを見てレスポンスにContent-Dispotionヘッダを設定する
			if (Boolean.valueOf((String) downloadRequestObj)) {

				// リソースクラスで指定されたファイル名
				String fileName = (String) responseMessage.get(messageMetadata.RESPONSE_DOWNLOAD_FILE_NAME);
				if (fileName == null || fileName.isEmpty()) {
					throw new GenericResourceException(
							"When resource class returns file data, response message need to include RESPONSE_DOWNLOAD_FILE_NAME metadata.");
				}

				// リクエストヘッダからユーザーエージェント（ブラウザの名前）取得
				String userAgent = (String) requestMessage.get(messageMetadata.HTTP_HEADER_USER_AGENT);

				// Content-DispositionのfileNameに用いる文字列の生成
				String fileNameStr = null;
				if (userAgent.contains(IE_UA_CONTAINS)) {
					fileNameStr = "filename=\"" + URLEncoder.encode(fileName, "UTF8").replace("+", "%20") + "\"";
					// Safariは、HttpHeaderをバイナリ化する必要があり、現状対応できていない
					//				} else if (!userAgent.contains(CHROME_UA_CONTAINS) && userAgent.contains(SAFARI_UA_CONTAINS)) {
					//					fileNameStr = "\"" + fileName + "\"";
				} else {
					// Chrome,Firefox,Opera
					fileNameStr = "filename*=utf-8'ja'" + URLEncoder.encode(fileName, "UTF8").replace("+", "%20");
				}

				String contentDispositionValue = "attachment;" + fileNameStr;

				headers.put(messageMetadata.HTTP_HEADER_CONTENT_DISPOSITION, contentDispositionValue);
			}

		} catch (UnsupportedEncodingException e) {
			throw new GenericResourceException("bad charactor encoding on response file name.");
		}
	}

	/**
	 * リソースプロセッサに渡す前のリクエスト編集処理を記述します.
	 *
	 * @param webRequest HTTPRequestのラッパー
	 * @param requestMessages リクエストデータを保持したRequestMessageのコンテナ
	 */
	protected void editRequest(WebRequest webRequest, RequestMessageContainer requestMessages) {
		// no edit
	}

	/**
	 * リソースプロセッサから返されたレスポンスの編集処理を記述します.
	 *
	 * @param webRequest HTTPRequestのラッパー
	 * @param responseMessages レスポンスデータを保持したResponseMessageのコンテナ
	 * @param requestMessages リクエストデータを保持したRequestMessageのコンテナ
	 */
	protected void editResponse(WebRequest webRequest, ResponseMessageContainer responseMessages,
			RequestMessageContainer requestMessages) {
		// no edit
	}

	/**
	 * {@link MessageMetadata MessageMetadata}を参照し、 「アクション」メタデータが指定されていない場合にデフォルトアクションを設定します. <br>
	 * message-metadata.propertiesで変更することができます。
	 *
	 * @param message　リクエストMessage
	 */
	protected void applyDefaultAction(RequestMessage message) {

		String action = (String) message.get(messageMetadata.ACTION);

		if (action == null) {
			String method = (String) message.get(messageMetadata.HTTP_METHOD);
			RequestMethod requestMethod = RequestMethod.valueOf(method);

			String defaultAction = null;
			if (requestMethod == RequestMethod.GET) {
				Object query = message.get(messageMetadata.QUERY);
				defaultAction = query == null ? resourceConfigurationParameter.DEFAULT_ACTION_FOR_GET_BY_ID
						: resourceConfigurationParameter.DEFAULT_ACTION_FOR_GET_BY_QUERY;
			}
			if (requestMethod == RequestMethod.PUT)
				defaultAction = resourceConfigurationParameter.DEFAULT_ACTION_FOR_PUT;
			if (requestMethod == RequestMethod.DELETE)
				defaultAction = resourceConfigurationParameter.DEFAULT_ACTION_FOR_DELETE;
			if (requestMethod == RequestMethod.POST){
				if (message.get(messageMetadata.COPY) != null) {
				//if (message.get("copy") != null) {
					defaultAction = resourceConfigurationParameter.DEFAULT_ACTION_FOR_COPY;
				} else if (message.get(messageMetadata.MOVE) != null) {
					defaultAction = resourceConfigurationParameter.DEFAULT_ACTION_FOR_MOVE;
				} else {
					defaultAction = resourceConfigurationParameter.DEFAULT_ACTION_FOR_POST;
				}
			}
			message.put(messageMetadata.ACTION, defaultAction, MessageSource.CONTROLLER);
		}
	}

	/**
	 * @return the resourceProcessor
	 */
	protected ResourceProcessor getResourceProcessor() {
		return resourceProcessor;
	}

	/**
	 * @param resourceProcessor the resourceProcessor to set
	 */
	public void setResourceProcessor(ResourceProcessor resourceProcessor) {
		this.resourceProcessor = resourceProcessor;
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
