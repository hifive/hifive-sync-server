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
package com.htmlhifive.sync.resource.separated;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.htmlhifive.sync.exception.ConflictException;
import com.htmlhifive.sync.exception.DuplicateElementException;
import com.htmlhifive.sync.resource.LockManager;
import com.htmlhifive.sync.resource.SyncProvider;
import com.htmlhifive.sync.resource.SyncRequestHeader;
import com.htmlhifive.sync.resource.SyncResource;
import com.htmlhifive.sync.resource.SyncResourceManager;
import com.htmlhifive.sync.resource.SyncResponse;
import com.htmlhifive.sync.resource.SyncResponseHeader;

/**
 * 専用の共通データサービスによる同期制御処理を行うリソースの抽象クラス.<br>
 * このクラスを継承してリソースクラスを作成すると、あるデータモデルの情報を同期対応リソースとして公開することができます.
 *
 * @author kishigam
 * @param <I> このリソースのIDの型
 * @param <E> このリソースのエレメントの型
 */
public abstract class SeparatedCommonDataSyncResource<I, E> implements SyncResource<E> {

	/**
	 * 同期を制御するための共通データサービス
	 */
	@Resource
	private SyncProvider syncProvider;

	/**
	 * リソースごとに決まるロック方式のマネージャオブジェクト.<br>
	 * リソースが生成される際に{@link SyncResourceManager } からセットされる.
	 */
	private LockManager lockManager;

	/**
	 * リクエストヘッダが指定する単一のリソースエレメントを取得します.<br>
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @return 取得したエレメントを含む同期レスポンスオブジェクト
	 */
	@Override
	public SyncResponse<E> get(SyncRequestHeader requestHeader) {

		SyncResponseHeader responseHeader = syncProvider.getCommonData(requestHeader);

		E element = null;
		try {
			lockManager.lock(requestHeader, responseHeader);
		} finally {
			element = getImpl(responseHeader.getResourceIdStr());
		}

		return new SyncResponse<>(responseHeader, element);
	}

	/**
	 * リクエストヘッダが指定し、指定時刻以降に更新された全リソースエレメントを取得します.<br>
	 *
	 * @param lastSyncTime 指定時刻
	 * @return 指定時刻以降に更新されたエレメントを含む同期レスポンスのリスト
	 */
	@Override
	public Set<SyncResponse<E>> getModifiedSince(SyncRequestHeader requestHeader) {

		Map<String, SyncResponseHeader> responseHeaderMap = syncProvider.getCommonDataModifiedSince(requestHeader);

		for (String resourceIdStr : responseHeaderMap.keySet()) {
			lockManager.lock(requestHeader, responseHeaderMap.get(resourceIdStr));
		}

		Map<String, E> elementMap = getImpl(responseHeaderMap.keySet(), requestHeader.getQueryMap());

		Set<SyncResponse<E>> responseSet = new HashSet<>();
		for (String targetResourceIdStr : responseHeaderMap.keySet()) {

			responseSet.add(new SyncResponse<>(responseHeaderMap.get(targetResourceIdStr), elementMap
					.get(targetResourceIdStr)));
		}

		return responseSet;
	}

	/**
	 * リクエストヘッダが指定するリソースエレメントを更新します.<br>
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param element 更新後のリソースエレメント
	 * @return 更新結果を含む同期レスポンス
	 * @throws ConflictException 衝突発生時の例外
	 */
	@Override
	public SyncResponse<E> put(SyncRequestHeader requestHeader, E element) throws ConflictException {

		SyncResponseHeader responseHeaderBeforUpdate = syncProvider.getCommonData(requestHeader);

		SyncResponseHeader responseHeaderAfterUpdate = null;

		if (!lockManager.canUpdate(requestHeader, responseHeaderBeforUpdate)) {

			// TODO:楽観ロック解決戦略の考慮→Managerへ
			throw new ConflictException("resource element has updated.", new SyncResponse<>(responseHeaderBeforUpdate,
					getImpl(responseHeaderBeforUpdate.getResourceIdStr())));
		}

		putImpl(responseHeaderBeforUpdate.getResourceIdStr(), element);
		responseHeaderAfterUpdate = syncProvider.saveUpdatedCommonData(requestHeader);

		lockManager.release(requestHeader, responseHeaderAfterUpdate);

		return new SyncResponse<>(responseHeaderAfterUpdate, element);
	}

	/**
	 * リクエストヘッダが指定するリソースエレメントを削除します.<br>
	 * このメソッドが担う同期共通データ管理上は、論理削除処理として同期メソッドをDELETEに設定し、更新します
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @return 削除結果を含む同期レスポンス
	 * @throws ConflictException 衝突発生時の例外
	 */
	@Override
	public SyncResponse<E> delete(SyncRequestHeader requestHeader) throws ConflictException {

		SyncResponseHeader responseHeaderBeforUpdate = syncProvider.getCommonData(requestHeader);

		SyncResponseHeader responseHeaderAfterUpdate = null;

		if (!lockManager.canUpdate(requestHeader, responseHeaderBeforUpdate)) {
			// TODO:楽観ロック解決戦略の考慮→Managerへ
			throw new ConflictException("resource element has updated.", new SyncResponse<>(responseHeaderBeforUpdate,
					getImpl(responseHeaderBeforUpdate.getResourceIdStr())));
		}

		deleteImpl(responseHeaderBeforUpdate.getResourceIdStr());
		responseHeaderAfterUpdate = syncProvider.saveUpdatedCommonData(requestHeader);

		lockManager.release(requestHeader, responseHeaderAfterUpdate);

		return new SyncResponse<>(responseHeaderAfterUpdate, null);
	}

	/**
	 * リソースエレメントを新規追加登録します.
	 *
	 * @param requestHeader 同期リクエストヘッダ
	 * @param newElement 追加したいリソースエレメント
	 * @return 追加結果を含む同期レスポンス
	 * @throws ConflictException キー重複衝突が発生
	 */
	@Override
	public SyncResponse<E> post(SyncRequestHeader requestHeader, E newElement) throws ConflictException {

		String newTargetResourceIdStr = null;

		try {
			newTargetResourceIdStr = postImpl(newElement);
		} catch (DuplicateElementException e) {

			SyncResponseHeader responseHeaderBeforeCreate = syncProvider.getCommonData(
					requestHeader.getDataModelName(), e.getDuplicateResourceIdStr());

			throw new ConflictException("duplicate key on target resource.", e, new SyncResponse<>(
					responseHeaderBeforeCreate, e.getDuplicateElement()));
		}

		SyncResponseHeader responseHeader = syncProvider.saveNewCommonData(requestHeader, newTargetResourceIdStr);

		return new SyncResponse<>(responseHeader, newElement);
	}

	/**
	 * 単一データGETメソッドのリソース別独自処理を実装する抽象メソッド.<br>
	 * サブクラスでは与えられたID文字列が示すリソースエレメントを返すようにこのメソッドを実装します.
	 *
	 * @param resourceIdStr リソースID文字列
	 * @return リソースエレメント
	 */
	protected abstract E getImpl(String resourceIdStr);

	/**
	 * 複数データGETメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではID文字列が示す全てのリソースエレメントの中で与えられたクエリ条件を満たすものを返すようにこのメソッドを実装します.
	 *
	 * @param resourceIdStrSet リソースID文字列のSet
	 * @param queryMap クエリMap
	 * @return リソースID文字列をKey、リソースエレメントをValueとするMap
	 */
	protected abstract Map<String, E> getImpl(Set<String> resourceIdStrSet, Map<String, String[]> queryMap);

	/**
	 * データPUTメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではID文字列が示すリソースエレメントを与えられたエレメントの内容で更新するようにこのメソッドを実装します.
	 *
	 * @param resourceIdStr リソースID文字列
	 * @param element 更新内容を含むリソースelement
	 */
	protected abstract void putImpl(String resourceIdStr, E element);

	/**
	 * データDELETEメソッドのリソース別独自処理を実装する抽象メソッド. <br>
	 * サブクラスではID文字列が示すリソースエレメントを削除するようにこのメソッドを実装します.<br>
	 *
	 * @param resourceIdStr リソースID文字列
	 */
	protected abstract void deleteImpl(String resourceIdStr);

	/**
	 * POSTメソッドのリソース別独自処理. <br>
	 * 追加されたデータのリソースIDを返す.
	 *
	 * @param newElement 生成内容を含むリソースelement
	 * @return 採番されたリソースID
	 * @throws DuplicateElementException 追加しようとしたデータが既に存在する場合
	 */
	protected abstract String postImpl(E newElement) throws DuplicateElementException;

	/**
	 * リソースID文字列からリソースのIDオブジェクトを導出し、返します. <br>
	 * サブクラスでは、同期リソースとしてのリソースID文字列と実際のデータ識別子を相互変換するようにこのメソッドを実装します.
	 *
	 * @param resourceIdStr リソースID文字列
	 * @return IDオブジェクト
	 */
	protected abstract I resolveResourceId(String resourceIdStr);

	/**
	 * リソースのIDオブジェクトからリソースID文字列を導出し、返します. <br>
	 * サブクラスでは、同期リソースとしてのリソースID文字列と実際のデータ識別子を相互変換するようにこのメソッドを実装します.
	 *
	 * @param id IDオブジェクト
	 * @return リソースID文字列
	 */
	protected abstract String generateNewResourceIdStr(I id);

	/**
	 * このリソースのエレメント型を返します.
	 *
	 * @return エレメントの型を表すClassオブジェクト
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<E> getElementType() {

		// この抽象クラスの型を取得
		ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();

		// この抽象クラスで2つ目の型変数に指定されているのがエレメントの型
		return (Class<E>) thisType.getActualTypeArguments()[1];
	}

	/**
	 * @param lockManager セットする lockManager
	 */
	@Override
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
}