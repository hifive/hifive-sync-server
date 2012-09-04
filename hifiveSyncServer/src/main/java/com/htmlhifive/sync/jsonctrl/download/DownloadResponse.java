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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.htmlhifive.sync.jsonctrl.ResponseBody;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * クライアントからの下り更新リクエストに対するレスポンスデータの抽象クラス.
 *
 * @author kishigam
 */
public abstract class DownloadResponse implements ResponseBody {

    /**
     * リクエストに対する処理を実行した同期時刻.
     */
    private long syncTime;

    /**
     * 下り更新レスポンスの本体となるメッセージオブジェクトのリスト.
     */
    private List<DownloadResponseMessage<?>> dataList;

    /**
     * 下り更新レスポンスを生成します.
     *
     * @param syncTime
     *            同期時刻
     * @param datalList
     *            同期レスポンスオブジェクトのリスト
     */
    public DownloadResponse(long syncTime, List<DownloadResponseMessage<?>> datalList) {
        this.syncTime = syncTime;
        this.dataList = datalList;
    }

    /**
     * 同期処理レスポンスから、クライアントに返す下り更新レスポンスメッセージを生成します.<br>
     *
     * @param responseSet
     *            同期処理のレスポンスオブジェクトのセット
     * @return 下り更新レスポンスメッセージのリスト
     */
    protected static List<DownloadResponseMessage<?>> createResponseMessageList(
            Set<SyncResponse<?>> responseSet) {

        List<DownloadResponseMessage<?>> responseMessages = new ArrayList<>();

        for (SyncResponse<?> response : responseSet) {
            responseMessages.add(new DownloadResponseMessage<>(response));
        }

        return responseMessages;
    }

    /**
     * @return syncTime
     */
    public long getSyncTime() {
        return syncTime;
    }

    /**
     * @param syncTime
     *            セットする syncTime
     */
    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    /**
     * @return dataList
     */
    public List<DownloadResponseMessage<?>> getDataList() {
        return dataList;
    }

    /**
     * @param dataList
     *            セットする dataList
     */
    public void setDataList(List<DownloadResponseMessage<?>> dataList) {
        this.dataList = dataList;
    }
}