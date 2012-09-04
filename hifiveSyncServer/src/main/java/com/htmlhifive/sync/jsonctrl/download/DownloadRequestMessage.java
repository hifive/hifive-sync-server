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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.SyncMethod;
import com.htmlhifive.sync.resource.SyncRequestHeader;

/**
 * JSON形式の下り更新リクエストで同期対象を表現するメッセージのデータクラス.
 *
 * @author kishigam
 */
public class DownloadRequestMessage {

    /**
     * データモデル名.
     */
    private String dataModelName;

    /**
     * クエリ文字列.
     */
    private String query = "";

    /**
     * 前回同期時刻.
     */
    private long lastSyncTime = 0L;

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof DownloadRequestMessage))
            return false;

        DownloadRequestMessage msg = (DownloadRequestMessage)obj;

        return EqualsBuilder.reflectionEquals(this, msg);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * このメッセージの内容をもとにリソースへのGETりクエストのためのリクエストヘッダを生成します.<br>
     *
     * @param storageId
     *            クライアントのストレージID
     * @param requestTime
     *            同期実行時刻
     * @return 同期リクエストヘッダ
     */
    public SyncRequestHeader createHeader(String storageId, long requestTime) {

        SyncRequestHeader requestHeader =
                new SyncRequestHeader(SyncMethod.GET, storageId, requestTime);
        requestHeader.setDataModelName(dataModelName);
        // クエリ文字列をMapに変換
        requestHeader.setQueryMap(generateQueryMap());
        requestHeader.setLastSyncTime(lastSyncTime);

        return requestHeader;
    }

    /**
     * クエリ文字列からMapを生成します.
     *
     * @return クエリMap
     */
    public Map<String, String> generateQueryMap() {

        // TODO: Mapへの変換仕様決定
        return new HashMap<>();
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
     * @return query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query
     *            セットする query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return lastSyncTime
     */
    public long getLastSyncTime() {
        return lastSyncTime;
    }

    /**
     * @param lastSyncTime
     *            セットする lastSyncTime
     */
    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
