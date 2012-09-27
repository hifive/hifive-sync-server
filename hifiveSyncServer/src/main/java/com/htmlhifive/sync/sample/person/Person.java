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
 * 人情報を保持するエンティティ.
 *
 * @author kishigam
 */
@Entity
@Table(name = "PERSON")
public class Person {

	/**
	 * IDのみ設定されたインスタンスを返します.
	 *
	 * @param personId
	 * @return インスタンス
	 */
	public static Person emptyPerson(String personId) {

		Person empty = new Person();
		empty.setPersonId(personId);

		return empty;
	}

	/**
	 * このエンティティのID.<br>
	 */
	@Id
	private String personId;

	/**
	 * この人の名前.
	 */
	private String name;

	/**
	 * この人の年齢.
	 */
	private Integer age;

	/**
	 * この人の所属組織
	 */
	private String organization;

	/**
	 * 人情報インスタンスを生成します.
	 */
	public Person() {
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof Person))
			return false;

		Person otherObj = (Person) obj;

		return EqualsBuilder.reflectionEquals(this, otherObj);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return HashCodeBuilder.reflectionHashCode(this);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return personId
	 */
	public String getPersonId() {
		return personId;
	}

	/**
	 * @param personId セットする personId
	 */
	public void setPersonId(String personId) {
		this.personId = personId;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name セットする name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return age
	 */
	public Integer getAge() {
		return age;
	}

	/**
	 * @param age セットする age
	 */
	public void setAge(Integer age) {
		this.age = age;
	}

	/**
	 * @return organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization セットする organization
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}
}
