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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 予定情報における予定日情報を保持するエンティティクラス.<br>
 *
 * @author kishigam
 */
@Entity
@Table(name = "SCHEDULE_DATE")
public class ScheduleDateBean {

    /**
     * このエンティティのID.
     */
    @Id
    @GeneratedValue
    private int id;

    /**
     * 予定日付.
     */
    private String scheduleDate;

    /**
     * この日付が設定された予定エンティティ.
     */
    @ManyToOne
    private Schedule schedule;

    /**
     * プライベートのデフォルトコンストラクタ. <br>
     * 永続マネージャーが使用するため、実装する必要があります.
     */
    @SuppressWarnings("unused")
    private ScheduleDateBean() {
    }

    /**
     * 予定エンティティおよびその予定日付を指定して新規予定日付エンティティを生成します.
     *
     * @param schedule
     *            予定エンティティ
     * @param date
     *            日付文字列
     */
    public ScheduleDateBean(Schedule schedule, String date) {
        this.schedule = schedule;
        this.scheduleDate = date;
    }

    /**
     * IDを除くフィールドの値が同一の時、同一とします.
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (!(obj instanceof ScheduleDateBean))
            return false;

        return EqualsBuilder.reflectionEquals(this, (ScheduleDateBean)obj, "id");
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this, "id");
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
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            セットする id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return scheduleDate
     */
    public String getScheduleDate() {
        return scheduleDate;
    }

    /**
     * @param scheduleDate
     *            セットする scheduleDate
     */
    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    /**
     * @return schedule
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * @param schedule
     *            セットする schedule
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

}
