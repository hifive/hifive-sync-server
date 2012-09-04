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

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.resource.SyncResponse;

/**
 * 同期ステータスを管理するサービス実装.<br>
 * インターフェースで規定されたサービスを永続データリポジトリを使用して保持します.
 *
 * @author kishigam
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class SyncStatusServiceImpl implements SyncStatusService {

    /**
     * クライアント前回アクセスデータのリポジトリ.
     */
    @Resource
    private ClientLastAccessRepository repository;

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
    @Override
    public boolean isDuplicatedRequest(String storageId, int hashCodeOfRequestData) {

        ClientLastAccessBean syncStatus = repository.findOne(storageId);
        if (syncStatus == null) {
            return false;
        }

        return hashCodeOfRequestData == syncStatus.getRequestHashCode();
    }

    /**
     * 指定されたクライアントごとのストレージIDで管理されているキャッシュデータを同期レスポンスオブジェクトに復元し、返します.
     * TODO:シリアライズ、デシリアライズの実装
     *
     * @param storageId
     *            クライアントのストレージID
     * @return 同期レスポンスオブジェクトのSet
     */
    @Override
    public Set<SyncResponse<?>> reversionResponseSet(String storageId) {

        // ClientLastAccessBean syncStatus = repository.findOne(storageId);

        // TODO: キャッシュフィールドの文字列をデシリアライズしてSetフィールドに代入
        return null;
    }

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
    @Override
    public void applyUploadResult(
            String storageId,
            int requestHashCode,
            Set<SyncResponse<?>> uploadResultSet) {

        ClientLastAccessBean status = repository.findOne(storageId);
        if (status == null) {
            status = new ClientLastAccessBean(storageId);
        }
        status.applySyncStatus(requestHashCode, createResponseCache(uploadResultSet));

        repository.save(status);
    }

    /**
     * 指定されたクライアントごとのストレージIDに対して管理しているキャッシュデータを削除します.
     *
     * @param storageId
     *            ストレージID
     */
    @Override
    public void removeClientAccess(String storageId) {

        ClientLastAccessBean status = repository.findOne(storageId);
        if (status != null) {
            repository.delete(status);
        }
    }

    /**
     * 上り更新の正常終了結果から、そのキャッシュ文字列を生成します.
     *
     * TODO:シリアライズ、デシリアライズの実装
     *
     * @return キャッシュ文字列
     */
    private String createResponseCache(Set<SyncResponse<?>> response) {

        // TODO: Setフィールドの内容をシリアライズしてキャッシュフィールドに代入
        return null;
    }
}
