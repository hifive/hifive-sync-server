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

/**
 * <H3>
 * ScheduleResourceItemのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ScheduleResourceItemTest {

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(ScheduleResourceItem.class, notNullValue());
    }

    /**
     * {@link ScheduleResourceItem#ScheduleResourceItem()}用テストメソッド.
     */
    @Test
    public void testInstantiation() {

        String scheduleId = "scheduleId";

        ScheduleResourceItem target = new ScheduleResourceItem(scheduleId);

        assertThat(target, notNullValue());
        assertThat(target.getScheduleId(), is(equalTo(scheduleId)));
    }

    /**
     * {@link ScheduleResourceItem#equals(Object)}用テストメソッド.
     */
    @SuppressWarnings("serial")
    @Test
    public void testEquals() {

        final String scheduleId = "scheduleId";
        final String otherScheduleId = "other scheduleId";

        final List<String> userIds = new ArrayList<String>() {
            {
                add("person1");
                add("person2");
            }
        };
        final List<String> otherUserIds = new ArrayList<String>() {
            {
                add("person2");
            }
        };

        final String title = "title";
        final String otherTitle = "other title";

        final String category = "category";
        final String otherCategory = "other category";

        final List<String> dates = new ArrayList<String>() {
            {
                add("20010101");
                add("20111111");
            }
        };
        final List<String> otherDates = new ArrayList<String>() {
            {
                add("20111111");
            }
        };

        final String startTime = "100";
        final String otherStartTime = "200";

        final String finishTime = "1000";
        final String otherFinishTime = "2000";

        final String detail = "detail";
        final String otherDetail = "other detail";

        final String place = "place";
        final String otherPlace = "other place";

        final String createUserName = "person1Name";
        final String otherCreateUserName = "person2name";

        // scheduleIdの違いによる同一性判定の検証
        assertThat(new ScheduleResourceItem(scheduleId).equals(new ScheduleResourceItem(
                otherScheduleId)), is(false));

        final ScheduleResourceItem target = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(userIds);
                setTitle(title);
                setCategory(category);
                setDates(dates);
                setStartTime(startTime);
                setFinishTime(finishTime);
                setDetail(detail);
                setPlace(place);
                setCreateUserName(createUserName);
            }
        };

        final ScheduleResourceItem other = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(userIds);
                setTitle(title);
                setCategory(category);
                setDates(dates);
                setStartTime(startTime);
                setFinishTime(finishTime);
                setDetail(detail);
                setPlace(place);
                setCreateUserName(createUserName);
            }
        };

        assertThat(target.equals(other), is(true));

        other.setUserIds(otherUserIds);
        assertThat(target.equals(other), is(false));

        other.setUserIds(userIds);
        other.setTitle(otherTitle);
        assertThat(target.equals(other), is(false));

        other.setTitle(title);
        other.setCategory(otherCategory);
        assertThat(target.equals(other), is(false));

        other.setCategory(category);
        other.setDates(otherDates);
        assertThat(target.equals(other), is(false));

        other.setDates(dates);
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
        other.setCreateUserName(otherCreateUserName);
        assertThat(target.equals(other), is(false));
    }
}
