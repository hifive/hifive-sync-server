/*
 * Copyright (C) 2012 NS Solutions Corporation
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
package com.htmlhifive.sync.resource.update;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.ItemUpdatedException;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;

/**
 * 更新を行わず、クライアントに判断を求める更新戦略実装クラス.<br>
 *
 * @author kishigam
 */
@Service
public class ClientResolvingStrategy implements UpdateStrategy {

	/**
	 * 競合を解決せず、ItemUpdateExceptionをスローします.
	 */
	@Override
	public <T> T resolveConflict(ResourceItemCommonData itemCommon, T item, ResourceItemCommonData serverCommon,
			T serverItem) throws ItemUpdatedException {

		throw new ItemUpdatedException("return client to resolve.");
	}

}
