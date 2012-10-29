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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * <H3>JsonResourceItemConverterのテストクラス.</H3>
 *
 * @author kishigam
 */
public class JsonResourceItemConverterTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(JsonResourceItemConverter.class, notNullValue());
	}

	/**
	 * {@link JsonResourceItemConverter#JsonResourceItemConverter()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		JsonResourceItemConverter<String> target = new JsonResourceItemConverter<>();
		assertThat(target, notNullValue());
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.
	 */
	@Test
	public void testConvertToItem() throws Exception {

		// Arrange：正常系
		JsonResourceItemConverter<String> target1 = new JsonResourceItemConverter<>();

		String item1 = "json";
		Object itemObj1 = item1;
		final Class<String> to1 = String.class;

		// Act
		String actual1 = target1.convertToItem(itemObj1, to1);
		// Assert：結果が正しいこと
		assertThat(actual1, is(equalTo(item1)));
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * オブジェクトがnullのときは結果もnullになる.
	 */
	@Test
	public void testConvertToItemReturnsNullBecauseOfNullItem() throws Exception {

		// Arrange：例外系
		JsonResourceItemConverter<String> target1 = new JsonResourceItemConverter<>();

		final Class<String> to1 = String.class;

		// Act
		String actual1 = target1.convertToItem(null, to1);
		// Assert：結果が正しいこと
		assertThat(actual1, is(nullValue()));
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * 変換後の型がnullの時は{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testConvertToItemReturnsNullBecauseOfNullType() throws Exception {

		// Arrange：異常系
		JsonResourceItemConverter<String> target1 = new JsonResourceItemConverter<>();

		String item1 = "json";
		Object itemObj1 = item1;

		// Act
		target1.convertToItem(itemObj1, null);

		// Assert：例外以外は失敗
		fail();
	}
}
