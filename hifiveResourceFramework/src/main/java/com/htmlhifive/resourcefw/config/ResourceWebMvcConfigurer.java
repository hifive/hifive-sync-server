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
package com.htmlhifive.resourcefw.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.htmlhifive.resourcefw.ctrl.ResourceHandler;
import com.htmlhifive.resourcefw.ctrl.ResourceHandlerMapping;
import com.htmlhifive.resourcefw.message.MessageContainerMethodProcessor;

/**
 * フレームワークのController層の設定クラス.<br/>
 * {@link WebMvcConfigurationSupport WebMvcConfigurationSupport}を拡張し、SpringMVCのController以前のクラスの拡張設定が記述されます.
 *
 * @author kishigam
 */
@Configuration
public class ResourceWebMvcConfigurer extends WebMvcConfigurationSupport {

	/**
	 * フレームワークデフォルトコンポーネント設定
	 */
	@Autowired
	private ResourceConfigurer serviceConfigurer;

	/**
	 * HandlerMapping設定.<br>
	 * RequestMappingHandlerMappingを拡張します.<br>
	 * URLの最上位パスがサービスパス({@link MessageMetadata MessageMetadata}参照)であるリクエストを処理するControllerクラス、オブジェクトを限定します.
	 */
	@Override
	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {

		RequestMappingHandlerMapping handlerMapping = new ResourceHandlerMapping(
				serviceConfigurer.resourceConfigurationParameter().SERVICE_ROOT_PATH, ResourceHandler.class,
				serviceConfigurer.resourceController());
		handlerMapping.setOrder(0);
		handlerMapping.setRemoveSemicolonContent(false);
		handlerMapping.setInterceptors(getInterceptors());
		handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());
		return handlerMapping;
	}

	/**
	 * {@link RequestMappingHandlerAdapter RequestMappingHandlerAdapter}におけるArgumentResolver設定.<br>
	 * {@link MessageContainerMethodProcessor MessageContainerMethodProcessor}を追加します.
	 */
	@Override
	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

		MessageContainerMethodProcessor argumentResolver = new MessageContainerMethodProcessor(getMessageConverters(),
				serviceConfigurer.messageMetadata());
		argumentResolvers.add(argumentResolver);
	}

	/**
	 * {@link RequestMappingHandlerAdapter RequestMappingHandlerAdapter}におけるReturnValueHandler設定.<br>
	 * {@link MessageContainerMethodProcessor MessageContainerMethodProcessor}を追加します.
	 */
	@Override
	protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {

		MessageContainerMethodProcessor returnValueHandler = new MessageContainerMethodProcessor(
				getMessageConverters(), mvcContentNegotiationManager(), serviceConfigurer.messageMetadata());
		returnValueHandlers.add(returnValueHandler);
	}

	/**
	 * {@link ContentNegotiationManager ContentNegotiationManager}を設定するConfigurerのBean定義.<br>
	 * 「ACCEPT」メタデータによるレスポンスのContent-Type指定を有効にし、HTTP HeaderのAcceptフィールドを無効にします.
	 *
	 * @see MessageMetadata
	 */
	@Override
	protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false).favorParameter(true)
				.parameterName(serviceConfigurer.messageMetadata().ACCEPT).ignoreAcceptHeader(true);
	}
}
