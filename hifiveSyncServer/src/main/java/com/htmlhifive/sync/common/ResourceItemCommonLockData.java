package com.htmlhifive.sync.common;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.htmlhifive.sync.resource.ResourceLockStatusType;

/**
 * リソースアイテム共通データのロック状態を保持するエンティティ.
 *
 * @author kishigam
 */
@Entity
@Table(name = "RESOURCE_ITEM_COMMON_LOCK")
public class ResourceItemCommonLockData {

	/**
	 * リソースアイテム共通データのIDオブジェクト.
	 */
	@Id
	private ResourceItemCommonDataId id;

	/**
	 * ロックトークン.
	 */
	private String token;

	/**
	 * このロック状態の終了時刻.
	 */
	private long limitOfTime;

	/**
	 * ロック状態.
	 */
	private ResourceLockStatusType status;

	/**
	 * @return id
	 */
	public ResourceItemCommonDataId getId() {
		return id;
	}

	/**
	 * @param id セットする id
	 */
	public void setId(ResourceItemCommonDataId id) {
		this.id = id;
	}

	/**
	 * @return token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token セットする token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return limitOfTime
	 */
	public long getLimitOfTime() {
		return limitOfTime;
	}

	/**
	 * @param limitOfTime セットする limitOfTime
	 */
	public void setLimitOfTime(long limitOfTime) {
		this.limitOfTime = limitOfTime;
	}

	/**
	 * @return status
	 */
	public ResourceLockStatusType getStatus() {
		return status;
	}

	/**
	 * @param status セットする status
	 */
	public void setStatus(ResourceLockStatusType status) {
		this.status = status;
	}
}
