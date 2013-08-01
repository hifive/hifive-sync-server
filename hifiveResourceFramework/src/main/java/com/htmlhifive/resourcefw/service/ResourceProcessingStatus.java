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
package com.htmlhifive.resourcefw.service;

import com.htmlhifive.resourcefw.service.processing.ProcessContinuationStrategy;

/**
 * 多重化リクエストにおける各リクエストの処理結果をもとに継続判定を行った結果を表す列挙型.<br>
 * {@link ProcessContinuationStrategy ProcessContinuationStrategy}によって決定されます.
 *
 * @author kishigam
 * @see ProcessContinuationStrategy
 */
public enum ResourceProcessingStatus {
	/**
	 * 継続
	 */
	CONTINUE,

	/**
	 * 中断
	 */
	TERMINATE
}
