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
package com.htmlhifive.resourcefw.resource.query;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specifications;

/**
 * リソースアイテムをリソースクエリによって検索するためのSpring Specification定義インターフェース.<br>
 * 各リソースではこのインターフェースでリソースクエリ仕様を実装します.
 *
 * @author kishigam
 * @param <E> このクエリ仕様が扱うエンティティの型
 * @see AbstractResourceQuerySpecifications
 */
public interface ResourceQuerySpecifications<E> {

	/**
	 * 条件名と条件値リストのMapをMapオブジェクトを解析し、 クエリ仕様を表現するSpecificationsオブジェクトを生成して返します.<br>
	 *
	 * @param conditions クエリ条件
	 * @return Specificationsオブジェクト
	 */
	Specifications<E> parseConditions(Map<String, List<String>> conditions);
}
