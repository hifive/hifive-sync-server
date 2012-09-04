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

import java.util.Date;

/**
 * クライアントからの下り更新リクエストに対する同期結果を保持するデータオブジェクト.
 *
 * @author kishigam
 */
public class SyncDownloadResult extends AbstractSyncResult {

    /**
     * クライアントに返す今回の同期時刻を、実際の同期実行時刻の何ミリ秒前とするかを設定します.<br>
     * (現状は120,000ミリ秒＝2分)
     */
    private static final long RESULT_TIME_DELAY = 2L * 60L * 1_000L;

    /**
     * ストレージIDを指定して同期結果オブジェクトを生成します.
     *
     * @param storageId
     *            クライアントのストレージID
     */
    public SyncDownloadResult(String storageId) {
        super(storageId);
    }

    /**
     * 下り更新リクエストに対してクライアントに返す実行時刻を導出し、設定します.<br>
     * 下り更新においては、同時に実行されている上り更新データを後に確実にダウンロードできるように、実際の実行時刻よりも一定時間後の時刻を設定します.<br>
     */
    @Override
    public void setGeneratedSyncTime() {

        setCurrentSyncTime(new Date().getTime() - RESULT_TIME_DELAY);
    }
}
