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
package com.htmlhifive.sync.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.message.MessageSource;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageContainer;
import com.htmlhifive.resourcefw.message.ResponseMessage;
import com.htmlhifive.resourcefw.message.ResponseMessageContainer;
import com.htmlhifive.resourcefw.resource.ResourceMethodInvoker;
import com.htmlhifive.resourcefw.service.DefaultResourceProcessor;
import com.htmlhifive.resourcefw.util.ResourcePathUtil;
import com.htmlhifive.sync.config.DownloadControlType;
import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.config.UploadControlType;
import com.htmlhifive.sync.exception.SyncUploadDuplicatedException;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.Synchronizer;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.common.SyncAction;

/**
 * sync機能を追加したresource frameworkのリソースプロセッサ実装.<br>
 * 単一リクエストを実行、あるいは多重化リクエストを順に実行し、失敗時のハンドリング、レスポンスメッセージの生成を担います.<br>
 * また、二重送信判定やリソースアイテムの悲観的ロックによる同期制御を実行します.
 *
 * @author kishigam
 */
public class SyncResourceProcessor extends DefaultResourceProcessor {

	private static final Logger LOGGER = Logger.getLogger(SyncResourceProcessor.class);

	/**
	 * 同期制御実行のためにデフォルトで使用するシンクロナイザーオブジェクト
	 */
	private Synchronizer synchronizer;

	/**
	 * sync機能の動作設定パラメータオブジェクト
	 */
	private SyncConfigurationParameter syncConfigurationParameter;

	/**
	 * syncリクエストの前回上り更新結果を管理するリポジトリ.
	 */
	@Autowired
	private SyncRequestCommonDataRepository syncRequestCommonDataRepository;

	/**
	 * HttpServletRequest メソッドによる動作判定のために使用
	 */
	@Autowired
	private HttpServletRequest httpServletRequest;

	/**
	 * リソース処理の事前処理として、二重送信判定、悲観的ロックなどの同期制御を実行します.
	 */
	@Override
	protected void preProcess(RequestMessageContainer requestMessages) {

		String requestPathStr = extractRequestPath(requestMessages);
		String httpMethod = httpServletRequest.getMethod();

		boolean uploadRequest = isUploadRequest(requestPathStr);
		boolean downloadRequest = isDownloadRequest(requestPathStr);
		boolean syncByHttpMethodRequest = isSyncByHttpMethodRequest(requestPathStr);

		if(syncByHttpMethodRequest) {
			// FIXME もうちょっと別の方法でHTTPメソッドが取得/判定できないか？
			if(httpMethod.equals("GET")) {
				downloadRequest = true;
			} else if(httpMethod.equals("POST") || httpMethod.equals("PUT") || httpMethod.equals("DELETE")) {
				uploadRequest = true;
			}
		}

		// syncリクエストでなければ事前処理なし
		if (!(uploadRequest || downloadRequest)) {
			return;
		}

		// URLパスからsync指示の部分を除去
		overwritePath(requestMessages, requestPathStr);

		// 各リクエストメッセージからリソースアイテム共通データIDを抽出し、メッセージに設定
		extractResourceItemCommonId(requestMessages);

		String action = null;
		if (uploadRequest) {
			SyncRequestCommonData currentRequest = (SyncRequestCommonData) requestMessages
					.getContextData(syncConfigurationParameter.REQUEST_COMMON_DATA);

			// 二重送信判定
			// 今回のリクエストで、前回上り更新時刻が設定されていなければ判定不要、今回分の保存もしない
			if (currentRequest.hasLastUploadTime()) {
				checkDuplicateUpload(currentRequest, requestMessages);
			}

			// SyncActionがすべてのメソッドにセットされているかを確認し
			// セットされていなければ適切なメソッドをセット
			setSyncAction(requestMessages, httpMethod);

			// 上り更新同期制御を実行する
			try {
				processUploadControl(requestMessages);
			} catch (AbstractResourceException e) {
				LOGGER.info("[syncfw]Resource sync upload processing is terminated by exception, status : "
						+ e.getErrorStatus() + " ,detail : " + e.getMessage());
			}

			action = syncConfigurationParameter.ACTION_FOR_UPLOAD;
		}

		if (downloadRequest) {

			// 下り更新同期制御を実行し、結果をリクエストメッセージに保持する
			try {
				processDownloadControl(requestMessages);
			} catch (AbstractResourceException e) {
				LOGGER.info("[syncfw]Resource sync download processing is terminated by exception, status : "
						+ e.getErrorStatus() + " ,detail : " + e.getMessage());
			}

			action = syncConfigurationParameter.ACTION_FOR_DOWNLOAD;
		}

		// アクションの反映
		for (RequestMessage requestMessage : requestMessages.getMessages()) {
			requestMessage.put(getMessageMetadata().ACTION, action, MessageSource.PROCESSOR);
		}
	}

	/**
	 * リクエストメッセージからパスを取り出し、かつsyncリクエストのアクション(上り/下り)を指示している部分を除去して再設定します.<br/>
	 * 取り出したパス(アクションを指示している部分と以降のパス)を返します.
	 *
	 * @param requestMessages
	 * @return パスを格納した配列
	 */
	private String extractRequestPath(RequestMessageContainer requestMessages) {

		if (requestMessages.isMultiplexed()) {
			return (String) requestMessages.getContextData(getMessageMetadata().REQUEST_PATH);
		}

		return (String) requestMessages.getMessages().get(0).get(getMessageMetadata().REQUEST_PATH);
	}

	/**
	 * syncリクエストのアクション(上り/下り)を指示している部分を除去して「パス」メタデータを再設定します.<br/>
	 * 取り出したパス(アクションを指示している部分と以降のパス)を返します.
	 *
	 * @param requestMessages
	 * @return パスを格納した配列
	 */
	private String[] overwritePath(RequestMessageContainer requestMessages, String requestPathStr) {

		String[] result;

		if (requestMessages.isMultiplexed()) {

			result = ResourcePathUtil.down(requestPathStr);
			requestMessages.putContextData(getMessageMetadata().REQUEST_PATH, result[1], MessageSource.PROCESSOR);

		} else {

			RequestMessage requestMessage = requestMessages.getMessages().get(0);

			result = ResourcePathUtil.down(requestPathStr);
			requestMessage.put(getMessageMetadata().REQUEST_PATH, result[1], MessageSource.PROCESSOR);
		}

		return result;
	}

	/**
	 * URLパスがsync上り更新リクエストを示しているときtrueを返します.
	 *
	 * @param requestPathStr URLパス
	 * @return sync上り更新リクエストであればtrue
	 */
	private boolean isUploadRequest(String requestPathStr) {

		String[] pathStr = ResourcePathUtil.down(requestPathStr);

		return pathStr[0].equals(syncConfigurationParameter.URL_PATH_UPLOAD);
	}

	/**
	 * URLパスがsync下り更新リクエストを示しているときtrueを返します.
	 *
	 * @param requestPathStr URLパス
	 * @return sync下り更新リクエストであればtrue
	 */
	private boolean isDownloadRequest(String requestPathStr) {

		String[] pathStr = ResourcePathUtil.down(requestPathStr);

		return pathStr[0].equals(syncConfigurationParameter.URL_PATH_DOWNLOAD);
	}

	/**
	 * URLパスがsync下り更新リクエストを示しているときtrueを返します.
	 *
	 * @param requestPathStr URLパス
	 * @return sync下り更新リクエストであればtrue
	 */
	private boolean isSyncByHttpMethodRequest(String requestPathStr) {
		String[] pathStr = ResourcePathUtil.down(requestPathStr);
		return pathStr[0].equals(syncConfigurationParameter.URL_PATH_SYNC_BY_HTTP_METHODS);
	}

	/**
	 * リクエストメッセージから、syncリソースアイテム共通データIDを生成し、メッセージに設定します.<br/>
	 * syncリソースアイテム共通データIDを構成する情報を取り出し、それを用いてインスタンスを生成します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 */
	private void extractResourceItemCommonId(RequestMessageContainer requestMessages) {

		for (RequestMessage requestMessage : requestMessages.getMessages()) {

			String resourceName = ResourcePathUtil.down((String) requestMessage.getPath())[0];
			String resourceItemId = (String) requestMessage.get(syncConfigurationParameter.RESOURCE_ITEM_ID);

			ResourceItemCommonDataId commonDataId = new ResourceItemCommonDataId(resourceName, resourceItemId);
			requestMessage.put(syncConfigurationParameter.RESOURCE_ITEM_COMMON_DATA_ID, commonDataId,
					MessageSource.PROCESSOR);
		}
	}

	/**
	 * 前回上り更新リクエストを参照しsync上り更新リクエストの二重送信を判定します.<br/>
	 * 今回のリクエストで前回上り更新時刻が指定されていない場合は判定を行いません.<br/>
	 * 前回の情報が存在しなければ二重送信ではありません.<br/>
	 * 前回の情報が存在する時、その時刻が前回リクエストの時刻と同じか、それ以前であれば二重送信と判定し、{@link SyncUploadDuplicatedException
	 * SyncUploadDuplicatedException}をスローします.
	 *
	 * @param currentRequest syncリクエスト共通データ
	 * @param requestMessages リクエストメッセージコンテナ
	 */
	private void checkDuplicateUpload(SyncRequestCommonData currentRequest, RequestMessageContainer requestMessages) {

		SyncRequestCommonData lastUploadRequest = syncRequestCommonDataRepository
				.findOne(currentRequest.getStorageId());

		// 初回リクエスト(前回上り更新リクエストの情報が存在しない)でなく、
		// 「前回上り更新時刻」が前回リクエストの時点と同じ、あるいは(何らかの想定外の理由で)前の時刻になっていたら二重送信
		if (lastUploadRequest != null && !currentRequest.isLaterUploadThan(lastUploadRequest)) {

			LOGGER.info(new StringBuilder().append("[syncfw] duplicate uploading detected. storageId : ")
					.append(currentRequest.getStorageId()).append(", lastUpdateTime : ")
					.append(currentRequest.getLastUploadTime()).toString());

			ResponseMessageContainer responseMessages = new ResponseMessageContainer(requestMessages.isMultiplexed());
			responseMessages.putContextData(syncConfigurationParameter.REQUEST_COMMON_DATA, lastUploadRequest,
					MessageSource.PROCESSOR);

			throw new SyncUploadDuplicatedException(responseMessages);

		}

		// 今回分を保存
		syncRequestCommonDataRepository.save(currentRequest);
	}

	/**
	 * 上り更新の同期制御を実行します.<br/>
	 * sync機能の動作設定に従い、複数の上り更新対象リソースアイテムのリクエストを変更します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 * @throws AbstractResourceException
	 */
	private void processUploadControl(RequestMessageContainer requestMessages) throws AbstractResourceException {

		UploadControlType controlType = UploadControlType.valueOf(syncConfigurationParameter.UPLOAD_CONTROL_TYPE);

		if (controlType == UploadControlType.NONE) {
			return;
		}

		MultiValueMap<ResourceItemCommonDataId, RequestMessage> messageMap = new LinkedMultiValueMap<>();
		for (RequestMessage requestMessage : requestMessages.getMessages()) {
			ResourceItemCommonDataId resourceItemCommonDataId = (ResourceItemCommonDataId) requestMessage
					.get(syncConfigurationParameter.RESOURCE_ITEM_COMMON_DATA_ID);
			messageMap.add(resourceItemCommonDataId, requestMessage);
		}

		// リソースアイテム共通データIDでソート
		List<ResourceItemCommonDataId> commonDataIdList = new ArrayList<>(messageMap.keySet());
		Collections.sort(commonDataIdList);

		switch (controlType) {
			case SORT:

				// Container内のMessageの順序を入れ替える
				List<RequestMessage> sorted = new ArrayList<>();
				for (ResourceItemCommonDataId itemCommonDataId : commonDataIdList) {
					List<RequestMessage> messagesForId = messageMap.get(itemCommonDataId);
					for (RequestMessage message : messagesForId) {
						sorted.add(message);
					}
				}
				requestMessages.setMessages(sorted);

				break;

			case LOCK:

				// 対象データを先読みし、共通データを悲観的ロックする
				for (ResourceItemCommonDataId itemCommonDataId : commonDataIdList) {
					List<RequestMessage> messagesForId = messageMap.get(itemCommonDataId);
					for (RequestMessage message : messagesForId) {

						// リソースクラスの取得
						ResourceMethodInvoker resourceMethod = getResourceManager().getResourceMethodByName(
								itemCommonDataId.getResourceName(), syncConfigurationParameter.ACTION_FOR_GETFORUPDATE,
								message);
						applyDefaultSynchronizer(resourceMethod);

						// ロックした共通データをリクエストメッセージに追加
						// 後に取得するロジックの関係上、リストで格納する
						@SuppressWarnings("unchecked")
						List<ResourceItemCommonData> got = (List<ResourceItemCommonData>) resourceMethod
								.invoke(message);
						message.put(syncConfigurationParameter.RESOURCE_ITEM_COMMON_DATA, got);
					}
				}

				break;

			default:
				break;

		}
	}

	/**
	 * 上り更新の同期制御を実行します.<br/>
	 * sync機能の動作設定に従い、複数の下り更新対象リソースアイテムのリクエストを変更します.
	 *
	 * @param requestMessages
	 * @throws AbstractResourceException
	 */
	private void processDownloadControl(RequestMessageContainer requestMessages) throws AbstractResourceException {

		DownloadControlType controlType = DownloadControlType.valueOf(syncConfigurationParameter.DOWNLOAD_CONTROL_TYPE);

		switch (controlType) {

		// LOCKの場合、対象データを先読みして共通データを悲観的ロックする
			case LOCK:

				MultiValueMap<ResourceItemCommonDataId, RequestMessage> messageMap = new LinkedMultiValueMap<>();
				for (RequestMessage requestMessage : requestMessages.getMessages()) {
					ResourceItemCommonDataId resourceItemCommonDataId = (ResourceItemCommonDataId) requestMessage
							.get(syncConfigurationParameter.RESOURCE_ITEM_COMMON_DATA_ID);
					messageMap.add(resourceItemCommonDataId, requestMessage);
				}
				List<ResourceItemCommonDataId> commonDataIdList = new ArrayList<>(messageMap.keySet());
				Collections.sort(commonDataIdList);

				for (ResourceItemCommonDataId itemCommonDataId : commonDataIdList) {

					List<RequestMessage> messagesForId = messageMap.get(itemCommonDataId);
					for (RequestMessage message : messagesForId) {

						// リソースクラスの取得
						ResourceMethodInvoker resourceMethod = getResourceManager().getResourceMethodByName(
								itemCommonDataId.getResourceName(), syncConfigurationParameter.ACTION_FOR_GETFORUPDATE,
								message);
						applyDefaultSynchronizer(resourceMethod);

						// ロックした共通データをリクエストメッセージに追加
						@SuppressWarnings("unchecked")
						List<ResourceItemCommonData> got = (List<ResourceItemCommonData>) resourceMethod
								.invoke(message);
						message.put(syncConfigurationParameter.RESOURCE_ITEM_COMMON_DATA, got);
					}
				}

			case NONE:
			default:
				break;
		}
	}

	/**
	 * リクエストメッセージごとの事前処理の前に、リソースに対してSynchronizerを設定します.<br/>
	 */
	@Override
	protected ResponseMessage processMessage(ResourceMethodInvoker resourceMethod, RequestMessage requestMessage)
			throws AbstractResourceException {

		applyDefaultSynchronizer(resourceMethod);
		return super.processMessage(resourceMethod, requestMessage);
	}

	/**
	 * リソースがsync対応で、独自のSynchronizerを持っていなければ、デフォルトを設定します.
	 *
	 * @param resourceMethod リソースメソッド
	 */
	private void applyDefaultSynchronizer(ResourceMethodInvoker resourceMethod) {

		Object resource = resourceMethod.getResource();
		if (resource instanceof SyncResource) {
			SyncResource syncResource = (SyncResource) resource;
			if (syncResource.getSynchronizer() == null) {
				syncResource.setSynchronizer(this.synchronizer);
			}
		}
	}

	/**
	 *
	 */
	private void setSyncAction(RequestMessageContainer requestMessages, String httpMethods) {
		for (RequestMessage mes : requestMessages.getMessages()) {
			// syncActionのチェック
			String syncActionStr = (String) mes.get(syncConfigurationParameter.SYNC_ACTION);
			// きちんと指定されているなら問題なし
			if(SyncAction.isSyncAction(syncActionStr)) {
				return;
			}

			// セット
			switch(httpMethods) {
				case "PUT":
					mes.put(syncConfigurationParameter.SYNC_ACTION, SyncAction.UPDATE.name());
					break;
				case "DELETE":
					mes.put(syncConfigurationParameter.SYNC_ACTION, SyncAction.DELETE.name());
					break;
				default:
					break;
			}
		}
	}

	/**
	 * リソース処理の事後処理として、ストレージID、同期時刻をリクエストから取り出し、レスポンスのコンテキスト情報として設定します.
	 *
	 * @param requestMessages リクエストメッセージコンテナ
	 * @param responseMessages レスポンスメッセージコンテナ
	 */
	@Override
	protected void postProcess(RequestMessageContainer requestMessages, ResponseMessageContainer responseMessages) {

		// ストレージIDを取り出し、コンテキスト情報として設定
		SyncRequestCommonData requestCommon = (SyncRequestCommonData) requestMessages
				.getContextData(syncConfigurationParameter.REQUEST_COMMON_DATA);
		responseMessages.putContextData(syncConfigurationParameter.STORAGE_ID, requestCommon.getStorageId(),
				MessageSource.PROCESSOR);

		// 同期時刻を取り出し、コンテキスト情報として設定(下り更新時はバッファを含める)
		Long syncTime = requestCommon.getSyncTime();

		// 本来はpreProcessでdownload or notをputContextDataしておいた方が良い
		String requestedAction = (String) requestMessages.getMessages().get(0).get(getMessageMetadata().ACTION);
		if (requestedAction.equals(syncConfigurationParameter.ACTION_FOR_DOWNLOAD)) {

			// 下り更新の同期時刻はバッファ時間を差し引く
			syncTime -= Long.valueOf(syncConfigurationParameter.BUFFER_TIME_FOR_DOWNLOAD);
		}

		responseMessages.putContextData(syncConfigurationParameter.SYNC_TIME, syncTime.toString(),
				MessageSource.PROCESSOR);
	}

	/**
	 * @return the synchronizer
	 */
	protected Synchronizer getSynchronizer() {
		return synchronizer;
	}

	/**
	 * @param synchronizer the synchronizer to set
	 */
	public void setSynchronizer(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}

	/**
	 * @return the syncConfigurationParameter
	 */
	protected SyncConfigurationParameter getSyncConfigurationParameter() {
		return syncConfigurationParameter;
	}

	/**
	 * @param syncConfigurationParameter the syncConfigurationParameter to set
	 */
	public void setSyncConfigurationParameter(SyncConfigurationParameter syncConfigurationParameter) {
		this.syncConfigurationParameter = syncConfigurationParameter;
	}

	/**
	 * @return the syncRequestCommonDataRepository
	 */
	protected SyncRequestCommonDataRepository getSyncRequestCommonDataRepository() {
		return syncRequestCommonDataRepository;
	}

	/**
	 * @param syncRequestCommonDataRepository the syncRequestCommonDataRepository to set
	 */
	public void setSyncRequestCommonDataRepository(SyncRequestCommonDataRepository syncRequestCommonDataRepository) {
		this.syncRequestCommonDataRepository = syncRequestCommonDataRepository;
	}
}
