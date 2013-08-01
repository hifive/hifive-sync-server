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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.htmlhifive.resourcefw.ctrl.ResourceController;
import com.htmlhifive.resourcefw.ctrl.ResourceExceptionHandler;
import com.htmlhifive.resourcefw.service.DefaultResourceManager;
import com.htmlhifive.resourcefw.service.DefaultResourceProcessor;
import com.htmlhifive.resourcefw.service.ResourceManager;
import com.htmlhifive.resourcefw.service.ResourceProcessor;
import com.htmlhifive.resourcefw.service.processing.AlwaysTerminatingStrategy;
import com.htmlhifive.resourcefw.service.processing.ProcessContinuationStrategy;

/**
 * フレームワークの動作設定クラス.<br/>
 * {@link ResourceController ResourceController}以降のサービスクラス群のBean定義、インジェクション設定を記述します.
 * applicationContext.xmlのような設定ファイルと同じ役割を担います.<br>
 * {@link Transactional Transactional}アノテーションの動作を有効にするため、 {@link EnableTransactionManagement
 * EnableTransactionManagement}アノテーションを付与しています.
 *
 * @see ResourceWebMvcConfigurer
 * @author kishigam
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "com.htmlhifive.resourcefw.resource" })
public class ResourceConfigurer {

	/**
	 * アプリケーションコンテキスト
	 */
	@Autowired
	ApplicationContext applicationContext;

	/**
	 * Multipartリクエストを処理するResolverを有効にする設定.
	 */
	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

	/**
	 * Controller(Handler)設定.<br>
	 */
	@Bean
	public ResourceController resourceController() {
		ResourceController resourceController = new ResourceController();
		return setUpResourceController(resourceController);
	}

	/**
	 * Controller(Handler)のプロパティを設定します.<br>
	 *
	 * @param resourceController
	 * @return {@link ResourceController}
	 */
	protected ResourceController setUpResourceController(ResourceController resourceController) {
		resourceController.setResourceProcessor(resourceProcessor());
		resourceController.setMessageMetadata(messageMetadata());
		resourceController.setResourceConfigurationParameter(resourceConfigurationParameter());
		return resourceController;
	}

	/**
	 * ExceptionHandler設定.<br>
	 */
	@Bean
	public ResourceExceptionHandler resourceExceptionHandler() {
		ResourceExceptionHandler resourceExceptionHandler = new ResourceExceptionHandler();

		return setUpResourceExceptionHandler(resourceExceptionHandler);
	}

	/**
	 * ExceptionHandlerのプロパティを設定します.<br>
	 *
	 * @param resourceExceptionHandler
	 * @return {@link ResourceExceptionHandler}
	 */
	protected ResourceExceptionHandler setUpResourceExceptionHandler(ResourceExceptionHandler resourceExceptionHandler) {
		resourceExceptionHandler.setMessageMetadata(messageMetadata());
		resourceExceptionHandler.setResourceConfigurationParameter(resourceConfigurationParameter());
		return resourceExceptionHandler;
	}

	/**
	 * リソースプロセッサのBean定義
	 */
	@Bean
	public ResourceProcessor resourceProcessor() {
		ResourceProcessor resourceProcessor = new DefaultResourceProcessor();

		return setUpResourceProcessor(resourceProcessor);
	}

	/**
	 * リソースプロセッサのプロパティを設定します.
	 *
	 * @param resourceProcessor
	 * @return {@link ResourceProcessor}
	 */
	protected ResourceProcessor setUpResourceProcessor(ResourceProcessor resourceProcessor) {

		boolean strategyExists = applicationContext.containsBean("processContinuationStrategy");
		ProcessContinuationStrategy strategy = strategyExists ? (ProcessContinuationStrategy) applicationContext
				.getBean("processContinuationStrategy") : new AlwaysTerminatingStrategy();

		resourceProcessor.setProcessContinuationStrategy(strategy);
		resourceProcessor.setMessageMetadata(messageMetadata());
		resourceProcessor.setResourceConfigurationParameter(resourceConfigurationParameter());
		resourceProcessor.setResourceManager(resourceManager());

		return resourceProcessor;
	}

	/**
	 * リソースマネージャのBean定義
	 */
	@Bean
	public ResourceManager resourceManager() {
		DefaultResourceManager manager = new DefaultResourceManager();

		return setUpResourceManager(manager);

	}

	/**
	 * リソースマネージャのプロパティを設定します.
	 *
	 * @param manager
	 * @return {@link ResourceManager}
	 */
	@SuppressWarnings("unchecked")
	protected ResourceManager setUpResourceManager(DefaultResourceManager manager) {

		manager.setMessageMetadata(messageMetadata());

		// 設定されている場合のみ上書き
		if (applicationContext.containsBean("resourceInterfaceList")) {
			manager.setResourceInterfaceList((List<Class<?>>) applicationContext.getBean("resourceInterfaceList"));
		}

		return manager;
	}

	/**
	 * メッセージメタデータオブジェクトのBean定義
	 */
	@Bean
	public MessageMetadata messageMetadata() {
		return new MessageMetadata();
	}

	/**
	 * フレームワーク動作設定パラメータオブジェクトのBean定義
	 */
	@Bean
	public ResourceConfigurationParameter resourceConfigurationParameter() {
		return new ResourceConfigurationParameter();
	}
}
