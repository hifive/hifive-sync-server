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

import java.util.Set;

import com.htmlhifive.sync.exception.ConflictException;

/**
 * 「リソース」を表すインターフェースです.<br>
 *
 * このフレームワークにおける「リソース」とは、 ある形を持ったデータの集合を表します。<br>
 * リソースはリソースエレメントを持ちます。エレメントはデータ同期の最小単位です。
 *
 * 概ねDAOパターンにおけるDAOとEntityの関係と同じですが、 実際のDB操作などのために別途DAOと呼ばれるクラスができる
 * ことと、HTTPにおけるURLにマッピングできることから リソースと呼ぶことにしています。<br>
 *
 * 例えばスケジュール管理アプリの場合 　- リソースエレメント = １件１件のスケジュール 　- リソース = スケジュールの集合
 * と設計することができます。<br>
 *
 * @param <E>
 *            エレメントのデータ型
 */
public interface SyncResource<E> {

    /**
     * リクエストヘッダが指定する単一のリソースエレメントを取得します.<br>
     *
     * @param requestHeader
     *            同期リクエストヘッダ
     * @return 取得したエレメントを含む同期レスポンスオブジェクト
     */
    SyncResponse<E> get(SyncRequestHeader requestHeader);

    /**
     * リクエストヘッダが指定し、指定時刻以降に更新された全リソースエレメントを取得します.<br>
     *
     * @param lastSyncTime
     *            指定時刻
     * @return 指定時刻以降に更新されたエレメントを含む同期レスポンスのリスト
     */
    Set<SyncResponse<E>> getModifiedSince(SyncRequestHeader requestHeader);

    /**
     * リクエストヘッダが指定するリソースエレメントを更新します.<br>
     *
     * @param requestHeader
     *            同期リクエストヘッダ
     * @param element
     *            更新後のリソースエレメント
     * @return 更新結果を含む同期レスポンスのリスト
     * @throws ConflictException
     *             衝突発生時の例外
     */
    SyncResponse<E> put(SyncRequestHeader requestHeader, E element) throws ConflictException;

    /**
     * リクエストヘッダが指定するリソースエレメントを削除します.
     *
     * @param requestHeader
     *            同期リクエストヘッダ
     * @return 削除結果を含む同期レスポンスのリスト
     * @throws ConflictException
     *             衝突発生時の例外
     */
    SyncResponse<E> delete(SyncRequestHeader requestHeader) throws ConflictException;

    /**
     * リソースエレメントを追加します.
     *
     * @param requestHeader
     *            同期リクエストヘッダ
     * @param newElement
     *            追加したいリソースエレメント
     * @return 追加結果を含む同期レスポンスのリスト
     * @throws ConflictException
     *             衝突が発生
     */
    SyncResponse<E> post(SyncRequestHeader requestHeader, E newElement) throws ConflictException;

    /**
     * このリソースのエレメント型を返します.
     *
     * @return エレメントの型を表すClassオブジェクト
     */
    Class<E> getElementType();

    void setLockManager(LockManager lm);
}
