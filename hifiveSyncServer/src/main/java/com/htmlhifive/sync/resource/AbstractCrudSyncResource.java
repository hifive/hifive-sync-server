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
package com.htmlhifive.sync.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.BadRequestException;
import com.htmlhifive.resourcefw.exception.ConflictException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.exception.LockedException;
import com.htmlhifive.resourcefw.exception.NotFoundException;
import com.htmlhifive.resourcefw.exception.NotModifiedException;
import com.htmlhifive.resourcefw.exception.ServiceUnavailableException;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.message.RequestMessageUtil;
import com.htmlhifive.resourcefw.resource.AbstractCrudResource;
import com.htmlhifive.sync.config.SyncConfigurationParameter;
import com.htmlhifive.sync.exception.SyncConflictException;
import com.htmlhifive.sync.exception.SyncDuplicateIdConflictException;
import com.htmlhifive.sync.exception.SyncUpdateConflictException;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;
import com.htmlhifive.sync.resource.common.SyncAction;
import com.htmlhifive.sync.resource.update.UpdateStrategy;
import com.htmlhifive.sync.service.SyncRequestCommonData;

/**
 * JPA EntityのCRUDアクションにsync機能を付加する抽象リソース実装.<br/>
 * リソース操作(アクション)に対するsync動作の汎用実装を提供します.<br/>
 * この汎用実装を使用するためには、リソースアイテムと、永続化のためのエンティティが同じ型である必要があります.
 *
 * @author kishigam
 * @param <T>　リソースアイテム型(JPA Entity)
 */
public abstract class AbstractCrudSyncResource<T> extends AbstractCrudResource<T> implements SyncResource {

	/**
	 * 下り更新結果オブジェクト(Map)に含むリソースアイテムデータのキー
	 */
	private static final String DOWNLOAD_RESULT_ITEM_KEY = "item";

	/**
	 * 下り更新結果オブジェクト(Map)に含むリソースアイテム共通データのキー
	 */
	private static final String DOWNLOAD_RESULT_COMMON_DATA_KEY = "resourceItemCommonData";

	/**
	 * sync機能におけるバージョン管理を行うsynchronizerオブジェクト.
	 */
	private Synchronizer synchronizer;

	/**
	 * 競合発生時の解決を行う更新戦略オブジェクト.
	 */
	private UpdateStrategy updateStrategy;

	/**
	 * {@link Synchronizer Synchronizer}を使用して同期上り更新を実行します.<br/>
	 * リクエストメッセージに含まれるsyncアクションに応じて、クライアントのデータをサーバに対して同期します.<br/>
	 * 同期に失敗した場合、{@link SyncConflictException}をスローします.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return 上り更新結果
	 */
	public Object upload(RequestMessage requestMessage) throws BadRequestException, ServiceUnavailableException,
			LockedException, NotFoundException, SyncConflictException {

		// リソースアイテムIDのチェック
		ResourceItemCommonDataId resourceItemIdObj = (ResourceItemCommonDataId) requestMessage.get(synchronizer
				.getSyncConfigurationParameter().RESOURCE_ITEM_COMMON_DATA_ID);
		if (resourceItemIdObj.getResourceItemId() == null || resourceItemIdObj.getResourceItemId().isEmpty()) {
			// ラップしてスローし、ResourceExceptionHandlerに処理させる
			throw new GenericResourceException(new BadRequestException("Resource item id is needed.", requestMessage));
		}

		// リクエスト情報からリソースアイテム共通データを生成
		ResourceItemCommonData clientItemCommon = createResourceItemCommonData(requestMessage);

		ResourceItemCommonData currentItemCommon = getCurrentItemCommon(requestMessage);

		switch (clientItemCommon.getSyncAction()) {
			case CREATE:
				currentItemCommon = doCreate(requestMessage, clientItemCommon, currentItemCommon);
				break;

			case UPDATE:
			case DELETE:
				currentItemCommon = doUpdateOrDelete(requestMessage, clientItemCommon, currentItemCommon);
				break;

			default:
				throw new GenericResourceException("Unknown sync action.");
		}

		// sync共通データの更新
		synchronizer.modify(currentItemCommon);

		return currentItemCommon;
	}

	/**
	 * 指定されたリクエストメッセージから上り更新内容を反映したリソースアイテム共通データを取得し、返します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return リソースアイテム共通データ
	 */
	private ResourceItemCommonData createResourceItemCommonData(RequestMessage requestMessage)
			throws BadRequestException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		ResourceItemCommonData common = new ResourceItemCommonData(
				(ResourceItemCommonDataId) requestMessage.get(configParam.RESOURCE_ITEM_COMMON_DATA_ID));

		common.setTargetItemId(getId(requestMessage));

		// syncActionのチェック
		String syncActionStr = (String) requestMessage.get(configParam.SYNC_ACTION);
		if (!isSyncAction(syncActionStr)) {
			throw new BadRequestException("No such sync action. : sync action = " + syncActionStr, requestMessage);
		}

		common.setSyncAction(SyncAction.valueOf(syncActionStr));

		Object lastModifiedObj = requestMessage.get(configParam.LAST_MODIFIED);
		if (lastModifiedObj != null) {
			common.setLastModified(Long.parseLong((String) lastModifiedObj));
		}

		return common;
	}

	/**
	 * 使用可能なSyncActionを表す文字列であればtrueを返します.
	 *
	 * @param syncActionStr syncActionを表す文字列
	 * @return SyncActionであればtrue
	 */
	private boolean isSyncAction(String syncActionStr) {

		if (syncActionStr == null)
			return false;

		for (SyncAction syncAction : EnumSet.allOf(SyncAction.class)) {
			if (syncAction.toString().equals(syncActionStr))
				return true;
		}

		return false;
	}

	/**
	 * 指定された情報から、リソースアイテムの共通データを取得して返します.<br/>
	 * 同期制御により先に悲観的ロックが行われている場合、そこで取得された共通データはリクエストメッセージに保持されています.<br/>
	 * クエリによる複数件取得はできません.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return リソースアイテム共通データ
	 */
	@SuppressWarnings("unchecked")
	private ResourceItemCommonData getCurrentItemCommon(RequestMessage requestMessage) throws SyncConflictException,
			NotFoundException, BadRequestException, LockedException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		ResourceItemCommonData currentItemCommon;

		// CREATEの時は新規生成
		SyncAction syncAction = SyncAction.valueOf((String) requestMessage.get(configParam.SYNC_ACTION));
		if (syncAction == SyncAction.CREATE) {
			ResourceItemCommonDataId commonDataId = (ResourceItemCommonDataId) requestMessage
					.get(configParam.RESOURCE_ITEM_COMMON_DATA_ID);
			currentItemCommon = synchronizer.getNew(commonDataId);

			// 共通データでIDの重複が発生
			if (currentItemCommon.getSyncAction() == SyncAction.DUPLICATE) {
				throw new SyncDuplicateIdConflictException(currentItemCommon.getSyncAction().toString(),
						findById(requestMessage), configParam, requestMessage);
			}

			return currentItemCommon;
		}

		// CREATE以外は取得

		// 事前にで共通データをgetForUpdateしていたらそれを使用
		Object gotCommonDataListObj = requestMessage.get(configParam.RESOURCE_ITEM_COMMON_DATA);
		if (gotCommonDataListObj != null) {

			List<ResourceItemCommonData> commonList = (List<ResourceItemCommonData>) gotCommonDataListObj;
			// 空の場合はNotFound
			if (commonList.isEmpty()) {
				throw new NotFoundException("Sync target resource item is not found.", requestMessage);
			}
			return commonList.get(0);

		}

		// 取得されていない場合はここで取得
		ResourceItemCommonDataId commonDataId = (ResourceItemCommonDataId) requestMessage
				.get(configParam.RESOURCE_ITEM_COMMON_DATA_ID);

		currentItemCommon = synchronizer.getForUpdate(commonDataId);

		// 空の場合はNotFound
		if (currentItemCommon == null) {
			throw new NotFoundException("Sync target resource item is not found.", requestMessage);
		}

		return currentItemCommon;
	}

	/**
	 * 新しいリソースアイテムをサーバに同期する処理を実行し、処理後のリソースアイテム共通データを返します.<br/>
	 * 既に保存されているリソースアイテムが存在する場合は{@link SyncConflictException}をスローします.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param clientItemCommon リクエストされた対象リソースアイテムの共通データ
	 * @param currentItemCommon サーバ管理されている対象リソースアイテムの共通データ
	 * @return リソースアイテム共通データ
	 */
	private ResourceItemCommonData doCreate(RequestMessage requestMessage, ResourceItemCommonData clientItemCommon,
			ResourceItemCommonData currentItemCommon) throws BadRequestException, ServiceUnavailableException,
			LockedException, NotFoundException, SyncConflictException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		SyncRequestCommonData requestCommon = (SyncRequestCommonData) requestMessage
				.get(configParam.REQUEST_COMMON_DATA);

		String targetItemId = null;
		try {
			targetItemId = insert(requestMessage);
		} catch (ConflictException e) {
			// syncのID重複例外に変換
			throw new SyncDuplicateIdConflictException(e, findById(requestMessage), configParam, requestMessage);
		}

		currentItemCommon.setTargetItemId(targetItemId);
		currentItemCommon.modify(clientItemCommon.getSyncAction(), requestCommon.getSyncTime());

		return currentItemCommon;
	}

	/**
	 * リソースアイテムの更新をサーバに同期する処理を実行し、処理後のリソースアイテム共通データを返します.<br/>
	 * 先に他のリクエストにより更新されていた場合、{@link UpdateStrategy}による競合解決を行います.<br/>
	 * その結果、リクエストとは異なるアクションが実行されることがあります.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param clientItemCommon リクエストされた対象リソースアイテムの共通データ
	 * @param currentItemCommon サーバ管理されている対象リソースアイテムの共通データ
	 * @return リソースアイテム共通データ
	 */
	private ResourceItemCommonData doUpdateOrDelete(RequestMessage requestMessage,
			ResourceItemCommonData clientItemCommon, ResourceItemCommonData currentItemCommon)
			throws BadRequestException, NotFoundException, LockedException, SyncConflictException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		SyncRequestCommonData requestCommon = (SyncRequestCommonData) requestMessage
				.get(configParam.REQUEST_COMMON_DATA);

		// 競合検出と解決の試行
		SyncAction resolvedSyncAction = clientItemCommon.getSyncAction();
		if (synchronizer.isConflicted(clientItemCommon, currentItemCommon, requestCommon)) {

			T clientItem = RequestMessageUtil.extractObject(requestMessage, getItemType(), getIdFieldName(),
					getId(requestMessage));

			Object currentItem = findById(requestMessage);

			// UpdateStrategyの設定がない場合にデフォルトを使用するため必ずgetUpdateStrategyを使用すること
			resolvedSyncAction = getUpdateStrategy().resolveConflict(clientItemCommon, clientItem, currentItemCommon,
					currentItem);

			if (resolvedSyncAction == SyncAction.CONFLICT) {
				throw new SyncUpdateConflictException(resolvedSyncAction.toString(), currentItem, configParam,
						requestMessage);
			}
		}

		// 決定した同期アクションに応じたリソースアイテムのアクションを実行
		switch (resolvedSyncAction) {
			case NONE:
				break;
			case UPDATE:
				update(requestMessage);
				break;
			case DELETE:
				remove(requestMessage);
				break;
			default:
				throw new GenericResourceException("Unknown resolved SyncAction. : " + resolvedSyncAction);
		}

		currentItemCommon.modify(resolvedSyncAction, requestCommon.getSyncTime());

		return currentItemCommon;
	}

	/**
	 * {@link Synchronizer Synchronizer}を使用して同期下り更新を実行します.<br/>
	 * クライアントが持っているデータの最終更新時刻以降に更新されたサーバデータがあれば、それをクライアントに返すことで同期します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return 下り更新結果の
	 * @throws AbstractResourceException
	 */
	public Object download(RequestMessage requestMessage) throws AbstractResourceException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		// lastModifiedメタデータの時刻以降に更新されているリソースアイテムが取得対象になる
		long modifiedSince = 0L;
		Object lastModifiedObj = requestMessage.get(configParam.LAST_MODIFIED);
		try {
			if (lastModifiedObj != null) {
				modifiedSince = Long.parseLong((String) lastModifiedObj);
			}
		} catch (NumberFormatException e) {
			throw new BadRequestException("Failed to parse Last modified time. : " + (String) lastModifiedObj,
					requestMessage);
		}

		String resourceName = ((ResourceItemCommonDataId) requestMessage.get(configParam.RESOURCE_ITEM_COMMON_DATA_ID))
				.getResourceName();

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		// queryがあればdownloadByQuery,なければdownloadById
		Object query = requestMessage.get(messageMetadata.QUERY);
		if (query == null) {
			return downloadById(resourceName, modifiedSince, requestMessage);
		}

		return downloadByQuery(resourceName, modifiedSince, requestMessage);
	}

	/**
	 * IDでリソースアイテムを指定して下り更新を実行します.<br/>
	 * 指定した時刻以降に更新されたアイテムのみ対象となり、そうでない場合は{@link NotModifiedException}がスローされます.
	 *
	 * @param resourceName リソース名
	 * @param modifiedSince クライアントデータの最終更新時刻
	 * @param requestMessage リクエストメッセージ
	 * @return 下り更新結果のリソースアイテムオブジェクト
	 * @throws AbstractResourceException
	 */
	@SuppressWarnings("unchecked")
	private Object downloadById(String resourceName, long modifiedSince, RequestMessage requestMessage)
			throws AbstractResourceException {

		// リソースアイテムがなければNotFound
		if (!(boolean) exists(requestMessage).get("exists")) {
			throw new NotFoundException("Sync target resource item is not found.", requestMessage);
		}

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		List<ResourceItemCommonData> commonList;

		Object gotCommonDataListObj = requestMessage.get(configParam.RESOURCE_ITEM_COMMON_DATA);
		if (gotCommonDataListObj != null) {

			// 事前に共通データをgetForUpdateしていたらそれを使用
			commonList = (List<ResourceItemCommonData>) gotCommonDataListObj;

		} else {

			// なければここで取得する
			List<String> targetItemIdList = new ArrayList<>();
			targetItemIdList.add(getId(requestMessage));

			commonList = synchronizer.getModified(resourceName, targetItemIdList, modifiedSince);
		}

		// modifiedでなければNotModified
		if (commonList.isEmpty()) {
			throw new NotModifiedException(requestMessage);
		}

		// 戻り値はリソースアイテムデータとその共通データを含むObject(Map)

		Map<String, Object> result = new HashMap<>();
		ResourceItemCommonData common = commonList.get(0);
		result.put(DOWNLOAD_RESULT_COMMON_DATA_KEY, common);

		// 削除済みであればIDだけのリソースアイテムを返す
		if (common.getSyncAction() == SyncAction.DELETE) {
			result.put(DOWNLOAD_RESULT_ITEM_KEY, createDeletedItem(common));
		} else {
			// リソースアイテムを取得して返す
			// findByIdはID未指定時にlistアクションの結果を返すが、この時点ではあり得ない
			// リソースアイテム型が返ることを前提とする
			result.put(DOWNLOAD_RESULT_ITEM_KEY, findById(requestMessage));
		}

		return result;
	}

	/**
	 * IDでリソースアイテムを指定して下り更新を実行します.<br/>
	 * 指定した時刻以降に更新されたアイテムのみ対象となるため、結果が空の場合があります.<br/>
	 *
	 * @param resourceName リソース名
	 * @param modifiedSince クライアントデータの最終更新時刻
	 * @param requestMessage リクエストメッセージ
	 * @return 下り更新結果のリソースアイテムオブジェクトのコレクション
	 */
	@SuppressWarnings("unchecked")
	private Object downloadByQuery(String resourceName, long modifiedSince, RequestMessage requestMessage)
			throws BadRequestException, LockedException, NotModifiedException, NotFoundException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		List<ResourceItemCommonData> modifiedCommonList;

		Object gotCommonDataListObj = requestMessage.get(configParam.RESOURCE_ITEM_COMMON_DATA);
		if (gotCommonDataListObj != null) {

			// 事前に共通データをgetForUpdateしていたらそれを使用
			modifiedCommonList = (List<ResourceItemCommonData>) gotCommonDataListObj;
		} else {

			// modified対象リソースアイテムを特定するためにクエリー実行し、対象リソースアイテムのIDを収集する
			List<String> targetItemIdList = new ArrayList<>();
			Object itemObj = findByQuery(requestMessage);

			// リソースアイテム型のリストでない場合はそのままリターンする
			if (!(itemObj instanceof List)) {
				return itemObj;
			}
			List<?> itemObjList = (List<?>) itemObj;
			if (itemObjList.isEmpty() || !getItemType().isAssignableFrom(itemObjList.get(0).getClass())) {
				return itemObj;
			}

			for (Object item : itemObjList) {
				targetItemIdList.add(getIdFieldValue(getItemType().cast(item)));
			}

			modifiedCommonList = synchronizer.getModified(resourceName, targetItemIdList, modifiedSince);
		}

		// 戻り値はリソースアイテムデータとその共通データを含むObject(Map)のList
		ArrayList<Map<String, Object>> resultList = new ArrayList<>();
		for (ResourceItemCommonData common : modifiedCommonList) {

			Map<String, Object> result = new HashMap<>();
			result.put(DOWNLOAD_RESULT_COMMON_DATA_KEY, common);

			// 削除済みであればIDだけのリソースアイテムを返す
			if (common.getSyncAction() == SyncAction.DELETE) {
				result.put(DOWNLOAD_RESULT_ITEM_KEY, createDeletedItem(common));
			} else { // IDを含むRequestMessageはないため、リポジトリを直接呼び出す
				T item = getRepository().findOne(common.getTargetItemId());
				result.put(DOWNLOAD_RESULT_ITEM_KEY, item);
			}

			resultList.add(result);
		}

		return resultList;
	}

	/**
	 * 削除済のリソースアイテムを表す、IDだけを持つオブジェクトを返します.
	 *
	 * @param common 対象リソースアイテムの共通データ
	 * @return 対象リソースアイテムインスタンス
	 */
	private T createDeletedItem(ResourceItemCommonData common) {
		Class<T> itemType = getItemType();

		BeanWrapper deletedWrapper = PropertyAccessorFactory
				.forBeanPropertyAccess(BeanUtils.instantiateClass(itemType));
		deletedWrapper.setPropertyValue(getIdFieldName(), common.getTargetItemId());

		return itemType.cast(deletedWrapper.getWrappedInstance());
	}

	/**
	 * リソースアイテム共通データを悲観的ロックによって取得します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return リソースアイテム共通データのリスト
	 * @throws AbstractResourceException
	 */
	@Override
	public List<ResourceItemCommonData> getForUpdate(RequestMessage requestMessage) throws AbstractResourceException {

		SyncConfigurationParameter configParam = synchronizer.getSyncConfigurationParameter();

		// lastModifiedメタデータの時刻以降に更新されているリソースアイテム共通データが取得対象になる
		long modifiedSince = 0L;
		Object lastModifiedObj = requestMessage.get(configParam.LAST_MODIFIED);
		try {
			if (lastModifiedObj != null) {
				modifiedSince = Long.parseLong((String) lastModifiedObj);
			}
		} catch (NumberFormatException e) {
			throw new BadRequestException("Failed to parse Last modified time. : " + (String) lastModifiedObj,
					requestMessage);
		}

		String resourceName = ((ResourceItemCommonDataId) requestMessage.get(configParam.RESOURCE_ITEM_COMMON_DATA_ID))
				.getResourceName();

		MessageMetadata messageMetadata = requestMessage.getMessageMetadata();

		Object query = requestMessage.get(messageMetadata.QUERY);
		if (query == null) {
			return getByIdForUpdate(resourceName, modifiedSince, requestMessage);
		}

		return getByQueryForUpdate(resourceName, modifiedSince, requestMessage);
	}

	/**
	 * IDでリソースアイテムを指定して悲観的ロックによる取得を実行します.<br/>
	 * 指定した時刻以降に更新されたアイテムのみ対象となり、そうでない場合はnullが返されます。
	 *
	 * @param resourceName リソース名
	 * @param modifiedSince クライアントデータの最終更新時刻
	 * @param requestMessage リクエストメッセージ
	 * @return リソースアイテムオブジェクト
	 * @throws AbstractResourceException
	 */
	private List<ResourceItemCommonData> getByIdForUpdate(String resourceName, long modifiedSince,
			RequestMessage requestMessage) throws AbstractResourceException {

		List<String> targetItemIdList = new ArrayList<>();

		// IDをリクエストメッセージから取得
		targetItemIdList.add(getId(requestMessage));

		// 高々1件
		return synchronizer.getModifiedForUpdate(resourceName, targetItemIdList, modifiedSince);
	}

	/**
	 * IDでリソースアイテムを指定して下り更新を実行します.<br/>
	 * 指定した時刻以降に更新されたアイテムのみ対象となるため、結果が空の場合があります.<br/>
	 *
	 * @param resourceName リソース名
	 * @param modifiedSince クライアントデータの最終更新時刻
	 * @param requestMessage リクエストメッセージ
	 * @return リソースアイテムオブジェクトのリスト
	 * @throws AbstractResourceException
	 */
	private List<ResourceItemCommonData> getByQueryForUpdate(String resourceName, long modifiedSince,
			RequestMessage requestMessage) throws AbstractResourceException {

		Object itemObj = findByQuery(requestMessage);

		// リソースアイテム型のリストでない場合は空でリターン
		if (!(itemObj instanceof List)) {
			return Collections.emptyList();
		}
		List<?> itemObjList = (List<?>) itemObj;
		if (itemObjList.isEmpty() || !getItemType().isAssignableFrom(itemObjList.get(0).getClass())) {
			return Collections.emptyList();
		}

		List<String> targetItemIdList = new ArrayList<>();
		for (Object item : itemObjList) {
			targetItemIdList.add(getIdFieldValue(getItemType().cast(item)));
		}

		return synchronizer.getModifiedForUpdate(resourceName, targetItemIdList, modifiedSince);
	}

	/**
	 * 同期に使用するシンクロナイザーオブジェクトを返します.
	 *
	 * @return synchronizer
	 */
	@Override
	public Synchronizer getSynchronizer() {
		return this.synchronizer;
	}

	/**
	 * このリソースに、同期に使用するシンクロナイザーオブジェクトを設定します.
	 */
	@Override
	public void setSynchronizer(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}

	@Override
	public UpdateStrategy getUpdateStrategy() {

		if (this.updateStrategy == null)
			return this.synchronizer.getDefaultUpdateStrategy();

		return updateStrategy;
	}

	@Override
	public void setUpdateStrategy(UpdateStrategy updateStrategy) {

		this.updateStrategy = updateStrategy;
	}
}
