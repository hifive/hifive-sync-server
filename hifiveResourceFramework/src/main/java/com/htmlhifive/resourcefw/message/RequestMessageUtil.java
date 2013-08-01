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
package com.htmlhifive.resourcefw.message;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorFactory;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.BadRequestException;

/**
 * リクエストメッセージから、オブジェクトのプロパティを取り出すためのユーティリティ.<br>
 * メッセージのキーと同名のプロパティに対して、メッセージが保持する値を設定し、そのオブジェクトの型のインスタンスを生成します.
 *
 * @author kishigam
 */
public class RequestMessageUtil {

	/**
	 * メッセージのキーと同名のプロパティに対して、メッセージが保持する値を設定し、オブジェクトの型のインスタンスを返します.
	 *
	 * @param <T> メッセージから生成するオブジェクトの型
	 * @param requestMessage リクエストメッセージ
	 * @param objectType 生成する型のクラスオブジェクト
	 * @return オブジェクトの型のインスタンス
	 * @throws AbstractResourceException メッセージの内容とオブジェクトの型の間に矛盾がある場合
	 */
	public static <T> T extractObject(RequestMessage requestMessage, Class<T> objectType) throws BadRequestException {

		BeanWrapper wrapper;
		try {

			wrapper = createWrapper(requestMessage, objectType);

		} catch (InvalidPropertyException | PropertyAccessException e) {
			throw new BadRequestException("Failed to create " + objectType.getSimpleName() + " item. ", e,
					requestMessage);
		}

		return objectType.cast(wrapper.getWrappedInstance());
	}

	/**
	 * メッセージのキーと同名のプロパティに対してメッセージが保持する値を設定し、指定されたコピー先インスタンスに設定して返します.
	 *
	 * @param <T> メッセージから生成するオブジェクトの型
	 * @param requestMessage リクエストメッセージ
	 * @param objectType 生成する型のクラスオブジェクト
	 * @param targetObject コピー先のオブジェクト
	 * @return オブジェクトの型のインスタンス
	 * @throws AbstractResourceException メッセージの内容とオブジェクトの型の間に矛盾がある場合
	 */
	public static <T> T extractObject(RequestMessage requestMessage, Class<T> objectType, T targetObject)
			throws BadRequestException {

		T sourceItem = extractObject(requestMessage, objectType);
		BeanUtils.copyProperties(sourceItem, targetObject);

		return targetObject;
	}

	/**
	 * メッセージのキーと同名のプロパティに対して、メッセージが保持する値を設定し、オブジェクトの型のインスタンスを返します.<br/>
	 * メッセージのキーとオブジェクトの型のフィールド名が異なる場合に1つだけ個別指定ができます.<br>
	 * 例えばIDなど、メッセージのキーは固定で、オブジェクトの型それぞれでのフィールド名が異なる場合に使用できます.
	 *
	 * @param <T> メッセージから生成するオブジェクトの型
	 * @param requestMessage リクエストメッセージ
	 * @param objectType 生成する型のクラスオブジェクト
	 * @param propName オブジェクトの型でのフィールド名
	 * @param propValue propNameプロパティの値
	 * @return オブジェクトの型のインスタンス
	 * @throws AbstractResourceException メッセージの内容とオブジェクトの型の間に矛盾がある場合
	 */
	public static <T> T extractObject(RequestMessage requestMessage, Class<T> objectType, String propName,
			String propValue) throws BadRequestException {

		BeanWrapper wrapper;
		try {

			wrapper = createWrapper(requestMessage, objectType);

			// 個別指定フィールドの設定
			wrapper.setPropertyValue(propName, propValue);

		} catch (InvalidPropertyException | PropertyAccessException e) {
			throw new BadRequestException("Failed to create " + objectType.getSimpleName() + " item. ", e,
					requestMessage);
		}

		return objectType.cast(wrapper.getWrappedInstance());
	}

	/**
	 * メッセージのキーと同名のプロパティに対してメッセージが保持する値を設定し、指定されたコピー先インスタンスに設定して返します.<br/>
	 * メッセージのキーとオブジェクトの型のフィールド名が異なる場合に1つだけ個別指定ができます.<br>
	 * 例えばIDなど、メッセージのキーは固定で、オブジェクトの型それぞれでのフィールド名が異なる場合に使用できます.
	 *
	 * @param <T> メッセージから生成するオブジェクトの型
	 * @param requestMessage リクエストメッセージ
	 * @param objectType 生成する型のクラスオブジェクト
	 * @param idPropName オブジェクトの型でのフィールド名
	 * @param idPropValue propNameプロパティの値
	 * @param targetObject コピー先のリソースアイテム
	 * @return オブジェクトの型のインスタンス
	 * @throws AbstractResourceException メッセージの内容とオブジェクトの型の間に矛盾がある場合
	 */
	public static <T> T extractObject(RequestMessage requestMessage, Class<T> objectType, String idPropName,
			String idPropValue, T targetObject) throws BadRequestException {

		T sourceItem = extractObject(requestMessage, objectType, idPropName, idPropValue);
		BeanUtils.copyProperties(sourceItem, targetObject);

		return targetObject;
	}

	/**
	 * 指定された型の{@link BeanWrapper BeanWrapper}を生成し、メッセージの中でその型のプロパティと同じ名称のデータをコピーします.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @param objectType ラップするオブジェクトの型
	 * @return BeanWrapperオブジェクト
	 */
	private static BeanWrapper createWrapper(RequestMessage requestMessage, Class<?> objectType) {

		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(BeanUtils.instantiateClass(objectType));

		for (String propName : requestMessage.keys()) {
			if (wrapper.isWritableProperty(propName)) {
				wrapper.setPropertyValue(propName, requestMessage.get(propName));
			}
		}

		return wrapper;
	}
}
