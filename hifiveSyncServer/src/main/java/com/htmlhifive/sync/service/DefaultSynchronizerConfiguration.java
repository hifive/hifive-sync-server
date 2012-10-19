package com.htmlhifive.sync.service;

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
	private UploadControlType uploadControlType = UploadControlType.RESERVE;

	/**
	 * 下り更新制御タイプ.
	 */
	private DownloadControlType downloadControlType = DownloadControlType.READ_LOCK;

	/**
	 * キー重複競合発生時の処理継続有無.<br>
	 */
	private boolean continueOnConflictOfDuplicateId = false;

	/**
	 * 更新競合発生時の処理継続有無.<br>
	 */
	private boolean continueOnConflictOfUpdated = false;

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
