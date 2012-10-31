/**
 *
 */
package com.htmlhifive.sync.sample.scd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * <H3>
 * ScheduleDateのテストクラス.</H3>
 *
 * @author kishigam
 */
public class ScheduleDateTest {

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(ScheduleDate.class, notNullValue());
    }

    /**
     * {@link ScheduleDate#ScheduleDate()}用テストメソッド.
     */
    @Test
    public void testInstantiation() {

        Schedule schedule = new Schedule("scheduleId");
        int date = 20010101;

        ScheduleDate target = new ScheduleDate(schedule, date);

        assertThat(target, notNullValue());
        assertThat(target.getSchedule(), is(equalTo(schedule)));
        assertThat(target.getDate(), is(equalTo(date)));
        assertThat(target.getId(), is(0L));
    }

    /**
     * {@link ScheduleDate#equals(Object)}用テストメソッド.
     */
    @Test
    public void testEquals() {

        Schedule schedule = new Schedule("scheduleId1");
        Schedule anotherSchedule = new Schedule("scheduleId2");

        int date = 20010101;
        int anotherDate = 20111111;

        ScheduleDate target = new ScheduleDate(schedule, date);
        ScheduleDate eq = new ScheduleDate(schedule, date);
        ScheduleDate ne1 = new ScheduleDate(anotherSchedule, date);
        ScheduleDate ne2 = new ScheduleDate(schedule, anotherDate);

        assertThat(target.equals(eq), is(true));
        assertThat(target.equals(ne1), is(false));
        assertThat(target.equals(ne2), is(false));
    }
}
