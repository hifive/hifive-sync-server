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
package com.htmlhifive.resourcefw.resource.lock;

import com.htmlhifive.resourcefw.exception.BadRequestException;
import com.htmlhifive.resourcefw.exception.LockedException;
import com.htmlhifive.resourcefw.message.RequestMessage;

/**
 * リソースアイテムのロックを管理するマネージャインターフェース.<br>
 *
 * @author kishigam
 */
public interface LockManager {

	/**
	 * IDで示されるリソースアイテムが読み取りロックされていない、または指定されたロックトークンで読み取りロックされているときtrueを返します.
	 *
	 * @param lockToken ロックトークン
	 * @param id リソースアイテムのID
	 * @param requestMessage リクエストメッセージ
	 * @return 読み取り可能な場合true
	 */
	boolean canRead(String lockToken, String id, RequestMessage requestMessage);

	/**
	 * IDで示されるリソースアイテムが書き込みロックまたは読み取りロックされていない、あるいは指定されたロックトークンで書き込みロックされているときtrueを返します.
	 *
	 * @param lockToken ロックトークン
	 * @param id リソースアイテムのID
	 * @param requestMessage リクエストメッセージ
	 * @return 書き込み可能な場合true
	 */
	boolean canWrite(String lockToken, String id, RequestMessage requestMessage);

	/**
	 * 指定されたロックタイプで、指定されたIDを持つリソースアイテムをロックします.<br/>
	 * ロックしたアイテムにアクセスするためのロックトークンを返します.
	 *
	 * @param id リソースアイテムのID
	 * @param lockType ロックタイプ
	 * @param requestMessage リクエストメッセージ
	 * @return ロックトークン
	 */
	String lock(String id, LockType lockType, RequestMessage requestMessage) throws LockedException;

	/**
	 * 指定されたロックタイプで、指定されたIDを持つリソースアイテムのロックを開放します.<br/>
	 * ロックしたアイテムにアクセスしたロックトークンを返します.
	 *
	 * @param id リソースアイテムのID
	 * @param lockType ロックタイプ
	 * @param requestMessage リクエストメッセージ
	 * @return ロックトークン
	 */
	String unlock(String id, LockType lockType, RequestMessage requestMessage) throws BadRequestException;

	/**
	 * リクエストメッセージのデータを使用してロックトークンを生成します.
	 *
	 * @param requestMessage リクエストメッセージ
	 * @return ロックトークン
	 */
	String createToken(RequestMessage requestMessage);
}
