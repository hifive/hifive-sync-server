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
package com.htmlhifive.sync.resource;

import java.util.Map;

/**
 * リソースを同期するための共通データを扱うサービスインターフェース.<br>
 * {@link SyncRequestHeader 同期リクエストヘッダ}、{@link SyncResponseHeader 同期レスポンスヘッダ}
 * でデータを受渡します.<br>
 * 共通データの永続化はサブクラスが担います.
 *
 * @author kishigam
 */
public interface SyncProvider {

    /**
     * リソースに対応する共通データを返します.<br>
     *
     * @param requestHeader
     *            リソースへのリクエストヘッダ
     * @return リソースからのレスポンスヘッダ
     */
    SyncResponseHeader getCommonData(SyncRequestHeader requestHeader);

    /**
     * 指定された時刻以降に更新された全てのリソースに対応する共通データを返します. ロックは考慮しません.
     *
     * @param requestHeader
     *            リソースへのリクエストヘッダ
     *
     * @return リソースID文字列をKey、リソースからのレスポンスヘッダをValueとするMap
     */
    Map<String, SyncResponseHeader> getCommonDataModifiedSince(SyncRequestHeader requestHeader);

    /**
     * リソースに対応する共通データに対し、ロックを設定します.<br>
     *
     * @param responseHeader
     *            リソースからのレスポンスヘッダ
     */
    boolean getLock(SyncRequestHeader requestHeader);

    /**
     * リソースに対応する共通データに対し、ロックを解除して更新します.
     *
     * @param responseHeader
     *            リソースからのレスポンスヘッダ
     */
    void releaseLock(SyncResponseHeader responseHeader);

    /**
     * 新規リソースに対応する共通データを生成し、保存します.
     *
     * @param requestHeader
     *            リソースへのリクエストヘッダ
     * @param targetResourceIdStr
     *            リソースエレメントに固有のID文字列
     * @return リソースからのレスポンスヘッダ
     */
    SyncResponseHeader
            saveNewCommonData(SyncRequestHeader requestHeader, String targetResourceIdStr);

    /**
     * リソースに対応する共通データを更新し、保存します.<br>
     * ロックは開放されます.
     *
     * @param requestHeader
     *            リソースへのリクエストヘッダ
     * @return リソースからのレスポンスヘッダ
     */
    SyncResponseHeader saveUpdatedCommonData(SyncRequestHeader requestHeader);
}
