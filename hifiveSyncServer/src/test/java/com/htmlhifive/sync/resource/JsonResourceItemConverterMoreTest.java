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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import mockit.Expectations;
import mockit.Mocked;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.htmlhifive.sync.exception.BadRequestException;

/**
 * <H3>JsonResourceItemConverterのテストクラス.</H3>
 *
 * @author kishigam
 */
public class JsonResourceItemConverterMoreTest {

	@Mocked(capture = 1)
	private ObjectMapper mapper;

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * 変換に失敗した場合{@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testConvertToItemFailBecauseOfThrowsException() throws Exception {

		// Arrange：異常系
		final JsonResourceItemConverter<String> target1 = new JsonResourceItemConverter<>();

		String item1 = "json";
		final Object itemObj1 = item1;
		final Class<String> to1 = String.class;

		final IllegalArgumentException expectedException = new IllegalArgumentException("in test");

		new Expectations() {
			{
				mapper = new ObjectMapper();
				mapper.convertValue(itemObj1, to1);
				result = expectedException;
			}
		};

		try {
			// Act
			target1.convertToItem(itemObj1, to1);

			// Assert：例外以外は失敗
			fail();
		} catch (Exception actual) {

			assertThat((IllegalArgumentException) actual.getCause(), is(equalTo(expectedException)));
			throw actual;
		}
	}
}
