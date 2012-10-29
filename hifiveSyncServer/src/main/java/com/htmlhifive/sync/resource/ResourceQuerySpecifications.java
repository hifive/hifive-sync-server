package com.htmlhifive.sync.resource;

import java.util.Map;

import org.springframework.data.jpa.domain.Specifications;

/**
 * リソースアイテムをリソースクエリによって検索するためのSpring Specification定義インターフェース.<br>
 * 各リソースではこのインターフェースでリソースクエリ仕様を実装します.
 *
 * @author kishigam
 * @param <E> このクエリ仕様が扱うエンティティの型
 */
public interface ResourceQuerySpecifications<E> {

	/**
	 * 「指定された識別子を持ち、かつデータ項目が指定された条件に合致する」 というクエリ仕様を表現するSpecificationsオブジェクトを返します.
	 *
	 * @param conditions クエリ条件
	 * @param ids リソースアイテムの識別子(複数可)
	 * @return Specificationsオブジェクト
	 */
	Specifications<E> parseConditions(Map<String, String[]> conditions, String... ids);
}
