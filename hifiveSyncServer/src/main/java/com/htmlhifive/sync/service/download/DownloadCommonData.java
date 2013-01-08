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
package com.htmlhifive.sync.service.download;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.htmlhifive.sync.service.SyncCommonData;

/**
 * 下り更新に関する共通情報を保持するデータクラス(エンティティ).
 *
 * @author kishigam
 */
public class DownloadCommonData implements SyncCommonData, Serializable {

	private static final long serialVersionUID = -6156228658566046747L;

	/**
	 * クライアントのストレージID.<br>
	 * このフィールドはクライアントの初回アクセス時のみクライアントへのレスポンスに含みます.<br>
	 * 未設定のときレスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonSerialize}を追加しています.
	 */
	private String storageId;

	/**
	 * 最終下り更新時刻.
	 */
	private long lastDownloadTime;

	/**
	 * この更新リクエストが実行される時刻.<br>
	 * このフィールドはクライアントとのリクエスト、レスポンスに含みません.<br>
	 * リクエスト、レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonIgnore}を追加しています.
	 */
	private long syncTime;

	/**
	 * フレームワーク、ライブラリが使用するプライベートデフォルトコンストラクタ.
	 */
	@SuppressWarnings("unused")
	private DownloadCommonData() {
	}

	/**
	 * 指定されたストレージIDを持つ下り更新共通データを生成します.
	 *
	 * @param storageId ストレージID
	 */
	public DownloadCommonData(String storageId) {
		this.storageId = storageId;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof DownloadCommonData))
			return false;

		DownloadCommonData downloadCommon = (DownloadCommonData) obj;

		return new EqualsBuilder().append(this.storageId, downloadCommon.storageId)
				.append(this.lastDownloadTime, downloadCommon.lastDownloadTime)
				.append(this.syncTime, downloadCommon.syncTime).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.storageId).append(this.lastDownloadTime).append(this.syncTime)
				.hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * nullのときクライアントへのレスポンスから除外するため、{@link JsonSerialize}を追加しています.
	 *
	 * @return storageId
	 */
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	@Override
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
	 * @return lastDownloadTime
	 */
	public long getLastDownloadTime() {
		return lastDownloadTime;
	}

	/**
	 * @param lastDownloadTime セットする lastDownloadTime
	 */
	public void setLastDownloadTime(long lastDownloadTime) {
		this.lastDownloadTime = lastDownloadTime;
	}

	/**
	 * リクエスト、レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonIgnore}を追加しています.
	 *
	 * @return syncTime
	 */
	@JsonIgnore
	@Override
	public long getSyncTime() {
		return syncTime;
	}

	/**
	 * @param syncTime セットする syncTime
	 */
	public void setSyncTime(long syncTime) {
		this.syncTime = syncTime;
	}
}
