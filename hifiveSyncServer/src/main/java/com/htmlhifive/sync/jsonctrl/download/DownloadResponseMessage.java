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

import com.htmlhifive.sync.jsonctrl.JsonDataConvertor;
import com.htmlhifive.sync.jsonctrl.SyncAction;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * 下り更新レスポンスの本体となるメッセージのデータクラス.<br>
 * サーバ上の1つのリソースエレメントに関する共通情報、およびエレメント本体を保持します.
 *
 * @param <E>
 *            リソースエレメントの型
 * @author kishigam
 */
public class DownloadResponseMessage<E> {

    /**
     * 同期データID.
     */
    private String syncDataId;

    /**
     * データモデル名
     */
    private String dataModelName;

    /**
     * このデータが生成された契機となった同期アクション.
     */
    private SyncAction action;

    /**
     * このデータの最終更新時刻
     */
    private long lastModified;

    /**
     * このデータが表現するリソースエレメントの本体.
     */
    private E element;

    /**
     * 同期レスポンスオブジェクトからクライアントに返す下り更新レスポンスを生成します.
     *
     * @param response
     *            同期レスポンスオブジェクト
     */
    public DownloadResponseMessage(SyncResponse<E> response) {

        dataModelName = response.getHeader().getDataModelName();
        action = JsonDataConvertor.convertSyncMethodToAction(response.getHeader().getSyncMethod());
        syncDataId = response.getHeader().getSyncDataId();
        lastModified = response.getHeader().getLastModified();
        element = response.getElement();
    }

    /**
     * @return syncDataId
     */
    public String getSyncDataId() {
        return syncDataId;
    }

    /**
     * @param syncDataId
     *            セットする syncDataId
     */
    public void setSyncDataId(String syncDataId) {
        this.syncDataId = syncDataId;
    }

    /**
     * @return dataModelName
     */
    public String getDataModelName() {
        return dataModelName;
    }

    /**
     * @param dataModelName
     *            セットする dataModelName
     */
    public void setDataModelName(String dataModelName) {
        this.dataModelName = dataModelName;
    }

    /**
     * @return action
     */
    public SyncAction getAction() {
        return action;
    }

    /**
     * @param action
     *            セットする action
     */
    public void setAction(SyncAction action) {
        this.action = action;
    }

    /**
     * @return lastModified
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified
     *            セットする lastModified
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return element
     */
    public E getElement() {
        return element;
    }

    /**
     * @param element
     *            セットする element
     */
    public void setElement(E element) {
        this.element = element;
    }
}
