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
import java.util.Iterator;
import java.util.List;

/**
 * {@link AbstractMessage AbstractMessage}を受け渡すためのコンテナ基底クラス.<br>
 * 複数のメッセージを扱う多重化リクエストに対応しています.<br>
 *
 * @author kishigam
 * @param <T> Messageクラス(Request, またはResponse)
 */
public abstract class AbstractMessageContainer<T extends AbstractMessage<?>> {

	/**
	 * 多重化リクエストかどうか.
	 */
	private boolean multiplexed;

	/**
	 * このコンテナが保持するメッセージのリスト.<br>
	 * 多重化リクエストの場合は1件以上、単一リクエストの場合は1件のメッセージを含みます.
	 */
	private List<T> messages = new ArrayList<>();

	/**
	 * 多重化リクエストかどうかを指定して、コンテナを生成します.
	 *
	 * @param multiplexed　多重化リクエストのときtrue
	 */
	public AbstractMessageContainer(boolean multiplexed) {
		this.multiplexed = multiplexed;
	}

	/**
	 * メッセージを追加します.
	 *
	 * @param message メッセージ
	 */
	public void addMessage(T message) {
		getMessages().add(message);
	}

	/**
	 * メッセージのイテレータを返します.
	 *
	 * @return メッセージのイテレータ
	 */
	public Iterator<T> iterator() {
		return messages.iterator();
	}

	/**
	 * このコンテナに含まれる各メッセージから参照できるコンテキスト情報オブジェクトを返します.<br>
	 * サブクラスではリクエスト、あるいはレスポンスで使用されるオブジェクトを返すように実装する必要があります.
	 *
	 * @return コンテキスト情報オブジェクト
	 */
	protected abstract AbstractMessageContext getContext();

	/**
	 * コンテキスト情報から指定されたキー名の値を返します.<br>
	 * コンテキスト情報に値を持たない場合はnullを返します.<br>
	 *
	 * @param key　キー名
	 * @return キーに対応する値
	 */
	public Object getContextData(String key) {
		return getContext().get(key);
	}

	/**
	 * このコンテナに含まれる各メッセージから参照できるコンテキスト情報オブジェクトに対して情報を挿入します.<br>
	 * ここで挿入された情報は、各メッセージ自身が保持していない場合に参照されます.
	 *
	 * @param key キー
	 * @param value 値
	 * @param source 値の情報源
	 */
	public void putContextData(String key, Object value, MessageSource source) {
		getContext().put(key, value, source);
	}

	/**
	 * @return the multiplexed
	 */
	public boolean isMultiplexed() {
		return multiplexed;
	}

	public List<T> getMessages() {
		return messages;
	}

	public void setMessages(List<T> messages) {
		this.messages = messages;

	}
}
