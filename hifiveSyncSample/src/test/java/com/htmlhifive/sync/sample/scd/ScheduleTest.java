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
package com.htmlhifive.sync.sample.scd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.htmlhifive.sync.sample.person.Person;

/**
 * <H3>
 * Scheduleのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ScheduleTest {

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(Schedule.class, notNullValue());
    }

    /**
     * {@link Schedule#Schedule()}用テストメソッド.
     */
    @Test
    public void testInstantiation() {

        String scheduleId = "scheduleId";

        Schedule scd1 = new Schedule();
        scd1.setScheduleId(scheduleId);

        Schedule scd2 = new Schedule(scheduleId);
        assertThat(scd1, notNullValue());
        assertThat(scd2, notNullValue());

        assertThat(scd1, is(equalTo(scd2)));
    }

    /**
     * {@link Schedule#equals(Object)}用テストメソッド.
     */
    @SuppressWarnings("serial")
    @Test
    public void testEquals() {

        final String scheduleId = "scheduleId";
        final String otherScheduleId = "other scheduleId";

        final Person person1 = new Person() {
            {
                setPersonId("person1");
            }
        };
        final Person person2 = new Person() {
            {
                setPersonId("person2");
            }
        };

        final List<Person> userBeans = new ArrayList<Person>() {
            {
                add(person1);
                add(person2);
            }
        };
        final List<Person> otherUserBeans = new ArrayList<Person>() {
            {
                add(person2);
            }
        };

        final String title = "title";
        final String otherTitle = "other title";

        final String category = "category";
        final String otherCategory = "other category";

        final int date1 = 20010101;
        final int date2 = 20111111;

        final String startTime = "100";
        final String otherStartTime = "200";

        final String finishTime = "1000";
        final String otherFinishTime = "2000";

        final String detail = "detail";
        final String otherDetail = "other detail";

        final String place = "place";
        final String otherPlace = "other place";

        final Person createUser = person1;
        final Person otherCreateUser = person2;

        final Schedule target = new Schedule(scheduleId) {
            {
                setUserBeans(userBeans);
                setTitle(title);
                setCategory(category);
                setStartTime(startTime);
                setFinishTime(finishTime);
                setDetail(detail);
                setPlace(place);
                setCreateUser(createUser);
            }
        };
        target.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(target, date2));
            }
        });

        final Schedule other = new Schedule(scheduleId) {
            {
                setUserBeans(userBeans);
                setTitle(title);
                setCategory(category);
                setStartTime(startTime);
                setFinishTime(finishTime);
                setDetail(detail);
                setPlace(place);
                setCreateUser(createUser);
            }
        };
        other.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(other, date2));
            }
        });

        assertThat(target.equals(other), is(true));

        other.setScheduleId(otherScheduleId);
        assertThat(target.equals(other), is(false));

        other.setScheduleId(scheduleId);
        other.setUserBeans(otherUserBeans);
        assertThat(target.equals(other), is(false));

        other.setUserBeans(userBeans);
        other.setTitle(otherTitle);
        assertThat(target.equals(other), is(false));

        other.setTitle(title);
        other.setCategory(otherCategory);
        assertThat(target.equals(other), is(false));

        other.setCategory(category);
        other.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(other, date1));
                add(new ScheduleDate(other, date2));
            }
        });
        assertThat(target.equals(other), is(false));

        other.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(other, date2));
            }
        });
        other.setStartTime(otherStartTime);
        assertThat(target.equals(other), is(false));

        other.setStartTime(startTime);
        other.setFinishTime(otherFinishTime);
        assertThat(target.equals(other), is(false));

        other.setFinishTime(finishTime);
        other.setDetail(otherDetail);
        assertThat(target.equals(other), is(false));

        other.setDetail(detail);
        other.setPlace(otherPlace);
        assertThat(target.equals(other), is(false));

        other.setPlace(place);
        other.setCreateUser(otherCreateUser);
        assertThat(target.equals(other), is(false));
    }
}
