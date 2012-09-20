/*
 * Copyright (C) 2012 NS Solutions Corporation
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

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.exception.DuplicateElementException;
import com.htmlhifive.sync.jsonctrl.JsonDataConvertor;
import com.htmlhifive.sync.jsonctrl.download.DownloadRequestMessage;
import com.htmlhifive.sync.jsonctrl.upload.UploadRequestMessage;
import com.htmlhifive.sync.resource.SyncRequestHeader;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.SyncResourceManager;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * リソースに対する同期処理を実行するサービス実装. <br>
 * TODO: JSON形式の抽象化
 *
 * @author kishigam
 */
@Component
public class SynchronizerImpl implements Synchronizer {

	@Resource
	private SyncResourceManager resourceManager;

	/**
	 * 下り更新を実行します.<br>
	 * 前回同期時刻以降、messageで指定したリソースにおける更新データをGETします.
	 *
	 * @param storageId クライアントのストレージID
	 * @param requestMessages 下り更新のリクエストメッセージのリスト
	 * @return 下り更新結果オブジェクト
	 */
	@Override
	public SyncDownloadResult syncDownload(final String storageId,
			final List<? extends DownloadRequestMessage> requestMessages) {

		// 同期結果オブジェクトを生成、同期実行時刻(＝リクエスト時刻)が設定される
		SyncDownloadResult downloadResult = new SyncDownloadResult(storageId);

		// リクエストに含まれるMessageごとに処理
		for (DownloadRequestMessage message : requestMessages) {

			// 同期リクエストヘッダ作成
			SyncRequestHeader requestHeader = message.createHeader(storageId, downloadResult.getCurrentSyncTime());

			// synchronizerへリクエスト発行
			SyncResource<?> resource = resourceManager.locateSyncResource(requestHeader.getDataModelName());

			Set<? extends SyncResponse<?>> responseSet = resource.getModifiedSince(requestHeader);

			// 結果は他のリクエストのものとマージされる
			downloadResult.addAllResultData(responseSet);
		}

		// OK
		downloadResult.setResultType(SyncResultType.OK);
		return downloadResult;
	}

	/**
	 * 上り更新を実行します.<br>
	 * 対象のリソースを判断し、リソースエレメントの更新内容に応じてリクエストを発行します.
	 *
	 * @param storageId クライアントのストレージID
	 * @param requestMessages 上り更新のリクエストメッセージのリスト
	 * @return 上り更新結果オブジェクト
	 */
	@Override
	public SyncUploadResult syncUpload(String storageId, List<? extends UploadRequestMessage<?>> requestMessages) {

		SyncUploadResult result = new SyncUploadResult(storageId);
		result.setResultType(SyncResultType.OK);

		for (UploadRequestMessage<?> requestMessage : requestMessages) {

			// ヘッダの生成
			SyncRequestHeader requestHeader = requestMessage.createtHeader(storageId, result.getCurrentSyncTime());

			try {
				SyncResource<?> resource = resourceManager.locateSyncResource(requestHeader.getDataModelName());
				SyncResponse<?> response = doSyncUpload(resource, requestHeader, requestMessage.getElement());

				// 一度CONFLICTEDになったらOKになることはなく、OKの結果データは不要
				// 他のCONFLICTED更新を検知するためメッセージの処理は継続
				if (result.getResultType() == SyncResultType.OK) {
					result.addResultData(response);
				}

			} catch (ConflictException e) {

				// 全てのリクエスト処理結果をロールバックする
				// (ここで例外を止めてしまうため、Transactionalアノテーションの"rollbackFor"が効かない？)
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

				// 最初のCONFLICTED(UPDATED)のときにOKデータを除去する
				if (result.getResultType() == SyncResultType.OK) {
					result.clearResultSet();
				}

				result.addResultData(e.getConflictedResponse());

				// CREATEのキー重複が発生した場合は以降のメッセージ処理は行わず、中断
				if (e.getCause() instanceof DuplicateElementException) {
					result.setResultType(SyncResultType.DUPLICATEDID);
					break;
				}
				result.setResultType(SyncResultType.UPDATED);
			}
		}

		return result;
	}

	/**
	 * リソースに対してアクションに応じたリクエストメソッドを呼び出すヘルパー.<br>
	 * エレメント型を固定することで、JSONからの型変換を行ったエレメントをリソースに渡すことができます.
	 *
	 * @param resource リソース
	 * @param requestHeader 上り更新リクエストヘッダ
	 * @param elementObj エレメント(DELETEのとき等、存在しない場合null)
	 * @return 同期レスポンスオブジェクト
	 */
	private <E> SyncResponse<E> doSyncUpload(SyncResource<E> resource, SyncRequestHeader requestHeader,
			Object elementObj) throws ConflictException {

		switch (requestHeader.getSyncMethod()) {
			case POST:
				return resource.post(requestHeader,
						JsonDataConvertor.convertJSONToElement(elementObj, resource.getElementType()));
			case PUT:
				return resource.put(requestHeader,
						JsonDataConvertor.convertJSONToElement(elementObj, resource.getElementType()));
			case DELETE:
				return resource.delete(requestHeader);
			default:
				throw new BadRequestException("undefined action has called.");
		}

	}
}