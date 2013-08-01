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

import java.util.Map;

import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.service.ResourceProcessingStatus;

/**
 * 多重化リクエストの各リクエスト処理が失敗したとき、その原因となったフレームワーク例外ごとに処理継続判断を行うリソース処理継続ロジック実装.<br>
 * 設定ファイルの内容に従って判断を実行します.
 *
 * @author kishigam
 */
public class ConfigurableProcessContinuationStrategy implements ProcessContinuationStrategy {

	/**
	 * 例外とそれがスローされたときのプロセス継続可否を示すResourceProcessingStatusのMap.
	 */
	private Map<Class<? extends AbstractResourceException>, ResourceProcessingStatus> strategyMap;

	/**
	 * 指定された例外がスローされた場合にリソース処理プロセスを継続するかどうかを判定し、示す{@link ResourceProcessingStatus}オブジェクトを返します.<br/>
	 * 例外ごとに設定によりマッピングされたResourceProcessingStatusを返します.<br/>
	 * 設定がない場合、{@link ResourceProcessingStatus#TERMINATE TERMINATE}を返します.
	 */
	@Override
	public ResourceProcessingStatus continueOnException(Throwable t) {
		for (Class<? extends AbstractResourceException> clazz : strategyMap.keySet()) {
			if (clazz.isInstance(t)) {
				return strategyMap.get(clazz);
			}
		}

		return ResourceProcessingStatus.TERMINATE;
	}

	/**
	 * @return the strategyMap
	 */
	public Map<Class<? extends AbstractResourceException>, ResourceProcessingStatus> getStrategyMap() {
		return strategyMap;
	}

	/**
	 * @param strategyMap the strategyMap to set
	 */
	public void setStrategyMap(Map<Class<? extends AbstractResourceException>, ResourceProcessingStatus> strategyMap) {
		this.strategyMap = strategyMap;
	}
}
