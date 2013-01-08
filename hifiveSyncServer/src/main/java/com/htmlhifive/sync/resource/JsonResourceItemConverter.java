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

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.BadRequestException;

/**
 * JSONデータなど、Map<String,Object>オブジェクトをリソースアイテムの型に変換するコンバータ実装.
 *
 * @author kishigam
 */
@Service
public class JsonResourceItemConverter<I> implements ResourceItemConverter<I> {

	/**
	 * JSON形式のアイテムデータをアイテム型に変換して返します.<br>
	 * 実際の型が、JSONデータを基にしたMap<String,Object>である場合に使用できます.
	 *
	 * @param itemObj アイテムデータ
	 * @param to アイテム型のクラスオブジェクト
	 * @return アイテム
	 * @throws BadRequestException アイテムデータがアイテムの型に適合しないとき
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I convertToItem(Object itemObj, Class<I> to) {

		BeanWrapper wrapper;
		try {

			wrapper = PropertyAccessorFactory.forBeanPropertyAccess(BeanUtils.instantiateClass(to));

			Map<String, Object> itemMap = (Map<String, Object>) itemObj;
			for (String propName : itemMap.keySet()) {
				wrapper.setPropertyValue(propName, itemMap.get(propName));
			}

		} catch (InvalidPropertyException | PropertyAccessException e) {
			e.printStackTrace();
			throw new BadRequestException("JSON data of sync resource cannot accept. ", e);
		}

		return to.cast(wrapper.getWrappedInstance());
	}
}
