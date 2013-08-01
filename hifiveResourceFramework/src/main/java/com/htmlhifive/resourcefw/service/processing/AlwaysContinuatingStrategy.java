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

/**
 * 多重化リクエストの各リクエスト処理が失敗したとき、常に次のリクエスト処理を継続する、リソース処理継続ロジック実装.
 *
 * @author kishigam
 */
public class AlwaysContinuatingStrategy implements ProcessContinuationStrategy {

	/**
	 * 指定された例外がスローされた場合にリソース処理プロセスを継続するかどうかを判定し、示す{@link ResourceProcessingStatus}オブジェクトを返します. 常に
	 * {@link ResourceProcessingStatus#CONTINUE}を返します.
	 */
	@Override
	public ResourceProcessingStatus continueOnException(Throwable t) {
		return ResourceProcessingStatus.CONTINUE;
	}
}
