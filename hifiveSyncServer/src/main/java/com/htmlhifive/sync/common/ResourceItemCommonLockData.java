package com.htmlhifive.sync.common;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.htmlhifive.sync.resource.ResourceLockModeType;

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
	 * ロックしているクライアントのストレージID.
	 */
	private String storageId;

	/**
	 * ロックした時刻.
	 */
	private long lockedTime;

	/**
	 * ロックモード.
	 */
	private ResourceLockModeType lockMode;

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
	 * @return storageId
	 */
	public String getStorageId() {
		return storageId;
	}

	/**
	 * @param storageId セットする storageId
	 */
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	/**
	 * @return lockedTime
	 */
	public long getLockedTime() {
		return lockedTime;
	}

	/**
	 * @param lockedTime セットする lockedTime
	 */
	public void setLockedTime(long lockedTime) {
		this.lockedTime = lockedTime;
	}

	/**
	 * @return lockMode
	 */
	public ResourceLockModeType getLockMode() {
		return lockMode;
	}

	/**
	 * @param lockMode セットする lockMode
	 */
	public void setLockMode(ResourceLockModeType lockMode) {
		this.lockMode = lockMode;
	}
}
