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
package com.htmlhifive.resourcefw.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.exception.GenericResourceException;

/**
 * リソースクラスインスタンス、Methodオブジェクトを保持し、リソースメソッドのinvokeを実行するクラス.
 *
 * @author kishigam
 */
public class ResourceMethodInvoker {

	/**
	 * このオブジェクト実行するリソースクラスのインスタンス
	 */
	private final Object resource;

	/**
	 * このオブジェクト実行するリソースクラスのメソッドオブジェクト
	 */
	private final Method method;

	/**
	 * リソースクラスのインスタンスとそのメソッドオブジェクトを指定してInvokerを生成します.
	 *
	 * @param resource リソースクラスインスタンス
	 * @param method メソッドオブジェクト
	 */
	public ResourceMethodInvoker(Object resource, Method method) {
		this.resource = resource;
		this.method = method;
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * argsを引数としてリソースクラスのメソッドを実行します.
	 *
	 * @param args メソッドの引数
	 * @return メソッドの戻り値
	 * @throws AbstractResourceException
	 */
	public Object invoke(Object... args) throws AbstractResourceException {

		Object returnValue = null;
		try {
			returnValue = this.method.invoke(this.resource, args);
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (cause instanceof AbstractResourceException) {
				throw (AbstractResourceException) cause;
			}
			throw new GenericResourceException("Resource method invocation failed.", cause);
		} catch (IllegalAccessException iae) {
			throw new GenericResourceException("Resource method invocation failed.", iae);
		}

		return this.method.getReturnType().cast(returnValue);
	}

	/**
	 * リソースクラスインスタンスを返します.
	 *
	 * @return リソースクラスインスタンス
	 */
	public Object getResource() {
		return resource;
	}
}
