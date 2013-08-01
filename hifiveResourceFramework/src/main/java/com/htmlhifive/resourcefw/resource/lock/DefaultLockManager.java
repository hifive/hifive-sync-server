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
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.exception.LockedException;
import com.htmlhifive.resourcefw.message.RequestMessage;

/**
 * リクエストされたリソースアイテムのLock管理、読み書き可能判定を行うデフォルトのマネージャ実装.
 *
 * @author kishigam
 */
public class DefaultLockManager implements LockManager {

	/**
	 * IDで示されるリソースアイテムが読み取りロックされていない、または指定されたロックトークンで読み取りロックされているときtrueを返します.
	 */
	@Override
	public boolean canRead(String lockToken, String id, RequestMessage requestMessage) {

		// 未実装
		return true;
	}

	/**
	 * IDで示されるリソースアイテムが書き込みロックまたは読み取りロックされていない、あるいは指定されたロックトークンで書き込みロックされているときtrueを返します.
	 */
	@Override
	public boolean canWrite(String lockToken, String id, RequestMessage requestMessage) {

		// 未実装
		return true;
	}

	/**
	 * 指定されたロックタイプで、指定されたIDを持つリソースアイテムをロックします.<br/>
	 * ロックしたアイテムにアクセスするためのロックトークンを返します.
	 */
	@Override
	public String lock(String id, LockType lockType, RequestMessage requestMessage) throws LockedException {

		// 未実装
		throw new GenericResourceException("This resource is not supported lock");
	}

	/**
	 * 指定されたロックタイプで、指定されたIDを持つリソースアイテムのロックを開放します.<br/>
	 * ロックしたアイテムにアクセスしたロックトークンを返します.
	 */
	@Override
	public String unlock(String id, LockType lockType, RequestMessage requestMessage) throws BadRequestException {

		// 未実装
		throw new GenericResourceException("This resource is not supported unlock");
	}

	/**
	 * リクエストメッセージのデータを使用してロックトークンを生成します.
	 */
	@Override
	public String createToken(RequestMessage requestMessage) {

		// 未実装
		throw new GenericResourceException("This resource is not supported lock");
	}
}
