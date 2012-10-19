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

import com.htmlhifive.sync.service.download.DownloadControlType;
import com.htmlhifive.sync.service.upload.UploadControlType;

/**
 * 同期制御のための設定情報取得インターフェース.
 *
 * @author kishigam
 */
public interface SyncConfiguration {

	/**
	 * クライアントに返す今回の同期時刻を、実際の同期実行時刻の何秒前とするか、その秒数を返します.
	 */
	int bufferTimeForDownload();

	/**
	 * 上り更新制御タイプを返します.
	 *
	 * @return 上り更新制御タイプ
	 */
	UploadControlType uploadControl();

	/**
	 * 下り更新制御タイプを返します.
	 *
	 * @return 下り更新制御タイプ
	 */
	DownloadControlType downloadControl();

	/**
	 * キー重複競合発生時の処理継続有無を返します.
	 *
	 * @return 継続する時true
	 */
	boolean isContinueOnConflictOfDuplicateId();

	/**
	 * 更新競合発生時の処理継続が可能かどうかを返します.
	 *
	 * @return 継続する時true
	 */
	boolean isContinueOnConflictOfUpdated();
}
