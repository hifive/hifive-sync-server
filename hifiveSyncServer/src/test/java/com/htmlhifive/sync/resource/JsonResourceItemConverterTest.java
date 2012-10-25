/**
 *
 */
package com.htmlhifive.sync.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
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
public class JsonResourceItemConverterTest {

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
			@Mocked(capture = 1)
			ObjectMapper mapper;
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
	public void testConvertToItemObjectClass() throws Exception {

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
	public void testConvertToItemNullItem() throws Exception {

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
	public void testConvertToItemNullClass() throws Exception {

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
