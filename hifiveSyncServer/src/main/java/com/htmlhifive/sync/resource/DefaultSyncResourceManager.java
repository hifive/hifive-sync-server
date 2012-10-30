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
package com.htmlhifive.sync.resource;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.SyncException;
import com.htmlhifive.sync.resource.lock.LockStrategy;
import com.htmlhifive.sync.resource.lock.ResourceLockStatusType;
import com.htmlhifive.sync.resource.update.UpdateStrategy;

/**
 * アプリケーション内に存在するリソースを管理するサービス実装.<br>
 * リソースをクラス内に保持し、必要に応じて呼び出すことができます.
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Service
public class DefaultSyncResourceManager implements SyncResourceManager {

	/**
	 * リソース検索対象のコンテキスト.
	 */
	@Resource
	private ApplicationContext context;

	/**
	 * アプリケーション内のリソースを保持するMap
	 */
	private Map<String, Class<? extends SyncResource<?>>> resourceMap;

	/**
	 * リソースごとのロックモードを保持するMap.<br>
	 * TODO 次期バージョンにて実装予定
	 */
	@Deprecated
	private Map<String, ResourceLockStatusType> requiredLockStatusMap;

	/**
	 * リソースごとのロック戦略オブジェクトを保持するMap.<br>
	 * TODO 次期バージョンにて実装予定
	 */
	@Deprecated
	private Map<String, Class<? extends LockStrategy>> lockStrategyMap;

	/**
	 * リソースごとの更新戦略オブジェクトを保持するMap.<br>
	 */
	private Map<String, Class<? extends UpdateStrategy>> updateStrategyMap;

	/**
	 * リソース全体に適用するプロパティ.
	 */
	private Properties resourceConfigurations;

	/**
	 * フレームワークが利用するデフォルトコンストラクタ.
	 */
	@SuppressWarnings("unused")
	private DefaultSyncResourceManager() {
	}

	/**
	 * インスタンスを生成し、Mapフィールドのセットアップを行います.
	 *
	 * @param syncResourceBaseTypeName リソースの基底タイプのクラス名
	 * @param resourceConfigurations 全リソース共通設定情報
	 */
	public DefaultSyncResourceManager(String syncResourceBaseTypeName, Properties resourceConfigurations) {

		this.resourceMap = new HashMap<>();

		// TODO 次期バージョンにて実装予定
		this.requiredLockStatusMap = new HashMap<>();

		// TODO 次期バージョンにて実装予定
		this.lockStrategyMap = new HashMap<>();

		this.updateStrategyMap = new HashMap<>();

		this.resourceConfigurations = resourceConfigurations;

		init(syncResourceBaseTypeName);
	}

	/**
	 * クラスパス上のリソースを検索し、Mapに保持してこのクラスのインスタンスを生成します.<br>
	 * resourceBaseTypeNameのサブタイプで、{@link SyncResourceService} アノテーションを付与したクラスをリソースとします.
	 *
	 * @param syncResourceBaseTypeName リソースの基底タイプのクラス名
	 */
	private void init(String syncResourceBaseTypeName) {

		Class<?> resourceBaseType;
		try {
			resourceBaseType = Class.forName(syncResourceBaseTypeName);
		} catch (ClassNotFoundException e) {
			throw new SyncException("Class of resource base type is not found. : " + syncResourceBaseTypeName, e);
		}

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(SyncResourceService.class));

		Set<BeanDefinition> candidates = scanner.findCandidateComponents("");
		for (BeanDefinition def : candidates) {

			Class<? extends SyncResource<?>> resourceClass = extractResource(def, resourceBaseType);
			if (resourceClass == null) {
				continue;
			}

			// リソースの情報をアノテーションから取得
			SyncResourceService resourceAnnotation = resourceClass.getAnnotation(SyncResourceService.class);

			// リソースを特定する
			String resourceName = resourceAnnotation.resourceName();
			if (resourceName == null || resourceName.isEmpty()) {
				continue;
			}
			resourceMap.put(resourceName, resourceClass);

			// ロック状態タイプを特定する
			// TODO 次期バージョンにて実装予定
			requiredLockStatusMap.put(resourceName, resourceAnnotation.requiredLockStatus());

			// ロックマネージャを特定する
			// TODO 次期バージョンにて実装予定
			lockStrategyMap.put(resourceName, resourceAnnotation.lockStrategy());

			// 更新戦略オブジェクトを特定する
			updateStrategyMap.put(resourceName, resourceAnnotation.updateStrategy());
		}
	}

	/**
	 * BeanDefinitionオブジェクトからリソースのクラスオブジェクトを抽出して返します.<br>
	 *
	 * @param def リソース候補のクラスのBeanDefinition
	 * @param syncResourceBaseType リソースの基底タイプ
	 * @return リソースのクラスオブジェクト
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends SyncResource<?>> extractResource(BeanDefinition def, Class<?> syncResourceBaseType) {

		Class<?> found;
		try {
			found = Class.forName(def.getBeanClassName());
		} catch (ClassNotFoundException e) {
			throw new SyncException("An Exception thrown by SyncResourceManager", e);
		}

		// null,interface, resourceBaseTypeNameで指定された基底タイプのサブタイプ以外, abstractのクラスを除外
		if (found == null || found.isInterface() || !syncResourceBaseType.isAssignableFrom(found)
				|| Modifier.isAbstract(found.getModifiers())) {
			return null;
		} else {
			return (Class<? extends SyncResource<?>>) found;
		}
	}

	/**
	 * リソース名から対応するリソースを返します.<br>
	 * リソースが存在しない場合、nullを返します.
	 *
	 * @param resourceName リソース名
	 * @return リソースクラス
	 */
	@Override
	public SyncResource<?> locateSyncResource(String resourceName) {

		Class<? extends SyncResource<?>> resourceClass = resourceMap.get(resourceName);

		if (resourceClass == null) {
			return null;
		}

		SyncResource<?> resource = context.getBean(resourceClass);

		// LockStrategy,UpdateStrategyのセット

		// TODO 次期バージョンにて実装予定
		resource.setRequiredLockStatus(requiredLockStatusMap.get(resourceName));

		// TODO 次期バージョンにて実装予定
		resource.setLockStrategy(context.getBean(lockStrategyMap.get(resourceName)));

		resource.setUpdateStrategy(context.getBean(updateStrategyMap.get(resourceName)));

		// リソース設定情報を適用する
		resource.applyResourceConfigurations(resourceConfigurations);

		return resource;
	}

	/**
	 * このマネージャが管理する全リソースのリソース名をリストで返します.
	 *
	 * @return リソース名のリスト
	 */
	@Override
	public List<String> allResourcNames() {

		return new ArrayList<>(resourceMap.keySet());
	}
}
