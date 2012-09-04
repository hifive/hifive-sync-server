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
package com.htmlhifive.sync.exception;

import com.htmlhifive.sync.resource.SyncResponse;

/**
 * 同期(上り更新)において競合が発生したことを示す例外.<br>
 * サーバ側で管理しているリソースエレメントの内容を含む同期レスポンスオブジェクトを保持しており、クライアントに返す情報の生成に使用することができます.
 *
 * @author kishigam
 */
public class ConflictException extends RuntimeException {

    /**
     * シリアルバージョンUID.
     */
    private static final long serialVersionUID = 4223423810625297238L;

    /**
     * 競合したリソースエレメントの内容を含む同期レスポンスオブジェクト.
     */
    private SyncResponse<?> response;

    /**
     * 競合対象のサーバ側リソースエレメントの内容を含んだ同期レスポンスオブジェクトを指定して、例外オブジェクトを生成します.
     *
     * @param response
     *            同期レスポンスオブジェクト
     * @see RuntimeException
     */
    public ConflictException(SyncResponse<?> response) {
        super();
        this.response = response;
    }

    /**
     * 競合対象のサーバ側リソースエレメントの内容を含んだ同期レスポンスオブジェクトを指定して、例外オブジェクトを生成します.
     *
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     * @param response
     *            同期レスポンスオブジェクト
     * @see RuntimeException
     */
    public ConflictException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
            SyncResponse<?> response) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.response = response;
    }

    /**
     * 競合対象のサーバ側リソースエレメントの内容を含んだ同期レスポンスオブジェクトを指定して、例外オブジェクトを生成します.
     *
     * @param message
     * @param cause
     *            原因となった例外.
     * @param response
     *            同期レスポンスオブジェクト
     * @see RuntimeException
     */
    public ConflictException(String message, Throwable cause, SyncResponse<?> response) {
        super(message, cause);
        this.response = response;
    }

    /**
     * 競合対象のサーバ側リソースエレメントの内容を含んだ同期レスポンスオブジェクトを指定して、例外オブジェクトを生成します.
     *
     * @param message
     * @param response
     *            同期レスポンスオブジェクト
     * @see RuntimeException
     */
    public ConflictException(String message, SyncResponse<?> response) {
        super(message);
        this.response = response;
    }

    /**
     * 競合対象のサーバ側リソースエレメントの内容を含んだ同期レスポンスオブジェクトを指定して、例外オブジェクトを生成します.
     *
     * @param cause
     * @param response
     *            同期レスポンスオブジェクト
     * @see RuntimeException
     */
    public ConflictException(Throwable cause, SyncResponse<?> response) {
        super(cause);
        this.response = response;
    }

    /**
     * この例外が保持する、競合対象のサーバ側リソースエレメントの内容を含む同期レスポンスオブジェクトを返します.
     *
     * @return 同期レスポンスオブジェクト
     */
    public SyncResponse<?> getConflictedResponse() {
        return response;
    }
}
