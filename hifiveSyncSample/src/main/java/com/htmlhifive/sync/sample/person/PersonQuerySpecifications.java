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
package com.htmlhifive.sync.sample.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;

/**
 * Personをリソースクエリによって検索するためのSpring Specification定義クラス.
 */
@Service
public class PersonQuerySpecifications implements ResourceQuerySpecifications<Person> {

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
    @Override
    public Specifications<Person> parseConditions(
            List<ResourceItemCommonData> commonDataList,
            Map<String, String[]> conditions) {

        List<Specification<Person>> specList = new ArrayList<>();

        List<String> targetItemIdList = new ArrayList<>();
        for (ResourceItemCommonData common : commonDataList) {
            targetItemIdList.add(common.getTargetItemId());
        }
        if (!targetItemIdList.isEmpty()) {
            specList.add(isInIds(targetItemIdList.toArray(new String[] {})));
        }

        for (String cond : conditions.keySet()) {
            switch (cond) {
            case ("personId"):
                specList.add(isInIds(conditions.get(cond)));
                break;
            case ("organization"):
                specList.add(isEqualOrganization(conditions.get(cond)[0]));
                break;

            // ・・・

            default:
                // 何もしない
            }
        }

        if (specList.isEmpty()) {
            return null;
        }

        Specifications<Person> specs = Specifications.where(specList.get(0));
        for (Specification<Person> spec : specList.subList(1, specList.size())) {
            specs = specs.and(spec);
        }
        return specs;
    }

    /**
     * 配列に含むIDのいずれかに合致するPersonを抽出するSpecificationを返します.
     *
     * @param ids
     *            IDの配列
     * @return Specificationオブジェクト
     */
    private Specification<Person> isInIds(final String... ids) {
        return new Specification<Person>() {

            @Override
            public Predicate toPredicate(
                    Root<Person> root,
                    CriteriaQuery<?> cq,
                    CriteriaBuilder builder) {

                return root.get(root.getModel().getSingularAttribute("personId", String.class)).in(
                        (Object[])ids);
            }
        };
    }

    /**
     * organizationが一致するPersonを抽出するSpecificationを返します.
     *
     * @param organization
     * @return Specificationオブジェクト
     */
    private Specification<Person> isEqualOrganization(final String organization) {
        return new Specification<Person>() {

            @Override
            public Predicate toPredicate(
                    Root<Person> root,
                    CriteriaQuery<?> cq,
                    CriteriaBuilder builder) {

                return builder.equal(
                        root.get(root.getModel().getSingularAttribute("organization", String.class)),
                        organization);
            }
        };
    }
}
