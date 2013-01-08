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
package com.htmlhifive.sync.service;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.htmlhifive.sync.service.download.DownloadControlType;
import com.htmlhifive.sync.service.upload.UploadControlType;

/**
 * 同期制御のための設定情報取得実装クラス.<br>
 *
 * @author kishigam
 */
@Component
public class DefaultSynchronizerConfiguration implements SyncConfiguration {

	/**
	 * クライアントに返す今回の同期時刻を、実際の同期実行時刻の何秒前とするか.
	 */
	private int bufferTimeForDownload = 0;

	/**
	 * 上り更新制御タイプ.
	 */
	private UploadControlType uploadControlType = UploadControlType.NONE;

	/**
	 * 下り更新制御タイプ.
	 */
	private DownloadControlType downloadControlType = DownloadControlType.NONE;

	/**
	 * キー重複競合発生時の処理継続有無.<br>
	 */
	private boolean continueOnConflictOfDuplicateId = false;

	/**
	 * 更新競合発生時の処理継続有無.<br>
	 */
	private boolean continueOnConflictOfUpdated = false;

	/**
	 * コンテナで管理されるインスタンスが構築された後、ログを出力します.
	 */
	@PostConstruct
	private void logPostConstruct() {

		StringBuilder log = new StringBuilder();
		log.append("DefaultSynchronizerConfiguration : ")
		//
				.append("  uploadControlType : ").append(uploadControlType) //
				.append("  downloadControlType : ").append(downloadControlType) //
				.append("  continueOnConflictOfDuplicateId : ").append(continueOnConflictOfDuplicateId) //
				.append("  continueOnConflictOfUpdated : ").append(continueOnConflictOfUpdated);

		LoggerFactory.getLogger(this.getClass()).info(log.toString());
	}

	/**
	 * 上り更新および下り更新に使用される同期時刻を返します.<br>
	 * システム時刻を用います.
	 *
	 * @return 同期時刻
	 */
	@Override
	public long generateSyncTime() {
		return System.currentTimeMillis();
	}

	@Override
	public int bufferTimeForDownload() {

		return bufferTimeForDownload;
	}

	@Override
	public UploadControlType uploadControl() {

		return uploadControlType;
	}

	@Override
	public DownloadControlType downloadControl() {

		return downloadControlType;
	}

	@Override
	public boolean isContinueOnConflictOfDuplicateId() {

		return continueOnConflictOfDuplicateId;
	}

	@Override
	public boolean isContinueOnConflictOfUpdated() {

		return continueOnConflictOfUpdated;
	}

	/**
	 * @param bufferTimeForDownload セットする bufferTimeForDownload
	 */
	public void setBufferTimeForDownload(int bufferTimeForDownload) {
		this.bufferTimeForDownload = bufferTimeForDownload;
	}

	/**
	 * @param uploadControlType セットする uploadControlType
	 */
	public void setUploadControlType(UploadControlType uploadControlType) {
		this.uploadControlType = uploadControlType;
	}

	/**
	 * @param downloadControlType セットする downloadControlType
	 */
	public void setDownloadControlType(DownloadControlType downloadControlType) {
		this.downloadControlType = downloadControlType;
	}

	/**
	 * @param continueOnConflictOfDuplicateId セットする continueOnConflictOfDuplicateId
	 */
	public void setContinueOnConflictOfDuplicateId(boolean continueOnConflictOfDuplicateId) {
		this.continueOnConflictOfDuplicateId = continueOnConflictOfDuplicateId;
	}

	/**
	 * @param continueOnConflictOfUpdated セットする continueOnConflictOfUpdated
	 */
	public void setContinueOnConflictOfUpdated(boolean continueOnConflictOfUpdated) {
		this.continueOnConflictOfUpdated = continueOnConflictOfUpdated;
	}
}
