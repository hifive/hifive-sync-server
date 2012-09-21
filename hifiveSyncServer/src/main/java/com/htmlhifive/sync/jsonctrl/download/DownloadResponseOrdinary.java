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
package com.htmlhifive.sync.jsonctrl.download;

/**
 * 既知のクライアントからの下り更新リクエストに対するレスポンスデータクラス.
 *
 * @author kishigam
 */
public class DownloadResponseOrdinary extends DownloadResponse {

	/**
	 * 下り更新結果データを生成します.
	 *
	 * @param lastDownloadTime 下り更新時刻
	 */
	public DownloadResponseOrdinary(long lastDownloadTime) {

		super(lastDownloadTime);
	}
}
