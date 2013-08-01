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
import java.util.Set;

/**
 * リクエスト、レスポンスの情報を保持するメッセージインターフェース.<br>
 * 1つのキーに対して複数の情報源({@link MessageSource MessageSource})からの上書き設定でき、履歴情報を{@link MessageKeyInfo MessageKeyInfo}
 * として取得できるという特徴を持ちます.
 *
 * @author kishigam
 */
public interface Message {

	/**
	 * キーを指定してメッセージが保持する値を取得します.
	 *
	 * @param key キー
	 * @return　値
	 */
	Object get(String key);

	/**
	 * 指定されたキー、値、情報源の情報をメッセージに挿入します.<br>
	 * getメソッドで取得できる値は最も後にputされた値になり、それまでにputされた情報は{@link MessageKeyInfo MessageKeyInfo}として取得できます.
	 *
	 * @param key キー
	 * @param value 値
	 * @param source 情報源を表す列挙値
	 */
	void put(String key, Object value, MessageSource source);

	/**
	 * このメッセージが保持している全てのキーを取得します.
	 *
	 * @return キーのSet
	 */
	Set<String> keys();

	/**
	 * 指定されたキーの{@link MessageKeyInfo MessageKeyInfo}のリストを取得します.
	 *
	 * @param key　キー
	 * @return MessageKeyInfoオブジェクト
	 */
	List<MessageKeyInfo> getKeyInfo(String key);

	/**
	 * 指定されたキーで保持する値が配列やListの時、指定indexの値のみで構成される {@link MessageKeyInfo MessageKeyInfo}のリストを取得します.<br>
	 * 配列やListでない場合、{@link IndexOutOfBoundsException IndexOutOfBoundsException}がスローされます.
	 *
	 * @param key　キー
	 * @param index インデックス
	 * @return MessageKeyInfoオブジェクト
	 * @throws IndexOutOfBoundsException 値が配列やリストでない場合
	 */
	List<MessageKeyInfo> getKeyInfo(String key, int index);
}
