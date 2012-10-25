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

import com.htmlhifive.sync.resource.common.ResourceItemCommonData;

/**
 * リソースクエリによるリソースアイテム検索のためのSpring Specifications抽象クラス.
 *
 * @author kishigam
 * @param <T> Specificationの対象エンティティ
 */
public abstract class AbstractResourceQuerySpecifications<T> implements ResourceQuerySpecifications<T> {

	/**
	 * 「指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致する」 というクエリ仕様を表現するSpecificationsオブジェクトを返します.
	 *
	 * @param commonDataList リソースアイテム共通データ
	 * @param conditions クエリ条件
	 * @return Specificationsオブジェクト
	 */
	@Override
	public Specifications<T> parseConditions(List<ResourceItemCommonData> commonDataList,
			Map<String, String[]> conditions) {

		List<Specification<T>> specList = new ArrayList<>();

		// 共通データの条件をSpecに追加
		specList.add(createResourceItemCommonDataSpec(commonDataList));

		// サブクラスで対象エンティティ固有の検索条件を追加
		specList.addAll(doParseConditions(conditions));

		Specifications<T> specs = Specifications.where(specList.get(0));
		for (Specification<T> spec : specList.subList(1, specList.size())) {
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
	protected abstract Collection<? extends Specification<T>> doParseConditions(Map<String, String[]> conditions);

	/**
	 * リソースアイテム共通データが持つ対象アイテムIDをIDとして持つエンティティを抽出するSpecificationオブジェクトを返します.
	 *
	 * @param commonDataList リソースアイテム共通データのリスト
	 * @return Specificationオブジェクト
	 */
	protected Specification<T> createResourceItemCommonDataSpec(List<ResourceItemCommonData> commonDataList) {

		List<String> targetItemIds = new ArrayList<>();
		for (ResourceItemCommonData common : commonDataList) {
			targetItemIds.add(common.getTargetItemId());
		}

		// IDがないときは空ではなく、nullを格納しなければならない
		Specification<T> commonDataSpec = isInIds(targetItemIds.isEmpty() ? new String[] { null } : targetItemIds
				.toArray(new String[] {}));

		return commonDataSpec;
	}

	/**
	 * 配列に含むIDのいずれかに合致するエンティティを抽出するSpecificationを返します.<br>
	 *
	 * @param ids IDの配列
	 * @return Specificationオブジェクト
	 */
	protected Specification<T> isInIds(final String... ids) {
		return new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> cq, CriteriaBuilder builder) {

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
		Class<T> entityClass = (Class<T>) thisType.getActualTypeArguments()[0];

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