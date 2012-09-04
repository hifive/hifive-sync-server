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

import java.util.Set;

import com.htmlhifive.sync.resource.SyncResponse;

/**
 * 同期ステータスを管理するサービスインターフェース.<br>
 * クライアントからの上り更新リクエストの二重送信を判別し、
 * 前回の上り更新リクエストに対するレスポンスをキャッシュし、二重送信を検知した場合にそれを返せるように保持します.
 *
 * @author kishigam
 */
public interface SyncStatusService {

    /**
     * 指定されたクライアントごとのストレージID、そのクライアントからのリクエストデータから生成したハッシュコードを受け取り、
     * このリクエストが既に送信されてきたものであるかどうかを判断します.
     *
     * @param storageId
     *            クライアントのストレージID
     * @param hashCode
     *            リクエストデータのハッシュコード
     * @return 二重送信である場合true
     */
    boolean isDuplicatedRequest(String storageId, int hashCode);

    /**
     * 指定されたクライアントごとのストレージIDで管理されているキャッシュデータを同期レスポンスオブジェクトに復元し、返します.
     *
     * @param storageId
     *            クライアントのストレージID
     * @return 同期レスポンスオブジェクトのSet
     */
    Set<SyncResponse<?>> reversionResponseSet(String storageId);

    /**
     * 指定されたクライアントごとのストレージIDに対して、リクエストデータのハッシュコードにそれに対するレスポンスデータをキャッシュします.
     *
     * @param storageId
     *            ストレージID
     * @param hashCode
     *            リクエストデータのハッシュコード
     * @param uploadResultSet
     *            同期レスポンスデータのセット
     */
    void applyUploadResult(String storageId, int hashCode, Set<SyncResponse<?>> uploadResultSet);

    /**
     * 指定されたクライアントごとのストレージIDに対して管理しているキャッシュデータを削除します.
     *
     * @param storageId
     *            ストレージID
     */
    void removeClientAccess(String storageId);
}
