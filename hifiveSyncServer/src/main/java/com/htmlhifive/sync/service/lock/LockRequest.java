package com.htmlhifive.sync.service.lock;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceQueryConditions;

/**
 * ロック取得リクエスト内容全体を表現するデータクラス.
 *
 * @author kishigam
 */
public class LockRequest {

	/**
	 * ロック取得共通データ.<br>
	 * このリクエストにおいてクライアントから渡される情報を保持しています.
	 */
	private LockCommonData lockCommonData;

	/**
	 * ロック対象のクエリリストのMap.<br>
	 * リソース別にクエリリストを保持します.
	 */
	private Map<String, List<ResourceQueryConditions>> queries;

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof LockRequest))
			return false;

		LockRequest request = (LockRequest) obj;

		return new EqualsBuilder().append(this.lockCommonData, request.lockCommonData)
				.append(this.queries, request.queries).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.lockCommonData).append(this.queries).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return new ToStringBuilder(this).append(this.lockCommonData).append(this.queries).toString();
	}

	/**
	 * @return lockCommonData
	 */
	public LockCommonData getLockCommonData() {
		return lockCommonData;
	}

	/**
	 * @param lockCommonData セットする lockCommonData
	 */
	public void setLockCommonData(LockCommonData lockCommonData) {
		this.lockCommonData = lockCommonData;
	}

	/**
	 * @return queries
	 */
	public Map<String, List<ResourceQueryConditions>> getQueries() {
		return queries;
	}

	/**
	 * @param queries セットする queries
	 */
	public void setQueries(Map<String, List<ResourceQueryConditions>> queries) {
		this.queries = queries;
	}
}
