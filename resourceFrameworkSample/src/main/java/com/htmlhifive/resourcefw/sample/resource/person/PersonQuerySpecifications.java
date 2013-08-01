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
 */package com.htmlhifive.resourcefw.sample.resource.person;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.htmlhifive.resourcefw.resource.query.AbstractResourceQuerySpecifications;

/**
 * Personをリソースクエリによって検索するためのSpring Specification定義クラス.
 */
@Service
public class PersonQuerySpecifications extends AbstractResourceQuerySpecifications<Person> {

	/**
	 * クエリ条件を解析します.<br>
	 * 独自のクエリ条件を持たないため、空のリストを返します.
	 *
	 * @param conditions クエリ条件
	 * @return Specificationオブジェクトのリスト
	 */
	@Override
	public List<Specification<Person>> doParseConditions(Map<String, List<String>> conditions) {

		return Collections.emptyList();
	}
}
