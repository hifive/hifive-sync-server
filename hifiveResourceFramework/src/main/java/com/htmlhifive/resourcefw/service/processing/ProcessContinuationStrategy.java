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
package com.htmlhifive.resourcefw.service.processing;

import com.htmlhifive.resourcefw.service.ResourceProcessingStatus;
import com.htmlhifive.resourcefw.service.ResourceProcessor;

/**
 * リソース処理継続ロジックインタフェース.<br>
 * 多重化リクエストにおいて{@link ResourceProcessor ResourceProcessor}の処理中にリソースが例外をスローした時、 以降のリクエストの処理方法を判定します.
 *
 * @author kishigam
 */
public interface ProcessContinuationStrategy {

	/**
	 * 指定された例外がスローされた場合にリソース処理プロセスを継続するかどうかを判定し、示す{@link ResourceProcessingStatus}オブジェクトを返します.
	 *
	 * @param t スローされた例外
	 * @return {@link ResourceProcessingStatus}
	 */
	ResourceProcessingStatus continueOnException(Throwable t);
}
