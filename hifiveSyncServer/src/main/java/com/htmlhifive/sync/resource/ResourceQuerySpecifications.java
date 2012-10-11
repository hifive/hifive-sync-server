package com.htmlhifive.sync.resource;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specifications;

import com.htmlhifive.sync.common.ResourceItemCommonData;

/**
 * リソースアイテムをリソースクエリによって検索するためのSpring Specification定義インターフェース.<br>
 * 各リソースではこのインターフェースでリソースクエリ仕様を実装します.
 *
 * @author kishigam
 *
 * @param <E>
 *            このクエリ仕様が扱うエンティティの型
 */
public interface ResourceQuerySpecifications<E> {

    /**
     * 「指定された共通データが対応するリソースアイテムであり、かつデータ項目が指定された条件に合致する」
     * というクエリ仕様を表現するSpecificationsオブジェクトを返します.
     *
     * @param commonDataList
     *            リソースアイテム共通データ
     * @param conditions
     *            クエリ条件
     * @return Specificationsオブジェクト
     */
    Specifications<E> parseConditions(
            List<ResourceItemCommonData> commonDataList,
            Map<String, String[]> conditions);

}
