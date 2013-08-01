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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 1件のHTTPリクエストまたはレスポンスに関する共有情報へアクセスするためのコンテキスト情報基底クラス.<br>
 * この共有情報は{@link AbstractMessage AbstractMessage}データを取得する時にも参照され、そのメッセージ自身が保持しないデータも共有情報に含まれていれば取得できるようになります.<br>
 * つまり、デフォルト値や複数メッセージに渡って共通のデータを保持するために使用することができます.<br>
 * メッセージと同様にkey-valueによるデータの保持、{@link MessageSource MessageSource} ごとのデータ設定履歴を保持することができ、 {@link MessageKeyInfo
 * MessageKeyInfo}による設定履歴の取出しが可能です.
 *
 * @author kishigam
 */
abstract class AbstractMessageContext implements Message {

	/**
	 * 指定されたキーの情報を返します.
	 *
	 * @param key キー
	 * @return 値
	 */
	@Override
	public Object get(String key) {

		Deque<MessageKeyInfo> deque = getMessageContextData().get(key);

		// リストの先頭のKeyInfoが採用されたKeyInfoになるのでそのvalueを返す
		return deque != null ? deque.peekFirst().getValue() : null;
	}

	/**
	 * 指定された情報をコンテキスト情報に挿入します.
	 */
	@Override
	public void put(String key, Object value, MessageSource source) {

		Map<String, Deque<MessageKeyInfo>> contextData = getMessageContextData();

		Deque<MessageKeyInfo> deque = contextData.get(key);
		if (deque == null) {
			deque = new LinkedList<>();
			contextData.put(key, deque);
		} else {
			// 以前のkeyInfoをaccepted=falseにすることで、先頭の要素がacceptedになるようにする
			deque.peekFirst().setAccepted(false);
		}

		deque.addFirst(new MessageKeyInfo(source, value, true));
	}

	/**
	 * コンテキスト情報の全てのキーを返します.
	 *
	 * @return キーのSet
	 */
	@Override
	public Set<String> keys() {

		return getMessageContextData().keySet();
	}

	/**
	 * 指定されたキーに該当する{@link MessageKeyInfo MessageKeyInfo}のリストを返します.<br>
	 *
	 * @param key キー
	 * @return MessageKeyInfoのリスト
	 */
	@Override
	public List<MessageKeyInfo> getKeyInfo(String key) {

		ArrayList<MessageKeyInfo> result = new ArrayList<>();

		Deque<MessageKeyInfo> keyInfo = getMessageContextData().get(key);
		if (!(keyInfo == null || keyInfo.isEmpty())) {
			result.addAll(keyInfo);
		}
		return result;
	}

	/**
	 * 指定されたキーに該当する{@link MessageKeyInfo MessageKeyInfo}のリストを返します.<br>
	 * 値が配列またはlistの場合には指定されたindex番目の値のみ含むMessageKeyInfoを返します.
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
	 * コンテキスト情報の実体となるキーとMessageKeyInfoリストのMapを返します.<br>
	 * サブクラスではリクエスト、あるいはレスポンスで使用されるデータを取得し、返すように実装する必要があります.<br>
	 *
	 * @return コンテキスト情報Map
	 */
	protected abstract Map<String, Deque<MessageKeyInfo>> getMessageContextData();
}
