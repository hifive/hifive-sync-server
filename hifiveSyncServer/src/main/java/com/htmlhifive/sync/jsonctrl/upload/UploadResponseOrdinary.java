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
package com.htmlhifive.sync.jsonctrl.upload;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.service.SyncStatus;

/**
 * クライアントからの上り更新リクエストに対する正常終了時のレスポンスデータ.
 *
 * @author kishigam
 */
public class UploadResponseOrdinary {

	/**
	 * 上り更新処理を実行した時刻.
	 */
	private long lastUploadTime;

	/**
	 * 同期ステータスオブジェクトから上り更新レスポンスを生成します.
	 *
	 * @param statusAfterDownload 同期ステータスオブジェクト
	 */
	public UploadResponseOrdinary(SyncStatus statusAfterDownload) {

		this.lastUploadTime = statusAfterDownload.getLastUploadTime();
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (!(obj instanceof UploadResponseOrdinary))
			return false;

		UploadResponseOrdinary req = (UploadResponseOrdinary) obj;

		return new EqualsBuilder().append(this.lastUploadTime, req.lastUploadTime).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.lastUploadTime).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return lastUploadTime
	 */
	public long getLastUploadTime() {
		return lastUploadTime;
	}

	/**
	 * @param lastUploadTime セットする lastUploadTime
	 */
	public void setLastUploadTime(long lastUploadTime) {
		this.lastUploadTime = lastUploadTime;
	}
}
