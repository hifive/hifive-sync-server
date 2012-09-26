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

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.BadRequestException;

/**
 * JSON形式のデータをリソースアイテムの型に変換するコンバータ実装.
 *
 * @author kishigam
 */
@Service
public class JsonResourceItemConverter<T> implements ResourceItemConverter<T> {

	/**
	 * JSON形式のアイテムデータをアイテム型に変換して返します.
	 *
	 * @param itemObj アイテムデータ(JSON形式)
	 * @param to アイテム型のクラスオブジェクト
	 * @return アイテム
	 * @throws BadRequestException アイテムデータがアイテムの型に適合しないとき
	 */
	@Override
	public T convert(Object itemObj, Class<T> to) {

		Object item = null;
		try {
			// JSONデータからの型へ変換
			item = new ObjectMapper().convertValue(itemObj, to);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new BadRequestException("JSON data of sync resource cannot accept. ", e);
		}

		return to.cast(item);
	}
}
