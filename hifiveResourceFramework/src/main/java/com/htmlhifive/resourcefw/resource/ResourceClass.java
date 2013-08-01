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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

import com.htmlhifive.resourcefw.service.ResourceManager;

/**
 * リソースクラスを宣言するためのアノテーション.<br>
 * リソースの名称、対応できるアイテムのタイプを設定できます.<br>
 * この情報は{@link ResourceManager ResourceManager}によって解析され、管理されます.
 *
 * @author kishigam
 * @see ResourceManager
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Inherited
@Service
public @interface ResourceClass {

	/**
	 * nameの未指定値
	 */
	final String UNDEFINED = "";

	/**
	 * あらゆるタイプに対応できることを表すtypeの値
	 */
	final String ALL_TYPES = "*/*";

	/**
	 * リソース名
	 */
	String name() default UNDEFINED;

	/**
	 * リソースがインプットとして対応できるタイプ
	 */
	String[] type() default {};
}
