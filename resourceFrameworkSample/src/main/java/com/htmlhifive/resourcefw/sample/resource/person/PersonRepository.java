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
package com.htmlhifive.resourcefw.sample.resource.person;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Personエンティティを永続化するためのリポジトリ. <br>
 * SpringFrameworkの標準的なリポジトリインターフェースを継承します.
 *
 * @author kishigam
 */
public interface PersonRepository extends JpaRepository<Person, String>, JpaSpecificationExecutor<Person> {

	/**
	 * 悲観的ロックを使用してIDでPersonを検索し、返します.<br>
	 *
	 * @param personId PersonのID
	 * @return Person
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT d FROM Person d WHERE d.personId = :personId")
	Person findOneForUpdate(@Param("personId") String personId);
}