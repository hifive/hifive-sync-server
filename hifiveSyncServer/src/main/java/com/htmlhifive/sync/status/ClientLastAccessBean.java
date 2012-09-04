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
package com.htmlhifive.sync.status;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 各クライアントの前回アクセス情報を保持するエンティティクラス.<br>
 * クライアントのストレージIDごとに、リクエスト(ハッシュコード)とレスポンス(キャッシュ文字列)を保持します.
 *
 * @author kishigam
 */
@Entity
@Table(name = "CLIENT_LAST_ACCESS")
public class ClientLastAccessBean {

    /**
     * クライアントのストレージID.<br>
     */
    @Id
    private String storageId;

    /**
     * リクエストデータのハッシュコード.
     */
    private int requestHashCode;

    /**
     * リクエストに対するレスポンスデータのキャッシュ文字列.
     */
    private String responseCache;

    /**
     * プライベートのデフォルトコンストラクタ. <br>
     * 永続マネージャーが使用するため、実装する必要があります.
     */
    @SuppressWarnings("unused")
    private ClientLastAccessBean() {
    }

    /**
     * ストレージIDを使用して、新規エンティティインスタンスを生成します.
     *
     * @param storageId
     *            クライアントのストレージID
     */
    public ClientLastAccessBean(String storageId) {
        this.storageId = storageId;
    }

    /**
     * このオブジェクトのステータスを更新します.<br>
     * リクエストのハッシュコードとレスポンスのキャッシュ文字列が反映されます.
     *
     * @param hashCode
     *            リクエストのハッシュコード
     * @param cacheStr
     *            レスポンスのキャッシュ文字列
     */
    public void applySyncStatus(int hashCode, String cacheStr) {

        this.requestHashCode = hashCode;
        this.responseCache = cacheStr;
    }

    /**
     * @return storageId
     */
    public String getStorageId() {
        return storageId;
    }

    /**
     * @param storageId
     *            セットする storageId
     */
    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    /**
     * @return requestHashCode
     */
    public int getRequestHashCode() {
        return requestHashCode;
    }

    /**
     * @param requestHashCode
     *            セットする requestHashCode
     */
    public void setRequestHashCode(int requestHashCode) {
        this.requestHashCode = requestHashCode;
    }

    /**
     * @return responseCache
     */
    public String getResponseCache() {
        return responseCache;
    }

    /**
     * @param responseCache
     *            セットする responseCache
     */
    public void setResponseCache(String responseCache) {
        this.responseCache = responseCache;
    }
}
