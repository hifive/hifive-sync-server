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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 同期リクエストにおいて、その内容が解釈できない場合にスローされる例外.<br>
 * この例外がスローされた場合、ステータスコード400のHTTPレスポンスが返されることを想定しています.
 *
 * @author kishigam
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad Requst")
public class BadRequestException extends RuntimeException {

    /**
     * シリアルバージョンUID.
     */
    private static final long serialVersionUID = -8730623190768211005L;

    /**
     * @see RuntimeException
     */
    public BadRequestException() {
        super();
    }

    /**
     * @see RuntimeException
     */
    public BadRequestException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @see RuntimeException
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @see RuntimeException
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * @see RuntimeException
     */
    public BadRequestException(Throwable cause) {
        super(cause);

    }
}
