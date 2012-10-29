package com.htmlhifive.sync.resource.common;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.resource.lock.ResourceLockStatusType;
import com.htmlhifive.sync.service.SyncCommonData;

/**
 * リソースアイテム共通データのロック状態を保持するエンティティ.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Deprecated
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
	 * 指定されたいずれかのロック状態でロックされている場合、{@link LockException}をスローします.<br>
	 * {@link ResourceLockStatusType#UNLOCK}の場合は例外はスローされません.
	 *
	 * @param statusForCheck チェックするロック状態
	 * @throws LockException ロックされている場合
	 */
	public void checkStatus(SyncCommonData syncCommon, ResourceLockStatusType... statusForCheck) {

		for (ResourceLockStatusType forCheck : statusForCheck) {
			if (status == ResourceLockStatusType.UNLOCK || forCheck != status)
				continue;

			if (!token.equals(syncCommon.getLockToken())) {
				throw new LockException("Resource item is locked by another. : " + id);
			}
		}
	}

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
