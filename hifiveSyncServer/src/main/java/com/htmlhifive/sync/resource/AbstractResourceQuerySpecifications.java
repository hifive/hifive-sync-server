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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

/**
 * リソースクエリによるリソースアイテム検索のためのSpring Specifications抽象クラス.
 *
 * @author kishigam
 * @param <E> Specificationの対象エンティティ
 */
public abstract class AbstractResourceQuerySpecifications<E> implements ResourceQuerySpecifications<E> {

	/**
	 * 「指定された識別子を持ち、かつデータ項目が指定された条件に合致する」 というクエリ仕様を表現するSpecificationsオブジェクトを返します.
	 *
	 * @param conditions クエリ条件
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return Specificationsオブジェクト
	 */
	@Override
	public Specifications<E> parseConditions(Map<String, String[]> conditions, String... ids) {

		List<Specification<E>> specList = new ArrayList<>();

		// 対象ID(識別子)の条件をSpecに追加
		specList.add(createItemIdentifierSpec(ids));

		// サブクラスで対象エンティティ固有の検索条件を追加
		specList.addAll(doParseConditions(conditions));

		Specifications<E> specs = Specifications.where(specList.get(0));
		for (Specification<E> spec : specList.subList(1, specList.size())) {
			specs = specs.and(spec);
		}

		return specs;
	}

	/**
	 * クエリ条件を解析します.<br>
	 * 各サブクラスが対象とするリソースアイテムの検索条件を解析し、Specificationオブジェクトのリストを返します.
	 *
	 * @param conditions クエリ条件
	 * @return Specificationオブジェクトのリスト
	 */
	protected abstract Collection<? extends Specification<E>> doParseConditions(Map<String, String[]> conditions);

	/**
	 * 対象アイテムの識別子を持つエンティティを抽出するSpecificationオブジェクトを返します.
	 *
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return Specificationオブジェクト
	 */
	protected Specification<E> createItemIdentifierSpec(String... ids) {

		// IDがないときは空ではなく、nullを格納しなければならない
		return isInIds(ids == null || ids.length == 0 ? new String[] { null } : ids);
	}

	/**
	 * 配列に含むIDのいずれかに合致するエンティティを抽出するSpecificationを返します.<br>
	 *
	 * @param ids IDの配列
	 * @return Specificationオブジェクト
	 */
	protected Specification<E> isInIds(final String... ids) {
		return new Specification<E>() {

			@Override
			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {

				return root.get(root.getModel().getSingularAttribute(entityIdFieldName(), String.class)).in(
						(Object[]) ids);
			}
		};
	}

	/**
	 * 実型引数を参照し、対象エンティティにおいてIDを保持するフィールド名を取得します.
	 *
	 * @return IDフィールド名
	 */
	@SuppressWarnings("unchecked")
	private String entityIdFieldName() {

		// この抽象クラスの型を取得
		ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();

		// この抽象クラスで1つ目の型変数に指定されているのがエンティティの型
		Class<E> entityClass = (Class<E>) thisType.getActualTypeArguments()[0];

		// フィールドを検索し、@Idがついたフィールド名を返す
		for (Field field : entityClass.getDeclaredFields()) {

			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				return field.getName();
			}
		}
		return null;
	}
}