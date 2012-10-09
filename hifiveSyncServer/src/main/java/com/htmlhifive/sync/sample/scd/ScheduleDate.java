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
public class ScheduleDate {

	/**
	 * このエンティティのID.
	 */
	@Id
	@GeneratedValue
	private int id;

	/**
	 * 予定日付("yyyyMMdd"の8桁整数).
	 */
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
	 * IDを除くフィールドの値が同一の時、同一とします.
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof ScheduleDate))
			return false;

		ScheduleDate dateBean = (ScheduleDate) obj;

		return new EqualsBuilder().append(this.date, dateBean.date).append(this.schedule, dateBean.schedule).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.date).append(this.schedule).hashCode();
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
	 * @param id セットする id
	 */
	public void setId(int id) {
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
