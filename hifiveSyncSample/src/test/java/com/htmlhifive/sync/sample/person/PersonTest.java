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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * <H3>
 * Personのテストクラス.</H3>
 *
 * @author kishigam
 */
public class PersonTest {

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(Person.class, notNullValue());
    }

    /**
     * {@link Person#Person()}用テストメソッド.
     */
    @Test
    public void testInstantiation() {
        Person target = new Person();
        assertThat(target, notNullValue());
    }

    /**
     * {@link Person#equals(Object)}用テストメソッド.
     */
    @Test
    public void testEquals() {

        final Person person = new Person() {
            {
                setPersonId("a");
                setName("person");
                setOrganization("org");
                setAge(0);
            }
        };

        Person eq = new Person() {
            {
                setPersonId(person.getPersonId());
                setName(person.getName());
                setOrganization(person.getOrganization());
                setAge(person.getAge());
            }
        };
        assertThat(person, is(equalTo(eq)));

        Person ne1 = new Person() {
            {
                setPersonId("b");
                setName(person.getName());
                setOrganization(person.getOrganization());
                setAge(person.getAge());
            }
        };
        assertThat(person, is(not(equalTo(ne1))));

        Person ne2 = new Person() {
            {
                setPersonId(person.getPersonId());
                setName("otherPerson");
                setOrganization(person.getOrganization());
                setAge(person.getAge());
            }
        };
        assertThat(person, is(not(equalTo(ne2))));

        Person ne3 = new Person() {
            {
                setPersonId(person.getPersonId());
                setName(person.getName());
                setOrganization("otherOrg");
                setAge(person.getAge());
            }
        };
        assertThat(person, is(not(equalTo(ne3))));

        Person ne4 = new Person() {
            {
                setPersonId(person.getPersonId());
                setName(person.getName());
                setOrganization(person.getOrganization());
                setAge(10);
            }
        };
        assertThat(person, is(not(equalTo(ne4))));

    }

    /**
     * {@link Person#hashCode()}用テストメソッド.
     */
    @Test
    public void testHashCode() {

        final Person person = new Person() {
            {
                setPersonId("person");
                setName("person");
                setOrganization("org");
                setAge(0);
            }
        };

        Person eq = new Person() {
            {
                setPersonId("person");
                setName("person");
                setOrganization("org");
                setAge(0);
            }
        };
        assertThat(person.hashCode(), is(equalTo(eq.hashCode())));

        Person ne = new Person() {
            {
                setPersonId(person.getPersonId());
                setName("person1");
                setOrganization("org1");
                setAge(10);
            }
        };
        assertThat(person.hashCode(), is(not(equalTo(ne.hashCode()))));
    }
}
