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
package com.htmlhifive.sync.sample.scd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Table;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.htmlhifive.sync.sample.person.Person;
import com.htmlhifive.sync.sample.person.PersonRepository;
import com.htmlhifive.sync.service.upload.UploadCommonDataRepository;

/**
 * <H3>
 * ScheduleRepositoryのテストクラス.</H3>
 *
 * @author kishigam
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(transactionManager = "txManager")
public class ScheduleRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final String TABLE_NAME = Schedule.class.getAnnotation(Table.class).name();
    private static final String TABLE_NAME_PERSON = Person.class.getAnnotation(Table.class).name();
    private static final String TABLE_NAME_DATE =
            ScheduleDate.class.getAnnotation(Table.class).name();

    @Resource
    private ScheduleRepository target;

    @Resource
    private PersonRepository personRepository;

    private Schedule data1;
    private Schedule data2;

    private Person person1;
    private Person person2;
    private Person person3;

    @SuppressWarnings("serial")
    @Before
    public void setUp() {

        deleteFromTables(TABLE_NAME_PERSON);
        deleteFromTables(TABLE_NAME_DATE);
        deleteFromTables(TABLE_NAME);

        person1 = new Person();
        person1.setPersonId("person1");

        person2 = new Person();
        person2.setPersonId("person2");

        personRepository.save(person1);
        personRepository.save(person2);

        // 非永続データ
        person3 = new Person();
        person3.setPersonId("person3");

        data1 = new Schedule();
        data1.setScheduleId("scheduleId1");
        data1.setUserBeans(new ArrayList<Person>() {
            {
                add(person1);
                add(person2);
            }
        });
        data1.setTitle("title1");
        data1.setCategory("category1");
        data1.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(data1, 20010101));
                add(new ScheduleDate(data1, 20111111));
            }
        });
        data1.setStartTime("100");
        data1.setFinishTime("200");
        data1.setDetail("detail1");
        data1.setPlace("place1");
        data1.setCreateUser(person1);

        data2 = new Schedule();
        data2.setScheduleId("scheduleId2");
        // not set userBeans
        data2.setTitle("title2");
        data2.setCategory("category2");
        // not set dateBeans
        data2.setStartTime("10000");
        data2.setFinishTime("20000");
        data2.setDetail("detail2");
        data2.setPlace("place2");
        data2.setCreateUser(person2);

        target.save(data1);
        target.save(data2);
    }

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(ScheduleRepository.class, notNullValue());
    }

    /**
     * {@link UploadCommonDataRepository#findOne()}用テストメソッド.<br>
     */
    @Test
    public void testFindOne() {

        Schedule actual = target.findOne("scheduleId1");
        assertThat(actual, is(equalTo(data1)));

        Schedule actual2 = target.findOne("scheduleId2");
        assertThat(actual2, is(equalTo(data2)));

        Schedule actual3 = target.findOne("notExist");
        assertThat(actual3, is(nullValue()));
    }

    @SuppressWarnings("serial")
    @Test(expected = EntityNotFoundException.class)
    public void testSaveFailBecauseOfNotPersistedPerson() throws Throwable {

        Schedule data = new Schedule();
        data.setScheduleId("scheduleId3");
        data.setUserBeans(new ArrayList<Person>() {
            {
                add(person3);
            }
        });

        try {
            target.save(data);
        } catch (Exception e) {
            // JPAの例外がSpringの例外にラップされている
            throw e.getCause();
        }
    }
}
