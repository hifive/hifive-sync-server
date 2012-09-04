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

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 予定リソースが外部と受け渡しするリソースエレメントクラス.
 *
 * @author kishigam
 */
public class ScheduleResourceElement {

    /**
     * このエレメントが示す予定のID.
     */
    private String scheduleId;

    /**
     * このエレメントが示す予定の対象者(文字列IDのリスト).
     */
    private List<String> userIds;

    /**
     * このエレメントが示す予定のタイトル.
     */
    private String title;

    /**
     * このエレメントが示す予定の種類.
     */
    private String category;

    /**
     * このエレメントが示す予定の予定日(文字列)のリスト.
     */
    private List<String> dates;

    /**
     * このエレメントが示す予定の開始時刻.
     */
    private String startTime;

    /**
     * このエレメントが示す予定の終了時刻.
     */
    private String finishTime;

    /**
     * このエレメントが示す予定の詳細情報.
     */
    private String detail;

    /**
     * このエレメントが示す予定の実施場所.
     */
    private String place;

    /**
     * 予定IDが一致する場合同一となるよう同一性判定します.<br>
     * 他のフィールドが全て同一の予定であっても同一とはなりません.
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ScheduleResourceElement)) {
            return false;
        }

        ScheduleResourceElement element = (ScheduleResourceElement)obj;

        return new EqualsBuilder().append(this.scheduleId, element.scheduleId).isEquals();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 37).append(this.scheduleId).hashCode();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return scheduleId
     */
    public String getScheduleId() {
        return scheduleId;
    }

    /**
     * @param scheduleId
     *            セットする scheduleId
     */
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * @return userIds
     */
    public List<String> getUserIds() {
        return userIds;
    }

    /**
     * @param userIds
     *            セットする userIds
     */
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            セットする title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            セットする category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return dates
     */
    public List<String> getDates() {
        return dates;
    }

    /**
     * @param dates
     *            セットする dates
     */
    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    /**
     * @return startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            セットする startTime
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return finishTime
     */
    public String getFinishTime() {
        return finishTime;
    }

    /**
     * @param finishTime
     *            セットする finishTime
     */
    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * @return detail
     */
    public String getDetail() {
        return detail;
    }

    /**
     * @param detail
     *            セットする detail
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * @return place
     */
    public String getPlace() {
        return place;
    }

    /**
     * @param place
     *            セットする place
     */
    public void setPlace(String place) {
        this.place = place;
    }
}
