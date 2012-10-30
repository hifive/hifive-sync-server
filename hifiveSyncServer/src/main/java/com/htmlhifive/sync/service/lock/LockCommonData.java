package com.htmlhifive.sync.service.lock;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.htmlhifive.sync.service.SyncCommonData;

/**
 * ロック取得に関する共通情報を保持するデータクラス.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@Deprecated
public class LockCommonData implements SyncCommonData, Serializable {

	private static final long serialVersionUID = -5832494043088305292L;

	/**
	 * クライアントのストレージID.<br>
	 * このフィールドはクライアントへのレスポンスに含みません.<br>
	 * レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonSerialize}を追加しています.
	 */
	private String storageId;

	/**
	 * ロック取得リクエストが実行される時刻.<br>
	 * このフィールドはクライアントへのレスポンスに含みません.<br>
	 * レスポンスから除外するため、このフィールドのgetterメソッドに{@link JsonIgnore}を追加しています.
	 */
	private long syncTime;

	/**
	 * リクエストの対象となっているロックトークン.<br>
	 * 設定されているときのみレスポンスに含めるため、このフィールドのgetterメソッドに{@link JsonSerialize}を追加しています.
	 */
	private String lockToken;

	/**
	 * レスポンスから除外するため、{@link JsonSerialize}を追加しています.
	 *
	 * @return storageId
	 */
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	@Override
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
	 * レスポンスから除外するため、{@link JsonIgnore}を追加しています.
	 *
	 * @return syncTime
	 */
	@JsonIgnore
	@Override
	public long getSyncTime() {
		return syncTime;
	}

	/**
	 * @param syncTime セットする syncTime
	 */
	public void setSyncTime(long syncTime) {
		this.syncTime = syncTime;
	}

	/**
	 * 設定されているときのみレスポンスに含めるため、{@link JsonSerialize}を追加しています.
	 *
	 * @return lockToken
	 */
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	@Override
	public String getLockToken() {
		return lockToken;
	}

	/**
	 * @param lockToken セットする lockToken
	 */
	public void setLockToken(String lockToken) {
		this.lockToken = lockToken;
	}
}
