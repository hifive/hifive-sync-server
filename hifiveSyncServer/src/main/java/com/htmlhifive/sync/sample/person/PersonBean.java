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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 人情報を保持するエンティティクラス.<br>
 * 予定情報の関係者として設定されます.
 *
 * @author kishigam
 */
@Entity
@Table(name = "PERSON")
public class PersonBean {

    /**
     * このエンティティのID.<br>
     */
    @Id
    private String id;

    /**
     * この人の名前.
     */
    private String name;

    /**
     * この人の年齢.
     */
    private int age;

    /**
     * この人の所属組織
     */
    private String organization;

    /**
     * プライベートのデフォルトコンストラクタ. <br>
     * 永続マネージャーが使用するため、実装する必要があります.
     */
    @SuppressWarnings("unused")
    private PersonBean() {
    }

    /**
     * idを指定して新規エンティティを生成します.
     *
     * @param id
     *            セットするid
     */
    PersonBean(String id) {

        this.id = id;
    }

    /**
     * idが同一であれば同一とします.
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (!(obj instanceof PersonBean))
            return false;

        PersonBean person = (PersonBean)obj;

        return new EqualsBuilder().append(this.id, person.id).isEquals();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 37).append(id).hashCode();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            セットする name
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * @return age
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age
     *            セットする age
     */
    void setAge(int age) {
        this.age = age;
    }

    /**
     * @return organization
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization
     *            セットする organization
     */
    void setOrganization(String organization) {
        this.organization = organization;
    }

}
