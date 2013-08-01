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
package com.htmlhifive.resourcefw.ctrl;

import static org.apache.log4j.Logger.getLogger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.htmlhifive.resourcefw.config.MessageMetadata;

/**
 * {@link RequestMappingHandlerMapping RequestMappingHandlerMapping}のフレームワーク拡張.<br>
 * 特定の リクエストURLをフレームワークのController(Handler)にマッピングします.
 *
 * @author kishigam
 */
public class ResourceHandlerMapping extends RequestMappingHandlerMapping {

	private static final Logger LOGGER = getLogger(ResourceHandlerMapping.class);

	/**
	 * パス区切り文字.
	 */
	private static final String PATH_SEPARATER = "/";

	/**
	 * サービスルートパス(先頭パス区切り文字)
	 */
	private final String serviceRootPathWithSlash;

	/**
	 * マッピング対象のHandler(Controller)オブジェクト.
	 */
	private final Object handler;

	/**
	 * マッピング対象のHandler(Controller)メソッド.
	 */
	private final Method handlerMethod;

	/**
	 * サービスルートパス、Handlerメソッドに付与するるアノテーション、Handlerオブジェクトを受け取り、これらに従って動作するRequestMappingインスタンスを生成します.
	 *
	 * @param serviceRootPath サービスルートパス(スラッシュを含まない)
	 * @param handlerMethodAnnotation Handlerメソッドに付与するアノテーション
	 * @param handler　マッピング対象のHandlerオブジェクト(Controllerクラス)
	 */
	public ResourceHandlerMapping(String serviceRootPath, Class<? extends Annotation> handlerMethodAnnotation,
			Object handler) {

		this.serviceRootPathWithSlash = PATH_SEPARATER + serviceRootPath;
		this.handler = handler;

		// アノテーションが付与されている最初のメソッドをHandlerメソッドとする
		for (Method method : handler.getClass().getMethods()) {
			if (method.isAnnotationPresent(handlerMethodAnnotation)) {
				this.handlerMethod = method;
				LOGGER.info("[resourcefw] ResourceController is activated. service root path = "
						+ serviceRootPathWithSlash);
				return;
			}
		}
		LOGGER.warn("[resourcefw] ResourceController cannot be activated. service root path = "
				+ serviceRootPathWithSlash);
		this.handlerMethod = null;
	}

	/**
	 * リクエストにマッチするHandlerメソッドをルックアップします. <br/>
	 * リクエストURLの最上位パスが{@link MessageMetadata MessageMetadata}で設定されたサービスルートパスと一致する場合、 このクラスのHandlerメソッドを返します.
	 * そうでない場合、SpringMVCの{@link RequestMapping RequestMapping}を用いた通常動作になります.<br/>
	 * (継承元の{@link RequestMappingHandlerMapping RequestMappingHandlerMapping}に処理が戻ります)<br>
	 */
	@Override
	protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {

		if (lookupPath.startsWith(serviceRootPathWithSlash)) {
			doHandleMatch(doCreateRequestMappingInfo(), lookupPath, request);
			return new HandlerMethod(handler, handlerMethod);
		}

		return super.lookupHandlerMethod(lookupPath, request);
	}

	/**
	 * 継承元のhandleMatchメソッドと同等の処理を実行します.<br>
	 * このMappingにおいて不要な処理は実行しません.
	 *
	 * @see RequestMappingInfoHandlerMapping#handleMatch
	 */
	private void doHandleMatch(RequestMappingInfo info, String lookupPath, HttpServletRequest request) {

		request.setAttribute(PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);

		Set<String> patterns = info.getPatternsCondition().getPatterns();
		String bestPattern = patterns.isEmpty() ? lookupPath : patterns.iterator().next();
		request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);
	}

	/**
	 * 継承元のcreateRequestMappingInfoメソッドと同等の処理を実行します.<br>
	 * このMappingにおいて不要な処理は実行しません.
	 */
	private RequestMappingInfo doCreateRequestMappingInfo() {

		String[] patterns = resolveEmbeddedValuesInPatterns(new String[] { serviceRootPathWithSlash });

		// リクエスト(パス)パターン以外の情報を持たないRequestMappingInfoオブジェクト
		return new RequestMappingInfo(new PatternsRequestCondition(patterns), null, null, null, null, null, null);
	}
}
