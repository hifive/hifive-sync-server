/*
 * Copyright (C) 2012-2013 NS Solutions Corporation
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
package com.htmlhifive.sync.sample.scd;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.htmlhifive.sync.exception.SyncException;

/**
 * 日付文字列のフォーマット変換を行うためのフォーマッタを保持するコンバートクラス.<br>
 * このクラスのインスタンスは同期化されないため、スレッドごとに別のインスタンスを使用する必要があります.
 *
 * @author kishigam
 */
public class ScheduleDateFormatConverter {

    /**
     * 日付書式変換のためのフォーマット文字列(整数8桁文字列)
     */
    static final String FORMAT_INT8 = "yyyyMMdd";

    /**
     * 日付書式変換のためのフォーマット文字列(スラッシュ区切りゼロ埋めなし)
     */
    static final String FORMAT_SLASH_SEPARATION = "y/M/d";

    /**
     * 変換前日付フォーマット
     */
    private DateFormat from;

    /**
     * 変換後日付フォーマット
     */
    private DateFormat to;

    /**
     * 変換前後の日付フォーマット文字列を指定してインスタンスを生成します.
     *
     * @param from
     *            変換前日付フォーマット文字列
     * @param to
     *            変換後日付フォーマット文字列
     */
    ScheduleDateFormatConverter(String from, String to) {
        this.from = new SimpleDateFormat(from);
        this.to = new SimpleDateFormat(to);
    }

    /**
     * 日付フォーマットを変換します.
     *
     * @param dateStr
     *            変換前日付文字列
     * @return 変換後日付文字列
     */
    String convertFormat(String dateStr) {

        String resultStr;
        try {
            resultStr = to.format(from.parse(dateStr));
        } catch (ParseException e) {
            throw new SyncException(e);
        }

        return resultStr;
    }
}