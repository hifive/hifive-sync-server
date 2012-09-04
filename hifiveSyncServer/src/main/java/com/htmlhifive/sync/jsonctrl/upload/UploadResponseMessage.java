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

import com.htmlhifive.sync.jsonctrl.SyncAction;

/**
 * 上り更新レスポンスの本体となるメッセージの抽象クラス.<br>
 * サーバ上の1つのリソースエレメントに関する更新結果を保持します.<br>
 * サブクラスでは、それぞれの更新結果を表現するために必要なデータを追加することができます.
 *
 * @author kishigam
 */
public abstract class UploadResponseMessage {

    /**
     * 上り更新対象データに対する同期データID.
     */
    private String syncDataId;

    /**
     * 上り更新の同期アクション.
     */
    private SyncAction action;

    /**
     * 上り更新レスポンスメッセージを生成します.
     *
     * @param syncDataId
     *            同期データID
     * @param action
     *            同期アクション
     */
    public UploadResponseMessage(String syncDataId, SyncAction action) {
        this.syncDataId = syncDataId;
        this.action = action;
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
}
