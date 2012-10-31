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
        Schedule otherSchedule = new Schedule("scheduleId2");

        int date = 20010101;
        int otherDate = 20111111;

        ScheduleDate target = new ScheduleDate(schedule, date);
        ScheduleDate eq1 = new ScheduleDate(schedule, date);
        ScheduleDate eq2 = new ScheduleDate(otherSchedule, date);
        ScheduleDate ne = new ScheduleDate(schedule, otherDate);

        assertThat(target.equals(eq1), is(true));
        // scheduleは同一性判定に含めない
        assertThat(target.equals(eq2), is(true));
        assertThat(target.equals(ne), is(false));
    }
}
