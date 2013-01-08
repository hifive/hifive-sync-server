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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.sample.person.Person;

/**
 * 予定の情報を保持するエンティティクラス.<br>
 *
 * @author kishigam
 */
@Entity
@Table(name = "SCHEDULE")
public class Schedule implements Serializable {

    private static final long serialVersionUID = 7778106673541238346L;

    /**
     * このエンティティのID.<br>
     */
    @Id
    private String scheduleId;

    /**
     * 予定の対象となっている人.
     */
    @ManyToMany
    @JoinTable(
            name = "SCHEDULE_PERSON", joinColumns = { @JoinColumn(name = "SCHEDULEID") },
            inverseJoinColumns = { @JoinColumn(name = "PERSONID") })
    private List<Person> userBeans;

    /**
     * 予定のタイトル.
     */
    private String title;

    /**
     * この予定の種類.
     */
    private String category;

    /**
     * 予定日付のリスト.
     */
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<ScheduleDate> dateBeans;

    /**
     * 予定の開始時刻(24時間・コロン区切り).<br>
     * 予定の種類によっては、開始－終了でない単一時刻も保持します.
     */
    private String startTime;

    /**
     * 予定の終了時刻(24時間・コロン区切り).<br>
     * 開始－終了でない単一時刻の予定の場合、設定する必要はありません.
     */
    private String finishTime;

    /**
     * 予定の詳細を表す文字列.
     */
    private String detail;

    /**
     * 予定されている場所を表す文字列.
     */
    private String place;

    /**
     * 予定を作成した人.
     */
    @ManyToOne
    private Person createUser;

    /**
     * 予定インスタンスを生成します.
     */
    public Schedule() {
        // リストフィールドに空のリストインスタンスを設定
        userBeans = new ArrayList<>();
        dateBeans = new ArrayList<>();
    }

    /**
     * 予定IDを指定して予定インスタンスを生成します.
     *
     * @param scheduleId
     */
    public Schedule(String scheduleId) {
        this();
        this.scheduleId = scheduleId;
    }

    /**
     * @see Object#equals()
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof Schedule))
            return false;

        Schedule scheduleBean = (Schedule)obj;

        // IDやcreateUserを含むすべての同一性を含む
        // リストフィールドは順不同ではない
        return new EqualsBuilder().append(this.scheduleId, scheduleBean.scheduleId).append(
                this.userBeans, scheduleBean.userBeans).append(this.title, scheduleBean.title).append(
                this.category, scheduleBean.category).append(this.dateBeans, scheduleBean.dateBeans).append(
                this.startTime, scheduleBean.startTime).append(
                this.finishTime, scheduleBean.finishTime).append(this.detail, scheduleBean.detail).append(
                this.place, scheduleBean.place).append(this.createUser, scheduleBean.createUser).isEquals();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 37).append(this.scheduleId).append(this.userBeans).append(
                this.title).append(this.category).append(this.dateBeans).append(this.startTime).append(
                this.finishTime).append(this.detail).append(this.place).append(this.createUser).hashCode();
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
     * @return userBeans
     */
    public List<Person> getUserBeans() {
        return userBeans;
    }

    /**
     * @param userBeans
     *            セットする userBeans
     */
    public void setUserBeans(List<Person> userBeans) {
        this.userBeans = userBeans;
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
     * @return dateBeans
     */
    public List<ScheduleDate> getDateBeans() {
        return dateBeans;
    }

    /**
     * @param dateBeans
     *            セットする dateBeans
     */
    public void setDateBeans(List<ScheduleDate> dateBeans) {
        this.dateBeans = dateBeans;
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

    /**
     * @return createUser
     */
    public Person getCreateUser() {
        return createUser;
    }

    /**
     * @param createUser
     *            セットする createUser
     */
    public void setCreateUser(Person createUser) {
        this.createUser = createUser;
    }
}
