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
package com.htmlhifive.sync.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;

import com.htmlhifive.sync.exception.BadRequestException;

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
		JsonResourceItemConverter<Item> target1 = new JsonResourceItemConverter<>();

		final Item item = new Item() {
			{
				setItem1("json");
				setItem2(1);
			}
		};

		@SuppressWarnings("serial")
		Map<String, Object> itemObj = new HashMap<String, Object>() {
			{
				put("item1", item.getItem1());
				put("item2", item.getItem2());
			}
		};
		final Class<Item> to = Item.class;

		// Act
		Item actual = target1.convertToItem(itemObj, to);

		// Assert：結果が正しいこと
		assertThat(actual.getItem1(), is(equalTo(item.getItem1())));
		assertThat(actual.getItem2(), is(equalTo(item.getItem2())));
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * オブジェクトがnullのときは{@link NullPointerException}がスローされる.
	 */
	@Test(expected = NullPointerException.class)
	public void testConvertToItemFailBecauseOfNullItem() throws Exception {

		// Arrange：異常系
		JsonResourceItemConverter<Item> target1 = new JsonResourceItemConverter<>();

		final Class<Item> to1 = Item.class;

		// Act
		target1.convertToItem(null, to1);

		fail();
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * 変換後の型がnullの時は{@link IllegalArgumentException}がスローされる.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConvertToItemFailBecauseOfNullType() throws Exception {

		// Arrange：異常系
		JsonResourceItemConverter<Item> target1 = new JsonResourceItemConverter<>();

		final Item item = new Item() {
			{
				setItem1("json");
				setItem2(1);
			}
		};
		@SuppressWarnings("serial")
		Map<String, Object> itemObj = new HashMap<String, Object>() {
			{
				put("item1", item.getItem1());
				put("item2", item.getItem2());
			}
		};

		// Act
		target1.convertToItem(itemObj, null);

		// Assert：例外以外は失敗
		fail();
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * アイテムクラスにデフォルトコンストラクタがない場合、インスタンスを作れないため {@link BeanInstantiationException}がスローされる.
	 */
	@Test(expected = BeanInstantiationException.class)
	public void testConvertToItemFailIfItemTypeHasNoDefaultConstructor() throws Exception {

		// Arrange：異常系
		JsonResourceItemConverter<ItemNoDefaultConstructor> target1 = new JsonResourceItemConverter<>();

		final ItemNoDefaultConstructor item = new ItemNoDefaultConstructor("json");

		@SuppressWarnings("serial")
		Map<String, Object> itemObj = new HashMap<String, Object>() {
			{
				put("item1", item.getItem1());
			}
		};

		final Class<ItemNoDefaultConstructor> to = ItemNoDefaultConstructor.class;

		// Act
		target1.convertToItem(itemObj, to);

		// Assert：例外以外は失敗
		fail();
	}

	/**
	 * {@link JsonResourceItemConverter#convertToItem(Object, Class)}用テストメソッド.<br>
	 * Mapに含まれるデータがアイテムクラスのプロパティに含まれない場合 {@link BadRequestException}がスローされる.
	 */
	@Test(expected = BadRequestException.class)
	public void testConvertToItemFailIfItemTypeMismatch() throws Exception {

		// Arrange：異常系
		JsonResourceItemConverter<Item> target1 = new JsonResourceItemConverter<>();

		final Item item = new Item() {
			{
				setItem1("json");
				setItem2(1);
			}
		};

		@SuppressWarnings("serial")
		Map<String, Object> itemObj = new HashMap<String, Object>() {
			{
				put("item1", item.getItem1());
				put("item2", item.getItem2());
				put("item3", "illegal");
			}
		};
		final Class<Item> to = Item.class;

		// Act
		target1.convertToItem(itemObj, to);

		// Assert：例外以外は失敗
		fail();
	}
}

class Item {
	private String item1;
	private int item2;

	public String getItem1() {
		return item1;
	}

	public void setItem1(String item1) {
		this.item1 = item1;
	}

	public int getItem2() {
		return item2;
	}

	public void setItem2(int item2) {
		this.item2 = item2;
	}
}

class ItemNoDefaultConstructor {
	private String item1;

	ItemNoDefaultConstructor(String item1) {
		this.item1 = item1;
	}

	public String getItem1() {
		return item1;
	}

	public void setItem1(String item1) {
		this.item1 = item1;
	}
}
