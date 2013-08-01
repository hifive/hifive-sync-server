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
package com.htmlhifive.sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.htmlhifive.resourcefw.config.ResourceConfigurer;
import com.htmlhifive.resourcefw.ctrl.ResourceController;
import com.htmlhifive.resourcefw.service.ResourceProcessor;
import com.htmlhifive.sync.ctrl.SyncController;
import com.htmlhifive.sync.resource.DefaultSynchronizer;
import com.htmlhifive.sync.resource.Synchronizer;
import com.htmlhifive.sync.resource.update.ClientResolvingStrategy;
import com.htmlhifive.sync.service.SyncResourceProcessor;

/**
 * resource frameworkにsync機能を付加する動作設定クラス.<br/>
 * {@link ResourceConfigurer ResourceConfigurer} を拡張し、resource frameworkにsync動作を導入します.
 *
 * @see ResourceConfigurer
 * @author kishigam
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "com.htmlhifive.resourcefw.resource", "com.htmlhifive.sync.service" })
public class SyncConfigurer extends ResourceConfigurer {

	/**
	 * Controller(Handler)設定.<br/>
	 * {@link ResourceController ResourceController}のサブクラスである {@link SyncController SyncController}を設定します.
	 */
	@Override
	@Bean
	public ResourceController resourceController() {
		SyncController resourceController = new SyncController();
		resourceController.setSyncConfigurationParameter(syncConfigurationParameter());
		return setUpResourceController(resourceController);
	}

	/**
	 * sync機能用ResourceProcessor設定. <br/>
	 * {@link ResourceProcessor ResourceProcessor}のサブクラスである {@link SyncResourceProcessor SyncResourceProcessor}を設定します.
	 */
	@Override
	@Bean
	public ResourceProcessor resourceProcessor() {
		SyncResourceProcessor resourceProcessor = new SyncResourceProcessor();
		resourceProcessor.setSynchronizer(synchronizer());
		resourceProcessor.setSyncConfigurationParameter(syncConfigurationParameter());
		return setUpResourceProcessor(resourceProcessor);
	}

	/**
	 * Synchronizer設定.
	 */
	@Bean
	public Synchronizer synchronizer() {
		DefaultSynchronizer synchronizer = new DefaultSynchronizer();

		synchronizer.setSyncConfigurationParameter(syncConfigurationParameter());
		synchronizer.setDefaultUpdateStrategy(new ClientResolvingStrategy());

		return synchronizer;
	}

	/**
	 * sync機能動作設定パラメータオブジェクトのBean定義
	 */
	@Bean
	public SyncConfigurationParameter syncConfigurationParameter() {
		return new SyncConfigurationParameter();
	}
}
