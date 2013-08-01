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
package com.htmlhifive.resourcefw.service;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.config.ResourceConfigurationParameter;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.exception.NotFoundException;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.resourcefw.resource.ResourceActionStatus;
import com.htmlhifive.resourcefw.resource.ResourceMethodInvoker;
import com.htmlhifive.resourcefw.service.processing.AlwaysTerminatingStrategy;
import com.htmlhifive.resourcefw.service.processing.ProcessContinuationStrategy;
import com.htmlhifive.resourcefw.util.ResourcePathUtil;

/**
 * リソースアクションのリクエストを処理し、レスポンスを生成するプロセッサのデフォルト実装.<br>
 * 単一リクエストを実行、あるいは多重化リクエストを順に実行し、失敗時のハンドリング、レスポンスメッセージの生成を担います.<br>
 *
 * @author kishigam
 */
@Transactional
public class DefaultResourceProcessor implements ResourceProcessor {

	private static final Logger LOGGER = Logger.getLogger(DefaultResourceProcessor.class);

	/**
	 * リソースを管理するマネージャ.
	 */
	private ResourceManager resourceManager;

	/**
	 * リソースが例外をスローした時の処理継続Strategy.
	 */
	private ProcessContinuationStrategy processContinuationStrategy = new AlwaysTerminatingStrategy();

	/**
	 * メッセージメタデータオブジェクト.
	 */
	private MessageMetadata messageMetadata;

	/**
	 * フレームワーク動作設定パラメータオブジェクト
	 */
	private ResourceConfigurationParameter resourceConfigurationParameter;

	/**
	 * リソースへのリクエストを実行します.<br/>
	 * コンテナに含まれる全てのリクエストを適切なリソースとそのアクションに振り分けます.<br/>
	 * 結果を取りまとめ、{@link ResponseMessageContainer}に格納して返します.
	 */
	@Override
	public ResponseMessageContainer process(RequestMessageContainer requestMessages) {

		// 全体の事前処理
		preProcess(requestMessages);

		// メインプロセス
		ResponseMessageContainer responseMessages = doProcess(requestMessages);

		// 全体の事後処理
		postProcess(requestMessages, responseMessages);

		return responseMessages;
	}

	/**
	 * プロセス全体の前処理を実行します.<br/>
	 * デフォルト実装では処理はありません.
	 *
	 * @param requestMessages
	 */
	protected void preProcess(RequestMessageContainer requestMessages) {
		// nothing
	}

	/**
	 * プロセス全体のメイン処理を実行します.<br/>
	 * コンテナ内の各リクエストメッセージの処理、および前後処理を実行します.
	 *
	 * @param requestMessages
	 * @return レスポンスメッセージコンテナ
	 */
	protected ResponseMessageContainer doProcess(RequestMessageContainer requestMessages) {

		ResponseMessageContainer responseMessages = new ResponseMessageContainer(requestMessages.isMultiplexed());
		for (RequestMessage requestMessage : requestMessages.getMessages()) {

			ResourceProcessingStatus processingStatus = ResourceProcessingStatus.CONTINUE;
			ResponseMessage responseMessage = null;
			try {

				String path = (String) requestMessage.get(messageMetadata.REQUEST_PATH);

				String[] nameAndRemain = ResourcePathUtil.down(path);
				String name = nameAndRemain[0];

				if (name.isEmpty()) {
					throw new NotFoundException("No resource specified.", requestMessage);
				}

				// 階層を降りて上書きする
				// RequestMessage内のパスが変わる(リソース名部分が除去される)
				requestMessage.put(messageMetadata.REQUEST_PATH, nameAndRemain[1], MessageSource.PROCESSOR);

				String action = (String) requestMessage.get(messageMetadata.ACTION);

				ResourceMethodInvoker targetResource = resourceManager.getResourceMethodByName(name, action,
						requestMessage);

				responseMessage = processMessage(targetResource, requestMessage);

			} catch (AbstractResourceException e) {

				responseMessage = handleResourceException(requestMessages, e);

				// Go Through
			}

			responseMessage.put(messageMetadata.PROCESSING_STATUS, processingStatus, MessageSource.PROCESSOR);
			responseMessages.addMessage(responseMessage);
		}

		return responseMessages;
	}

	/**
	 * プロセス全体の事後処理を実行します.<br/>
	 * デフォルト実装では処理はありません.
	 *
	 * @param requestMessages
	 * @param responseMessages
	 */
	protected void postProcess(RequestMessageContainer requestMessages, ResponseMessageContainer responseMessages) {
		// nothing
	}

	/**
	 * 1つのリクエストの処理を実行します.
	 *
	 * @param resourceMethod リソースに対してアクションを実行するためのInvokerオブジェクト
	 * @param requestMessage リクエストメッセージ
	 * @return リクエスト処理結果を含むレスポンスメッセージ
	 * @throws AbstractResourceException リソース処理前後にスローされるフレームワーク例外
	 */
	protected ResponseMessage processMessage(ResourceMethodInvoker resourceMethod, RequestMessage requestMessage)
			throws AbstractResourceException {

		Object resultObj = resourceMethod.invoke(requestMessage);

		// 次があれば、次を呼んだ結果が最終結果
		ResourceMethodInvoker nextResourceMethod = getNextResource(requestMessage, resultObj);
		if (nextResourceMethod != null) {
			RequestMessage nextRequestMessage = createNextRequestMessage(resultObj, requestMessage);
			return processMessage(nextResourceMethod, nextRequestMessage);
		}

		ResponseMessage responseMessage = createResponseMessage(resultObj, requestMessage);

		return responseMessage;
	}

	/**
	 * リソース処理中にスローされた例外ごとに処理継続可否を判断し、継続する場合はFailureResponseMessageを返します.<br/>
	 * 継続しない場合はGenericResourceExceptionでラップしてスローします.
	 *
	 * @param requestMessages　リクエストメッセージ
	 * @param e スローされた例外
	 * @return レスポンスメッセージ
	 */
	protected ResponseMessage handleResourceException(RequestMessageContainer requestMessages,
			AbstractResourceException e) {

		// 単一リクエストの時、あるいは多重化リクエストで継続判定した結果TERMINATEの時
		// GenericResourceExceptionにラップしてスローすることでトランザクションをロールバックし、ExceptionHandlerにハンドリングさせる
		if (!requestMessages.isMultiplexed()
				|| processContinuationStrategy.continueOnException(e) == ResourceProcessingStatus.TERMINATE) {

			LOGGER.info("[resourcefw]Resource processing is terminated by exception, status : " + e.getErrorStatus()
					+ " ,detail : " + e.getMessage());

			throw new GenericResourceException(e);
		}

		// assert(processingStatus == ResourceProcessingStatus.CONTINUE);
		return e.getFailureResponseMessage();
	}

	/**
	 * 未消化のパスがある場合、前段のリソース処理の結果を含むオブジェクトから次のパスに対する処理を実行するためのリクエストメッセージを生成します.
	 *
	 * @param resultObj 前段のリソース処理結果を含むオブジェクト
	 * @param requestMessage 元のリクエストメッセージ
	 * @return　次のリソース処理のリクエストメッセージオブジェクト
	 */
	protected RequestMessage createNextRequestMessage(Object resultObj, RequestMessage requestMessage) {

		RequestMessage newRequestMessage = new RequestMessage(messageMetadata);

		// リソースチェーン対応時に検討
		// 前回リクエストのcopyや前回結果のセットなど
		return newRequestMessage;
	}

	/**
	 * 未消化のパスの有無を判定して、次のパスに対する処理を実行するためのリソースを探し、返します.
	 *
	 * @param requestMessage 元のリクエストメッセージ
	 * @param resultObj 前段のリソース処理結果を含むオブジェクト
	 * @return 次のリソース、アクションを実行するためのInvokerオブジェクト
	 * @throws AbstractResourceException
	 */
	protected ResourceMethodInvoker getNextResource(RequestMessage requestMessage, Object messageOrItem)
			throws AbstractResourceException {

		// ResponseMessageである=次のリソースでの処理がある可能性がある
		if (messageOrItem instanceof ResponseMessage) {

			// リソースチェーン対応
			// Resourceで処理したパスはREQUEST_PATHから除去されている前提
			// パスが残っていなければ終了
			// あれば、先頭がactionになる
			// nextResourceNameメタデータが設定されていれば、actionとnameで、
			// contentTypeメタデータが設定されていればactionとtypeでリソースを探し、
			// どちらもなければ404や501を返す

			String type = null;
			String action = null;

			return resourceManager.getResourceMethodByType(type, action, requestMessage);
		}

		// ResponseMessageでない=最終結果のリソースアイテムである場合ば終了
		return null;
	}

	/**
	 * リソース処理がそれぞれのリソースアイテム型のようなオブジェクトを返したとき、それを保持するレスポンスメッセージを生成します.
	 *
	 * @param resultObj リソース処理結果のオブジェクト
	 * @param requestMessage リクエストメッセージ
	 * @return レスポンスメッセージ
	 */
	private ResponseMessage createResponseMessage(Object resultObj, RequestMessage requestMessage) {

		if (resultObj instanceof ResponseMessage)
			return (ResponseMessage) resultObj;

		ResponseMessage responseMessage = new ResponseMessage(requestMessage);

		String action = (String) requestMessage.get(messageMetadata.ACTION);
		ResourceActionStatus status = action.equals(resourceConfigurationParameter.DEFAULT_ACTION_FOR_POST) ? ResourceActionStatus.CREATED
				: ResourceActionStatus.OK;

		responseMessage.put(messageMetadata.RESPONSE_STATUS, status, MessageSource.PROCESSOR);
		responseMessage.put(messageMetadata.RESPONSE_BODY, resultObj, MessageSource.PROCESSOR);

		return responseMessage;
	}

	/**
	 * @return the resourceManager
	 */
	protected ResourceManager getResourceManager() {
		return resourceManager;
	}

	/**
	 * @param resourceManager the resourceManager to set
	 */
	@Override
	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	/**
	 * @return the processContinuationStrategy
	 */
	protected ProcessContinuationStrategy getProcessContinuationStrategy() {
		return processContinuationStrategy;
	}

	/**
	 * @param processContinuationStrategy the processContinuationStrategy to set
	 */
	@Override
	public void setProcessContinuationStrategy(ProcessContinuationStrategy processContinuationStrategy) {
		this.processContinuationStrategy = processContinuationStrategy;
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
	@Override
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
	@Override
	public void setResourceConfigurationParameter(ResourceConfigurationParameter resourceConfigurationParameter) {
		this.resourceConfigurationParameter = resourceConfigurationParameter;
	}
}
