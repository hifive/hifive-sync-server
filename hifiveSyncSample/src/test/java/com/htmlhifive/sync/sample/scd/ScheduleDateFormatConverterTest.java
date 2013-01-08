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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * <H3>
 * ScheduleDateFormatConverterのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ScheduleDateFormatConverterTest {

    private String from = "yyyyMMdd hhmmss";
    private String to = "yy/M/d h:m:s";

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(ScheduleDateFormatConverter.class, notNullValue());
    }

    /**
     * {@link ScheduleDateFormatConverter#ScheduleDateFormatConverter(String, String)}
     * 用テストメソッド.
     */
    @Test
    public void testInstantiation() {

        ScheduleDateFormatConverter target = new ScheduleDateFormatConverter(from, to);

        assertThat(target, notNullValue());
    }

    /**
     * {@link ScheduleDateFormatConverter#convertFormat(String)}用テストメソッド.<br>
     * 定数として提供しているフォーマット形式の相互変換の検証.
     */
    @Test
    public void testConvertFormatConstantFormat() {

        ScheduleDateFormatConverter target =
                new ScheduleDateFormatConverter(
                        ScheduleDateFormatConverter.FORMAT_INT8,
                        ScheduleDateFormatConverter.FORMAT_SLASH_SEPARATION);

        final String dateStr = "20010101";

        String actual = target.convertFormat(dateStr);
        String expected = "2001/1/1";
        assertThat(actual, is(equalTo(expected)));

        ScheduleDateFormatConverter reverse =
                new ScheduleDateFormatConverter(
                        ScheduleDateFormatConverter.FORMAT_SLASH_SEPARATION,
                        ScheduleDateFormatConverter.FORMAT_INT8);

        assertThat(reverse.convertFormat(actual), is(equalTo(dateStr)));
    }

    /**
     * {@link ScheduleDateFormatConverter#convertFormat(String)}用テストメソッド.
     */
    @Test
    public void testConvertFormat() {

        ScheduleDateFormatConverter target = new ScheduleDateFormatConverter(from, to);

        final String dateStr = "20010101 010101";

        String actual = target.convertFormat(dateStr);
        String expected = "01/1/1 1:1:1";
        assertThat(actual, is(equalTo(expected)));

        ScheduleDateFormatConverter reverse = new ScheduleDateFormatConverter(to, from);
        assertThat(reverse.convertFormat(actual), is(equalTo(dateStr)));
    }

}
