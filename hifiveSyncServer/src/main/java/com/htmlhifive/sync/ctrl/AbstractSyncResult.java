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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.htmlhifive.sync.resource.SyncResponse;

/**
 * クライアントからの同期リクエストに対する同期実行結果を保持するデータオブジェクトの抽象クラス.<br>
 * 具体的な同期リクエストの種類に応じたサブクラスを定義することができます.
 *
 * @author kishigam
 */
public abstract class AbstractSyncResult {

	/**
	 * クライアントのストレージID
	 */
	private String storageId;

	/**
	 * 同期結果タイプ.
	 */
	private SyncResultType resultType;

	/**
	 * 同期実行時刻.<br>
	 */
	private long currentSyncTime;

	/**
	 * 同期結果のSet.<br>
	 * 順序を維持するためLinkedHashSetクラスを使用する.
	 */
	private Set<SyncResponse<?>> resultDataSet = new LinkedHashSet<>();

	/**
	 * ストレージIDを指定して同期結果オブジェクトを生成します. <br>
	 * 同期時刻が設定されます.
	 *
	 * @param storageId クライアントのストレージID
	 */
	public AbstractSyncResult(String storageId) {

		this.storageId = storageId;
		setGeneratedSyncTime();
	}

	/**
	 * 同期時刻を設定します.<br>
	 * 各サブクラスが表す同期リクエストの種類ごとに時刻設定ルールを適用することができます.
	 */
	public abstract void setGeneratedSyncTime();

	/**
	 * 指定された同期レスポンスオブジェクトを、このオブジェクトが保持しているセットに加えます.
	 *
	 * @param response 同期レスポンスオブジェクト
	 */
	public void addResultData(SyncResponse<?> response) {

		resultDataSet.add(response);
	}

	/**
	 * 指定された同期レスポンスオブジェクトのセットの内容を、このオブジェクトが保持しているセットに全て加えます.
	 *
	 * @param responseSet 同期レスポンスオブジェクトのSet
	 */
	public void addAllResultData(Set<? extends SyncResponse<?>> responseSet) {

		resultDataSet.addAll(responseSet);
	}

	/**
	 * このオブジェクトが保持する同期結果のSetをクリアします.
	 */
	public void clearResultSet() {
		this.resultDataSet.clear();
	}

	/**
	 * @return storageId
	 */
	public String getStorageId() {
		return storageId;
	}

	/**
	 * @param storageId セットする storageId
	 */
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	/**
	 * @return resultType
	 */
	public SyncResultType getResultType() {
		return resultType;
	}

	/**
	 * @param resultType セットする resultType
	 */
	public void setResultType(SyncResultType resultType) {
		this.resultType = resultType;
	}

	/**
	 * @return currentSyncTime
	 */
	public long getCurrentSyncTime() {
		return currentSyncTime;
	}

	/**
	 * @param currentSyncTime セットする currentSyncTime
	 */
	public void setCurrentSyncTime(long currentSyncTime) {
		this.currentSyncTime = currentSyncTime;
	}

	/**
	 * @return resultDataSet
	 */
	public Set<SyncResponse<?>> getResultDataSet() {
		return Collections.unmodifiableSet(resultDataSet);
	}

	/**
	 * @param resultDataSet セットする resultDataSet
	 */
	public void setResultDataSet(Set<SyncResponse<?>> resultDataSet) {
		this.resultDataSet = resultDataSet;
	}
}