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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

/**
 * リソースクエリによるリソースアイテム検索のためのSpring Specifications抽象クラス.
 *
 * @author kishigam
 * @param <E> Specificationの対象エンティティ
 */
public abstract class AbstractResourceQuerySpecifications<E> implements ResourceQuerySpecifications<E> {

	private static final String FILED_SERIAL_VERSION_UID = "serialVersionUID";

	/**
	 * 条件名と条件値リストのMapをMapオブジェクトを解析し、 クエリ仕様を表現するSpecificationsオブジェクトを生成して返します.<br>
	 * この実装では、条件名がエンティティフィールド(カラム)名とし、条件値リストのいずれかがそのフィールド(カラム)の値に一致するエンティティを返すSpecificationsオブジェクトを生成します.<br>
	 * (in句の実装)
	 *
	 * @param conditions クエリ条件
	 * @return Specificationsオブジェクト
	 */
	@Override
	public Specifications<E> parseConditions(Map<String, List<String>> conditions) {

		List<Specification<E>> specList = new ArrayList<>();

		for (Field field : getEntityFields()) {
			// 対象エンティティのフィールド名のクエリー条件があれば、in句のSpecificationを追加する
			List<String> valueListOfCondition = conditions.get(field.getName());
			if (valueListOfCondition != null && !valueListOfCondition.isEmpty()) {

				String[] valueArrayOfCondition = (String[]) valueListOfCondition
						.toArray(new String[valueListOfCondition.size()]);

				specList.add(isIn(field.getName(), field.getType(), valueArrayOfCondition));

				// ここでSpecification化された条件を削除
				conditions.remove(field.getName());
			}
		}

		// サブクラスで対象エンティティ固有の条件を追加
		specList.addAll(doParseConditions(conditions));

		if (specList.isEmpty()) {
			return null;
		}

		Specifications<E> specs = Specifications.where(specList.get(0));
		for (Specification<E> spec : specList.subList(1, specList.size())) {
			specs = specs.and(spec);
		}

		return specs;
	}

	/**
	 * クエリ条件を解析します.<br>
	 * 各サブクラスが対象とするリソースアイテムの検索条件を解析し、Specificationオブジェクトのリストを返します.<br>
	 * 条件がない場合は空のリストを返す必要があります.
	 *
	 * @param conditions クエリ条件
	 * @return Specificationオブジェクトのリスト
	 */
	protected abstract Collection<? extends Specification<E>> doParseConditions(Map<String, List<String>> conditions);

	/**
	 * 配列に含むIDのいずれかに合致するエンティティを抽出するSpecificationを返します.<br>
	 *
	 * @param fieldName フィールド名
	 * @param fieldType フィールドの型
	 * @param ids フィールド値(文字列)の配列
	 * @return Specificationオブジェクト
	 */
	protected Specification<E> isIn(final String fieldName, final Class<?> fieldType, final String... ids) {
		return new Specification<E>() {
			@Override
			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {
				SingularAttribute<? super E, ?> singularAttribute = root.getModel().getSingularAttribute(fieldName,
						fieldType);
				return root.get(singularAttribute).in((Object[]) ids);
			}
		};
	}

	/**
	 * 実型引数を参照し、対象エンティティ自身に定義されたフィールドをリストで返します.<br>
	 * {@link Transient Transient}アノテーションが付与されているフィールド、コレクション型のフィールドは除外されます.
	 */
	protected List<Field> getEntityFields() {

		// この抽象クラスの型を取得
		ParameterizedType thisType = (ParameterizedType) this.getClass().getGenericSuperclass();

		// この抽象クラスで1つ目の型変数に指定されているのがエンティティの型
		@SuppressWarnings("unchecked")
		Class<E> entityClass = (Class<E>) thisType.getActualTypeArguments()[0];

		List<Field> fields = new ArrayList<>();
		// フィールドを検索し、@Idがついたフィールド名を返す
		for (Field field : entityClass.getDeclaredFields()) {

			if (field.isAnnotationPresent(Transient.class))
				continue;

			if (Collection.class.isAssignableFrom(field.getType()))
				continue;

			if (field.getName().equals(FILED_SERIAL_VERSION_UID))
				continue;

			field.setAccessible(true);
			fields.add(field);
		}
		return fields;
	}
}