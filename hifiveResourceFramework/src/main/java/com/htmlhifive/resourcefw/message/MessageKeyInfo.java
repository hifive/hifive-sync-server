/*
 * Copyright (C) 2012-2013 NS Solutions Corporation
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
package com.htmlhifive.resourcefw.message;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * メッセージに保持する情報(key-value)と、その情報源を取得できるキー情報クラス.
 *
 * @author kishigam
 */
public class MessageKeyInfo {

	/**
	 * 情報源
	 */
	private final MessageSource source;

	/**
	 * 情報源で設定された値
	 */
	private final Object value;

	/**
	 * このキー情報がメッセージにおいて有効になっている場合true.<br>
	 * 同じキーで再度putされるとfalseになります.
	 */
	private boolean accepted;

	/**
	 * キー情報を生成します.
	 *
	 * @param source 情報源
	 * @param value 情報源における値
	 * @param accepted 有効のときtrue
	 */
	public MessageKeyInfo(MessageSource source, Object value, boolean accepted) {
		this.source = source;
		this.value = value;
		this.accepted = accepted;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof MessageKeyInfo))
			return false;

		MessageKeyInfo keyInfo = (MessageKeyInfo) obj;

		return new EqualsBuilder().append(this.source, keyInfo.source).append(this.value, keyInfo.value).isEquals();
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.source).append(this.value).toHashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append(this.accepted ? "[accepted]" : "[not accepted] ").append(this.source)
				.append(" : ").append(this.value).toString();
	}

	/**
	 * @return the source
	 */
	public MessageSource getSource() {
		return source;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the value of index
	 * @throws ClassCastException
	 * @throws IndexOutOfBoundsException
	 */
	public Object getValue(int index) {

		if (value.getClass().isArray()) {
			return ((Object[]) value)[index];
		}

		if (!(value instanceof List)) {
			throw new IndexOutOfBoundsException("The value is neither an array nor list.");
		}

		return ((List<?>) value).get(index);
	}

	/**
	 * @return the accepted
	 */
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * @param accepted the accepted to set
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
}
