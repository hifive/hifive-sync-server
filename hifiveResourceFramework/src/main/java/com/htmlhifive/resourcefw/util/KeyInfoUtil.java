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
package com.htmlhifive.resourcefw.util;

import com.htmlhifive.resourcefw.message.AbstractMessage;
import com.htmlhifive.resourcefw.message.AbstractMessageContainer;
import com.htmlhifive.resourcefw.message.Message;

/**
 * MesasgeKeyInfoの情報を扱うユーティリティ.
 *
 * @author kishigam
 */
public class KeyInfoUtil {

	public static <T extends AbstractMessage<?>> void printKeyInfo(AbstractMessageContainer<T> container) {

		System.out.println("size = " + container.getMessages().size());
		int i = 0;
		for (T message : container.getMessages()) {
			System.out.println("message" + i++);
			printKeyInfo(message);
		}
	}

	public static void printKeyInfo(Message message) {
		for (String key : message.keys()) {
			System.out.println("key : " + key);
			System.out.println(message.getKeyInfo(key));
		}
	}
}
