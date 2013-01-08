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
package com.htmlhifive.sync.sample.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.resource.AbstractResourceQuerySpecifications;

/**
 * Personをリソースクエリによって検索するためのSpring Specification定義クラス.
 */
@Service
public class PersonQuerySpecifications extends AbstractResourceQuerySpecifications<Person> {

    /**
     * クエリ条件を解析します.<br>
     * Personの検索条件を解析し、Specificationオブジェクトのリストを返します.
     *
     * @param conditions
     *            クエリ条件
     * @return Specificationオブジェクトのリスト
     */
    @Override
    public List<Specification<Person>> doParseConditions(Map<String, String[]> conditions) {

        List<Specification<Person>> specList = new ArrayList<>();

        for (String cond : conditions.keySet()) {
            switch (cond) {
            case ("personId"):
                specList.add(super.isInIds(conditions.get(cond)));
                break;
            case ("organization"):
                specList.add(isEqualOrganization(conditions.get(cond)[0]));
                break;

            // ・・・

            default:
                // 何もしない
            }
        }

        return specList;
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
