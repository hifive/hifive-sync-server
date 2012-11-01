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
package com.htmlhifive.sync.sample.ctrl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.security.Principal;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.sample.person.Person;
import com.htmlhifive.sync.sample.person.PersonResource;

/**
 * <H3>
 * PersonControllerのテストクラス.</H3>
 *
 * @author kishigam
 */
public class PersonControllerTest {

    @Mocked
    private PersonResource personResource;

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(PersonController.class, notNullValue());
    }

    /**
     * {@link PersonController#PersonController()}用テストメソッド.
     */
    @Test
    public void testInstantiation() {
        PersonController target = new PersonController();
        assertThat(target, notNullValue());
    }

    /**
     * {@link PersonController#getUserId(Principal)}用テストメソッド.
     *
     * @param principal
     *            引数のモック
     */
    @SuppressWarnings("serial")
    @Test
    public void testGetUserId(@Mocked final Principal principal) {

        // Arrange：正常系
        final PersonController target = new PersonController();

        final String personId = "personId";
        final Person expectedPerson = new Person() {
            {
                setPersonId(personId);
            }
        };

        new Expectations() {
            {
                setField(target, personResource);

                principal.getName();
                result = "personId";

                personResource.getResourceItemByPersonId(personId);
                result = expectedPerson;
            }
        };

        // Act
        ResponseEntity<Person> actual = target.getUserId(principal);

        // Assert：結果が正しいこと
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Person> expected =
                new ResponseEntity<>(expectedPerson, headers, HttpStatus.OK);

        assertThat(actual.getBody(), is(equalTo(expectedPerson)));
        assertThat(actual.getHeaders(), is(equalTo(expected.getHeaders())));
        assertThat(actual.getStatusCode(), is(equalTo(expected.getStatusCode())));

        // 上記3プロパティがequalでもResponseEntityはequalにならない(equalsメソッドがObject#equalsのため)
        // assertThat(actual, is(equalTo(expected)));
    }

    /**
     * {@link PersonController#getUserId(Principal)}用テストメソッド.<br>
     * Personが存在せず{@link BadRequestException}
     * がスローされた場合、principalオブジェクトのnameをIDとするPersonオブジェクトを返す.
     *
     * @param principal
     *            引数のモック
     */
    @SuppressWarnings("serial")
    @Test
    public void testGetUserIdWhenPersonNotFound(@Mocked final Principal principal) {

        // Arrange：正常系
        final PersonController target = new PersonController();

        final String personId = "personId";
        final Person expectedPerson = new Person() {
            {
                setPersonId(personId);
            }
        };

        new Expectations() {
            {
                setField(target, personResource);

                principal.getName();
                result = "personId";

                personResource.getResourceItemByPersonId(personId);
                result = new BadRequestException();

                principal.getName();
                result = "personId";
            }
        };

        // Act
        ResponseEntity<Person> actual = target.getUserId(principal);

        // Assert：結果が正しいこと
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Person> expected =
                new ResponseEntity<>(expectedPerson, headers, HttpStatus.OK);

        assertThat(actual.getBody(), is(equalTo(expectedPerson)));
        assertThat(actual.getHeaders(), is(equalTo(expected.getHeaders())));
        assertThat(actual.getStatusCode(), is(equalTo(expected.getStatusCode())));

        // 上記3プロパティがequalでもResponseEntityはequalにならない(equalsメソッドがObject#equalsのため)
        // assertThat(actual, is(equalTo(expected)));}
    }
}
