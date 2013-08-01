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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.resource.Resource;

/**
 * リクエスト、レスポンスの情報を保持するメッセージ基底クラス.<br>
 * 1件のHTTPリクエスト、レスポンス内に多重化されたリクエスト、レスポンスが含まれる場合はこのオブジェクトが複数生成され、{@link AbstractMessageContainer
 * AbstractMessageContainer}で集約されます.<br>
 * このメッセージは1件のHTTPリクエストあるいはレスポンス内、つまりContainer内で共通のコンテキスト情報への参照を持ちます.<br>
 * 各メッセージは自身が保持していないキーの情報についてはコンテキスト情報を参照します.
 *
 * @author kishigam
 * @param <E> MessageContextクラス
 */
public abstract class AbstractMessage<E extends AbstractMessageContext> implements Message {

	/**
	 * メッセージメタデータオブジェクト.
	 */
	private MessageMetadata messageMetadata;

	/**
	 * コンテキスト情報オブジェクト.<br>
	 * 複数のリクエスト、あるいは複数のレスポンスで共通に参照される情報を保持します.
	 */
	private E messageContext;

	/**
	 * データを保持する内部Mapインスタンス.
	 */
	private Map<String, Object> map = new HashMap<>();

	/**
	 * 各データの情報源と値のペアを持つ{@link MessageKeyInfo MessageKeyInfo}オブジェクトリストを持つMap.<br/>
	 * Messageにおいて各データは様々な情報源(MessageSource)から設定され、規定の優先度に基づいて上書きされますが、 このMapでは全てのMessageSourceの値を持っています.<br>
	 * MessageKeyInfoリスト(Deque)の先頭要素が持つ値が、その時点での値になります.
	 *
	 * @see MessageSource
	 * @see MessageKeyInfo
	 */
	private Map<String, Deque<MessageKeyInfo>> keyInfoMap = new HashMap<>();

	/**
	 * メッセージメタデータオブジェクトを指定してMessageオブジェクトを生成します.
	 *
	 * @param messageMetadata
	 */
	public AbstractMessage(MessageMetadata messageMetadata) {
		this.messageMetadata = messageMetadata;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;
		if (!(obj instanceof AbstractMessage))
			return false;

		AbstractMessage<?> abstractMessage = (AbstractMessage<?>) obj;

		return new EqualsBuilder().append(this.map, abstractMessage.map)
				.append(this.keyInfoMap, abstractMessage.keyInfoMap).isEquals();
	}

	@Override
	public int hashCode() {

		return new HashCodeBuilder(17, 37).append(this.map).append(this.keyInfoMap).toHashCode();
	}

	/**
	 * このMessageが持つ「パス」メタデータを取得するコンビニエンスメソッド.
	 */
	public String getPath() {
		return (String) get(messageMetadata.REQUEST_PATH);
	}

	/**
	 * このメッセージが持つ、指定されたキーの値を返します.<br>
	 * Message自身がキーに対応する値を持たない場合、コンテキスト情報を参照します.<br>
	 * いずれにも値を持たない場合はnullを返します.
	 *
	 * @param key キー
	 * @return キーに対応する値
	 */
	@Override
	public Object get(String key) {

		Object result = map.get(key);

		if (result != null)
			return result;

		return messageContext.get(key);
	}

	/**
	 * このメッセージに指定されたキーで、値を保持します.<br>
	 * 値の情報源は{@link Resource}が設定されます.<br>
	 * {@link AbstractMessage#put(String, Object, MessageSource)}をリソースから使用する場合に使用することができます.
	 *
	 * @param key キー
	 * @param value 値
	 */
	public void put(String key, Object value) {
		put(key, value, MessageSource.RESOURCE);
	}

	/**
	 * このメッセージに指定されたキーで、値を保持します.<br>
	 * 値の情報源はsourceに設定します.<br>
	 * {@link MessageKeyInfo MessageKeyInfo}の情報が生成、保持されます.
	 *
	 * @param key キー
	 * @param value 値
	 * @param source 値の情報源
	 * @see MessageKeyInfo
	 */
	@Override
	public void put(String key, Object value, MessageSource source) {

		// MessagekeyInfoの生成
		Deque<MessageKeyInfo> deque = keyInfoMap.get(key);
		if (deque == null) {
			deque = new LinkedList<>();
			keyInfoMap.put(key, deque);
		} else {
			// 以前のkeyInfoをaccepted=falseにすることで、先頭の要素がacceptedになるようにする
			deque.peekFirst().setAccepted(false);
		}
		deque.addFirst(new MessageKeyInfo(source, value, true));

		// 本体mapにもput(上書き)
		map.put(key, value);
	}

	/**
	 * このMessageが持つ、指定されたキーの値をMapから削除します.また、その値を返します.<br>
	 * このメソッドでは、コンテキストオブジェクトを参照、および削除しません。<br>
	 * キーに該当する値がない場合はnullを返します.
	 *
	 * @param key キー
	 */
	public Object remove(String key) {

		// Contextにさかのぼって削除することはない
		return map.remove(key);
	}

	/**
	 * このMessageから取得できるキーのセットを返します.<br>
	 * コンテキストオブジェクトの内容も含みます.
	 */
	@Override
	public Set<String> keys() {

		Set<String> set = new HashSet<>();
		set.addAll(messageContext.keys());
		set.addAll(map.keySet());

		return set;
	}

	/**
	 * 指定されたキーに該当する{@link MessageKeyInfo MessageKeyInfo}のリストを返します.<br>
	 * リストは、このメッセージに設定された値と、コンテキストオブジェクトが保持する値の両方のMessageKeyInfoを含みます.<br/>
	 *
	 * @param key キー
	 * @return MessageKeyInfoのリスト
	 */
	@Override
	public List<MessageKeyInfo> getKeyInfo(String key) {

		List<MessageKeyInfo> result = new ArrayList<>();

		// MessageContextのKeyInfoは、Message固有のKeyInfoがあればnotAcceptedになる
		boolean containsKey = keyInfoMap.containsKey(key);
		for (MessageKeyInfo info : getMessageContext().getKeyInfo(key)) {
			if (containsKey) {
				info.setAccepted(false);
			}
			result.add(info);
		}

		// Message固有のKeyInfo
		Deque<MessageKeyInfo> messageKeyInfo = keyInfoMap.get(key);
		if (!(messageKeyInfo == null || messageKeyInfo.isEmpty()))
			result.addAll(messageKeyInfo);

		return result;
	}

	/**
	 * 指定されたキーに該当する{@link MessageKeyInfo MessageKeyInfo}のリストを返します.<br>
	 * 値が配列またはlistの場合には指定されたindex番目の値のみ含むMessageKeyInfoを返します.
	 * リストは、このメッセージに設定された値と、コンテキストオブジェクトが保持する値の両方のMessageKeyInfoを含みます.<br/>
	 *
	 * @param key キー
	 * @param index 要素のindex
	 * @return MessageKeyInfoのリスト
	 * @throws IndexOutOfBoundsException 値が配列やリストでない場合
	 */
	@Override
	public List<MessageKeyInfo> getKeyInfo(String key, int index) {

		List<MessageKeyInfo> result = new ArrayList<>();
		for (MessageKeyInfo info : getKeyInfo(key)) {
			result.add(new MessageKeyInfo(info.getSource(), info.getValue(index), info.isAccepted()));
		}

		return result;
	}

	/**
	 * メッセージメタデータオブジェクトを返します.<br>
	 * メッセージごとにオブジェクトの内容が変わることはありません.
	 */
	public MessageMetadata getMessageMetadata() {
		return this.messageMetadata;
	}

	/**
	 * @return the messageContext
	 */
	protected E getMessageContext() {
		return messageContext;
	}

	/**
	 * @param messageContext the messageContext to set
	 */
	protected void setMessageContext(E messageContext) {
		this.messageContext = messageContext;
	}
}
