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

import javax.persistence.Column;
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
public class ScheduleDate implements Serializable {

	private static final long serialVersionUID = 8658446519798521096L;

	/**
	 * このエンティティのID.
	 */
	@Id
	@GeneratedValue
	private long id;

	/**
	 * 予定日付("yyyyMMdd"の8桁整数).
	 */
	@Column(name = "dateNum")
	// dateはOracle等では使用できないため、カラム名を別途指定
	private int date;

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
	private ScheduleDate() {
	}

	/**
	 * 予定エンティティおよびその予定日付を指定して新規予定日付エンティティを生成します.
	 *
	 * @param schedule 予定エンティティ
	 * @param date 日付(8桁整数)
	 */
	public ScheduleDate(Schedule schedule, int date) {
		this.schedule = schedule;
		this.date = date;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof ScheduleDate))
			return false;

		ScheduleDate dateBean = (ScheduleDate) obj;

		// scheduleは同一性判定に含めない
		return new EqualsBuilder().append(this.date, dateBean.date).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.date).hashCode();
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
	public long getId() {
		return id;
	}

	/**
	 * @param id セットする id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return date
	 */
	public int getDate() {
		return date;
	}

	/**
	 * @param date セットする date
	 */
	public void setDate(int date) {
		this.date = date;
	}

	/**
	 * @return schedule
	 */
	public Schedule getSchedule() {
		return schedule;
	}

	/**
	 * @param schedule セットする schedule
	 */
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

}
