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
package com.htmlhifive.sync.ctrl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.htmlhifive.resourcefw.ctrl.ResourceController;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.BadRequestException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.exception.SyncUploadDuplicatedException;
import com.htmlhifive.sync.service.SyncRequestCommonData;

/**
 * sync機能を追加したresource frameworkのController(Handler).
 *
 * @author kishigam
 */
public class SyncController extends ResourceController {

	/**
	 * sync機能の動作設定パラメータオブジェクト
	 */
	private SyncConfigurationParameter syncConfigurationParameter;

	/**
	 * リクエスト編集処理.<br/>
	 * syncリクエスト共通データを事前に生成し、コンテキスト情報として設定します.
	 */
	@Override
	protected void editRequest(WebRequest webRequest, RequestMessageContainer requestMessages) {

		// syncリクエスト共通データをRequestMessageContainerに設定
		SyncRequestCommonData requestCommon = createSyncRequestCommonData(requestMessages);
		requestMessages.putContextData(syncConfigurationParameter.REQUEST_COMMON_DATA, requestCommon,
				MessageSource.CONTROLLER);
	}

	/**
	 * リクエスト情報からsyncリクエストのための共通データを抽出し、{@link SyncRequestCommonData SyncRequestCommonData}に格納したものを返します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 * @return syncリクエスト共通データオブジェクト
	 */
	private SyncRequestCommonData createSyncRequestCommonData(RequestMessageContainer requestMessages) {

		SyncRequestCommonData requestCommon = new SyncRequestCommonData();

		try {
			// 多重化リクエストの場合はコンテナ(コンテキスト情報)から、単一の場合はそのメッセージから共通データを抽出
			if (requestMessages.isMultiplexed()) {

				// storageIdの抽出
				setUpStorageId(requestCommon,
						(String) requestMessages.getContextData(syncConfigurationParameter.STORAGE_ID), requestMessages);

				// 前回上り更新時刻の抽出
				setUpLastUploadTime(requestCommon,
						(String) requestMessages.getContextData(syncConfigurationParameter.LAST_UPLOAD_TIME),
						requestMessages);

			} else {
				RequestMessage requestMessage = requestMessages.getMessages().get(0);

				// storageIdの抽出
				setUpStorageId(requestCommon, (String) requestMessage.get(syncConfigurationParameter.STORAGE_ID),
						requestMessages);

				// 前回上り更新時刻の抽出
				setUpLastUploadTime(requestCommon,
						(String) requestMessage.get(syncConfigurationParameter.LAST_UPLOAD_TIME), requestMessages);
			}

		} catch (AbstractResourceException e) {
			// ラップしてスローすることで、resource frameworkのResourceExceptionHandlerに処理を任せる
			throw new GenericResourceException(e);
		}

		// 同期時刻の生成
		requestCommon.setSyncTime(generateSyncTime(requestMessages));

		return requestCommon;
	}

	/**
	 * リクエストから抽出したストレージIDを共通データオブジェクトに設定します.<br>
	 * 抽出できない場合、新規に生成します.
	 *
	 * @param requestCommon syncリクエスト共通データ
	 * @param storageId リクエストから抽出したストレージID
	 * @param requestMessages リクエストメッセージコンテナ
	 */
	private void setUpStorageId(SyncRequestCommonData requestCommon, String storageId,
			RequestMessageContainer requestMessages) {

		// storageIdがセットされていなければ生成
		if (storageId == null || storageId.isEmpty()) {
			storageId = generateStrageId(requestMessages);
		}

		requestCommon.setStorageId(storageId);
	}

	/**
	 * リクエストから抽出した前回上り更新時刻を共通データオブジェクトに設定します.<br>
	 * 抽出できなかった場合は何も設定しません(ゼロ時刻は初回上り更新時刻を表すため、何も設定しない状態とは異なります).<br>
	 *
	 * @param requestCommon syncリクエスト共通データ
	 * @param lastUploadTimeStr リクエストから抽出し前回上り更新時刻文字列
	 * @param requestMessages リクエストメッセージコンテナ
	 * @throws BadRequestException 前回上り更新時刻が数値として解釈できない場合
	 */
	private void setUpLastUploadTime(SyncRequestCommonData requestCommon, String lastUploadTimeStr,
			RequestMessageContainer requestMessages) throws BadRequestException {

		if (lastUploadTimeStr == null) {
			return;
		}

		// 前回上り更新時刻が抽出できたらセット
		try {
			requestCommon.setLastUploadTime(Long.parseLong(lastUploadTimeStr));
		} catch (NumberFormatException e) {
			throw new BadRequestException("Upload request must include last upload time.", e, requestMessages
					.getMessages().get(0));
		}
	}

	/**
	 * ストレージIDを生成します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 * @return ストレージID
	 */
	protected String generateStrageId(RequestMessageContainer requestMessages) {

		return UUID.randomUUID().toString();
	}

	/**
	 * 同期時刻を決定し、返します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 * @return 同期時刻
	 */
	protected long generateSyncTime(RequestMessageContainer requestMessages) {

		return System.currentTimeMillis();
	}

	/**
	 * sync機能としてのレスポンス編集処理.<br/>
	 * syncリクエスト共通データの情報を取り出し、HTTPレスポンスの適切な箇所に反映されるようにレスポンスへ再セットします.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void editResponse(WebRequest webRequest, ResponseMessageContainer responseMessages,
			RequestMessageContainer requestMessages) {

		// ストレージID、同期時刻をヘッダで返す
		Map<String, Object> headers;

		// 多重化リクエストではContextDataのheaderMapへ
		if (responseMessages.isMultiplexed()) {

			headers = (Map<String, Object>) responseMessages.getContextData(getMessageMetadata().RESPONSE_HEADER);
			if (headers == null) {
				headers = new HashMap<>();
				responseMessages
						.putContextData(getMessageMetadata().RESPONSE_HEADER, headers, MessageSource.CONTROLLER);
			}

		} else {

			// 単一リクエストはそのResponseMessageのheaderMapへ
			// 単一リクエストのgetContextデータとMessage本体のheaderをマージするようにMethodProcessorのhandleReturnValueを変更するか、
			// ContainerのgetContextData/putContextDataで透過的に単一リクエストのメッセージにアクセスするか
			// によって、多重化リクエストと処理を同形にできる

			ResponseMessage responseMessage = responseMessages.getMessages().get(0);

			headers = (Map<String, Object>) responseMessage.get(getMessageMetadata().RESPONSE_HEADER);
			if (headers == null) {
				headers = new HashMap<>();
				responseMessage.put(getMessageMetadata().RESPONSE_HEADER, headers, MessageSource.CONTROLLER);
			}

		}

		headers.put(syncConfigurationParameter.STORAGE_ID,
				responseMessages.getContextData(syncConfigurationParameter.STORAGE_ID));
		headers.put(syncConfigurationParameter.SYNC_TIME,
				responseMessages.getContextData(syncConfigurationParameter.SYNC_TIME));
	}

	/**
	 * 上り更新の二重送信が検知された場合の例外ハンドラ.<br/>
	 * 前回の上り更新時刻を同期時刻としてクライアントに返します.
	 *
	 * @param e {@link SyncUploadDuplicatedException}
	 * @return {@link ResponseEntity}
	 */
	@ExceptionHandler
	protected ResponseEntity<?> handleSyncUploadDuplication(SyncUploadDuplicatedException e) {

		// 確認：
		// もしResponseMessageContainerを返すことでMethodProcessorを通れば
		// でResponseEntityの生成をMethodProcessorにまとめることができる
		// ResourceExceptionHandlerも同様

		ResponseMessageContainer responseMessageContainer = e.getResponseMessageContainer();

		SyncRequestCommonData lastUploadRequest = (SyncRequestCommonData) responseMessageContainer
				.getContextData(syncConfigurationParameter.REQUEST_COMMON_DATA);

		String storageIdKeyName = syncConfigurationParameter.STORAGE_ID.replaceFirst(
				getMessageMetadata().PREFIX_METADATA, getMessageMetadata().PREFIX_HTTP_HEADER);
		String syncTimeKeyName = syncConfigurationParameter.SYNC_TIME.replaceFirst(
				getMessageMetadata().PREFIX_METADATA, getMessageMetadata().PREFIX_HTTP_HEADER);

		HttpHeaders headers = new HttpHeaders();
		headers.add(storageIdKeyName, lastUploadRequest.getStorageId());
		headers.add(syncTimeKeyName, String.valueOf(lastUploadRequest.getLastUploadTime()));

		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	protected SyncConfigurationParameter getSyncConfigurationParameter() {
		return syncConfigurationParameter;
	}

	public void setSyncConfigurationParameter(SyncConfigurationParameter syncConfigurationParameter) {
		this.syncConfigurationParameter = syncConfigurationParameter;
	}
}
