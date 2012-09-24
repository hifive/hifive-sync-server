package com.htmlhifive.sync.commondata;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * {@link CommonData 共通データクラス}のIDクラス.
 *
 * @author kishigam
 */
@Embeddable
public class CommonDataId implements Serializable {

	/**
	 * シリアルバージョンUID.
	 */
	private static final long serialVersionUID = -3582140249142802398L;

	/**
	 * リソース名.<br>
	 */
	private String resourceName;

	/**
	 * リソースアイテムのID.
	 */
	private String resourceItemId;

	/**
	 * アプリケーションからは使用できないプライベートデフォルトコンストラクタ.<br>
	 * 永続化マネージャが使用します.
	 */
	@SuppressWarnings("unused")
	private CommonDataId() {
	}

	public CommonDataId(String resourceName, String resourceItemId) {
		this.resourceName = resourceName;
		this.resourceItemId = resourceItemId;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (!(obj instanceof CommonDataId))
			return false;

		return EqualsBuilder.reflectionEquals(this, ((CommonDataId) obj));
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return HashCodeBuilder.reflectionHashCode(this);
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
	 * @return resourceItemId
	 */
	public String getResourceItemId() {
		return resourceItemId;
	}
}