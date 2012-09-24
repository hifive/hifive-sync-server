package com.htmlhifive.sync.service;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.htmlhifive.sync.resource.ResourceQuery;

/**
 * リソースごとのクエリリストを保持するコンテナ.
 *
 * @author kishigam
 */
public class ResourceQueriesContainer {

	/**
	 * リソース名.
	 */
	private String resourceName;

	/**
	 * クエリオブジェクトのリスト.
	 */
	private List<ResourceQuery> queryList;

	/**
	 * リソース名を指定してコンテナを生成します.
	 *
	 * @param resourceName リソース名
	 */
	public ResourceQueriesContainer(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof ResourceQueriesContainer))
			return false;

		ResourceQueriesContainer request = (ResourceQueriesContainer) obj;

		return new EqualsBuilder().append(this.resourceName, request.resourceName)
				.append(this.queryList, request.queryList).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.resourceName).append(this.queryList).hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * @return resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceName セットする resourceName
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return queryList
	 */
	public List<ResourceQuery> getQueryList() {
		return queryList;
	}

	/**
	 * @param queryList セットする queryList
	 */
	public void setQueryList(List<ResourceQuery> queryList) {
		this.queryList = queryList;
	}

	/**
	 * このオブジェクトが保持するクエリリストにクエリを追加します.<br>
	 * ただし、すでに(追加しようとしているクエリのequalsがtrueとなるようなクエリが)リスト内に存在する場合は追加されません.
	 *
	 * @param item リソースアイテム
	 */
	public void mergeQuery(ResourceQuery query) {
		if (!this.queryList.contains(query)) {
			this.queryList.add(query);
		}
	}
}
