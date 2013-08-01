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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.exception.GenericResourceException;
import com.htmlhifive.resourcefw.exception.NotFoundException;
import com.htmlhifive.resourcefw.exception.NotImplementedException;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.resource.BasicResource;
import com.htmlhifive.resourcefw.resource.Resource;
import com.htmlhifive.resourcefw.resource.ResourceClass;
import com.htmlhifive.resourcefw.resource.ResourceMethod;
import com.htmlhifive.resourcefw.resource.ResourceMethodInvoker;

/**
 * リソースを管理するマネージャのデフォルト実装.<br>
 * アプリケーション起動時にリソースの情報を収集し、保持します。<br>
 * 外部からリソース名あるいはリソースのタイプ(Content-Typeに類似)とアクションを与えられることにより、管理対象のリソースクラス、メソッドを特定し、呼びだすことができます.
 *
 * @author kishigam
 */
public class DefaultResourceManager implements ResourceManager {

	private static Logger LOGGER = Logger.getLogger(DefaultResourceManager.class);

	/**
	 * フレームワークのベースパッケージ.<br>
	 * このパッケージに含まれるリソースは、フレームワーク提供の(汎用の)リソースとして管理対象に含まれます.
	 */
	private static final String BASE_PACKAGE = "com.htmlhifive.resourcefw";

	/**
	 * リソースクラスの接尾語.<br>
	 * アノテーションやプロパティでリソース名が与えられていない場合、リソースクラス名からこの接尾語を除き、全て小文字にしたものがリソース名になります.
	 */
	private static final String RESOURCE_CLASS_NAME_POSTFIX = "Resource";

	/**
	 * アプリケーションコンテキスト.
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * メッセージメタデータオブジェクト.
	 */
	private MessageMetadata messageMetadata;

	/**
	 * アクションを実行するリソースメソッドを探すインターフェース（抽象クラス、クラス）のリスト.
	 */
	private List<Class<?>> resourceInterfaceList = new ArrayList<>();

	/**
	 * プロパティファイルで定義されたリソース定義のリスト.<br>
	 */
	private Properties resourceDef = new Properties();

	/**
	 * 管理対象のリソースを保持するMap.<br>
	 * リソース名でリソースオブジェクトが取得できます.
	 */
	private Map<String, Class<?>> resourceNameMap = new HashMap<>();

	/**
	 * 管理対象のリソースを保持するMap.<br>
	 * タイプ(ContentType)で、そのタイプを受け付けることができるリソースオブジェクトのリストが取得できます.
	 */
	private MultiValueMap<String, Class<?>> resourceTypeMap = new LinkedMultiValueMap<>();

	/**
	 * デフォルトの動作をするリソースマネージャを生成します.
	 */
	public DefaultResourceManager() {

		// resourceInterfaceList のデフォルト設定
		this.resourceInterfaceList.add(BasicResource.class);
	}

	/**
	 * リソースを特定し、{@link ResourceMethodInvoker ResourceMethodInvoker}オブジェクトを返します.<br>
	 * リソース名とアクションによってリソースクラスとメソッドが決定されます.<br>
	 */
	@Override
	public ResourceMethodInvoker getResourceMethodByName(String name, String action, RequestMessage requestMessage)
			throws NotFoundException, NotImplementedException {

		// リソース定義プロパティを先に探す
		ResourceMethodInvoker found = searchActionMethodFromResource(name, action);
		if (found != null) {
			return found;
		}

		// アノテーションをスキャンした結果のresourceNameMapから探す
		Class<?> resourceClass = resourceNameMap.get(name);
		if (resourceClass == null) {
			throw new NotFoundException("name = " + name, requestMessage);
		}
		Method resourceMethod = searchActionMethod(resourceClass, action);
		if (resourceMethod == null) {
			throw new NotImplementedException("Unsupported resource action : name = " + name + " , action = " + action,
					requestMessage);
		}

		return new ResourceMethodInvoker(applicationContext.getBean(resourceClass), resourceMethod);
	}

	/**
	 * 指定されたリソースのタイプ(Content-Type)とアクション名から、{@link ResourceMethodInvoker ResourceMethodInvoker}オブジェクトを返します.
	 * リソースのタイプとアクションによってリソースクラスとメソッドが決定されます.<br>
	 */
	@Override
	public ResourceMethodInvoker getResourceMethodByType(String type, String action, RequestMessage requestMessage) {

		return null;

		// (リソースチェーン対応予定)
		// resourceDef,resourceTypeMapから探す
		//		List<Class<?>> list = resourceTypeMap.get(type);
		//
		//		if (list != null && !list.isEmpty()) {
		//			for (Class<?> resourceClass : list) {
		//				Method resourceMethod = specifyActionMethod(resourceClass, action);
		//				if (resourceMethod != null) {
		//					// actionに対するMethodは1クラスの1メソッドしかない前提
		//					return new ResourceMethodInvoker(applicationContext.getBean(resourceClass), resourceMethod);
		//				}
		//			}
		//		}
		//
		//		list = resourceTypeMap.get(ResourceClass.ALL_TYPES);
		//		// ALL_TYPESから探す
		//		for (Class<?> resourceClass : list) {
		//			Method resourceMethod = specifyActionMethod(resourceClass, action);
		//			if (resourceMethod != null) {
		//				// actionに対するMethodは1クラスの1メソッドしかない前提
		//				return new ResourceMethodInvoker(applicationContext.getBean(resourceClass), resourceMethod);
		//			}
		//		}
		//
		//		return null;
	}

	/**
	 * リソース定義プロパティから、actionを実行できるメソッド(リソースメソッド)を探し、返します.<br>
	 *
	 * @param name リソース名
	 * @param action アクション名
	 * @return Methodオブジェクト
	 */
	private ResourceMethodInvoker searchActionMethodFromResource(String name, String action) {

		String classFqcn;
		String methodName;

		String def = (String) resourceDef.get(name + "." + action);
		if (def == null) {
			def = (String) resourceDef.get(name + "." + "*");
		}
		if (def == null)
			return null;

		methodName = def.substring(def.lastIndexOf(".") + 1);
		classFqcn = def.substring(0, def.lastIndexOf("."));
		if (methodName.equals("*")) {
			methodName = action;
		}

		Class<?> clazz;
		Method method;
		try {
			clazz = Class.forName(classFqcn);
			method = clazz.getMethod(methodName, RequestMessage.class);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			throw new GenericResourceException("No such resource method, check resource-def properties. : " + classFqcn
					+ "." + methodName, e);
		}

		return new ResourceMethodInvoker(applicationContext.getBean(clazz), method);
	}

	/**
	 * リソースクラスから、actionを実行できるメソッド(リソースメソッド)を探し、返します.<br>
	 *
	 * @param resourceClass リソースクラスのクラスオブジェクト
	 * @param action アクション名
	 * @return Methodオブジェクト
	 */
	private Method searchActionMethod(Class<?> resourceClass, String action) {

		Method[] declaredMethods = resourceClass.getDeclaredMethods();

		// ResourceMethodアノテーションの"action"が付与されているものでマッチング
		for (Method method : declaredMethods) {
			ResourceMethod methodAnnotation = method.getAnnotation(ResourceMethod.class);
			if (methodAnnotation == null) {
				continue;
			}
			if (methodAnnotation.action().equals(action)) {
				return method;
			}
		}

		// 該当するResourceMethodアノテーションがない場合、publicメソッド名でマッチング
		for (Method method : declaredMethods) {
			if (method.getName().equals(action) && Modifier.isPublic(method.getModifiers())) {
				return method;
			}
		}

		// resourceInterfaceListに含まれるリソースインターフェース型で定義されたメソッド名でマッチング
		for (Class<?> resourceInterface : resourceInterfaceList) {
			Method specified = searchActionMethod(resourceInterface, resourceClass, action);
			if (specified != null) {
				return specified;
			}
		}

		return null;
	}

	/**
	 * リソースクラスが指定された型の実装またはサブクラスの時、その型からactionを実行できるメソッド(リソースメソッド)を探し、返します.<br>
	 *
	 * @param fromClass メソッドを探す型
	 * @param resourceClass リソースクラスのクラスオブジェクト
	 * @param action アクション名
	 * @return Methodオブジェクト
	 */
	private Method searchActionMethod(Class<?> fromClass, Class<?> resourceClass, String action) {
		if (fromClass.isAssignableFrom(resourceClass)) {
			for (Method basicMethod : fromClass.getDeclaredMethods()) {
				if (basicMethod.getName().equals(action)) {
					return basicMethod;
				}
			}
		}
		return null;
	}

	/**
	 * 指定されたactionを実行できる全てのリソースクラスのクラスオブジェクトを返します.
	 *
	 * @return リソースのクラスオブジェクトのSet
	 */
	@Override
	public Set<Class<?>> getResourceInfoFor(String action) {

		Set<Class<?>> resultSet = new HashSet<>();

		for (String resourceName : this.resourceNameMap.keySet()) {

			Class<?> resourceClass = resourceNameMap.get(resourceName);
			Method actionMethod = searchActionMethod(resourceClass, action);

			if (actionMethod != null) {
				resultSet.add(resourceClass);
			}
		}
		for (String resourceType : this.resourceTypeMap.keySet()) {

			List<Class<?>> resourceClasses = resourceTypeMap.get(resourceType);
			for (Class<?> resourceClass : resourceClasses) {
				Method actionMethod = searchActionMethod(resourceClass, action);

				if (actionMethod != null) {
					resultSet.add(resourceClass);
				}
			}
		}

		return resultSet;
	}

	/**
	 * マネージャ初期化処理.<br>
	 * ApplicationContextの構築が終わった後で実行するため、{@link PostConstruct PostConstruct}アノテーションを付与しています.
	 */
	@PostConstruct
	protected void init() {

		// リソース定義プロパティが定義されていれば設定
		if (applicationContext.containsBean("resourceDef")) {
			Properties resourceDef = (Properties) applicationContext.getBean("resourceDef");

			for (Object key : resourceDef.keySet()) {
				LOGGER.debug("[resourcefw] Resource definition detected : " + key);
			}

			this.resourceDef = resourceDef;
		}

		Set<Class<?>> resourceClasses = scanResourceClass();

		// スキャンした結果の候補クラス群から情報を収集
		for (Class<?> resourceClass : resourceClasses) {

			putAnnotatedResourceByName(resourceClass);
			putAnnotatedResourceByType(resourceClass);
		}
	}

	/**
	 * ApplicationContext内のBeanをスキャンし、{@link Resource Resource}インターフェースを実装したクラスを収集、クラスオブジェクトのセットを返します.
	 *
	 * @return 条件に合致したClassオブジェクトのセット
	 */
	private Set<Class<?>> scanResourceClass() {

		Set<Class<?>> resultSet = new HashSet<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(ResourceClass.class));

		// フレームワークが提供する汎用リソース実装のスキャン
		Set<BeanDefinition> candidates = scanner.findCandidateComponents(BASE_PACKAGE);
		candidates.addAll(scanner.findCandidateComponents(""));

		for (BeanDefinition def : candidates) {

			Class<?> clazz;
			try {
				clazz = Class.forName(def.getBeanClassName());
			} catch (ClassNotFoundException e) {
				throw new GenericResourceException("An Exception thrown by ResourceManager", e);
			}

			// null, interface, abstractのクラスを除外
			if (clazz == null || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
				continue;
			}
			resultSet.add(clazz);
		}

		return resultSet;
	}

	/**
	 * リソースクラスの情報からリソース名を特定します.<br/>
	 * {@link ResourceClass ResourceClass}アノテーションのname属性が指定されている場合はそれがリソース名になり、
	 * そうでない場合はリソースクラス名から末尾の"Resource"を除去し、すべて小文字にしたものがリソース名になります.
	 *
	 * @param resourceClass リソースクラスのClassオブジェクト
	 * @return　リソース名
	 */
	private void putAnnotatedResourceByName(Class<?> resourceClass) {

		ResourceClass annotation = resourceClass.getAnnotation(ResourceClass.class);
		if (annotation == null) {
			return;
		}

		String resourceName = null;
		if (!annotation.name().equals(ResourceClass.UNDEFINED)) {
			resourceName = annotation.name();
		} else {
			String className = resourceClass.getSimpleName();
			int index = className.lastIndexOf(RESOURCE_CLASS_NAME_POSTFIX);
			resourceName = className.substring(0, index).toLowerCase();
		}

		resourceNameMap.put(resourceName, resourceClass);
		LOGGER.debug("[resourcefw] Resource Class added. name = " + resourceName);
	}

	/**
	 * リソースクラスの情報からリソースタイプを特定し、返します.<br/>
	 * {@link ResourceClass ResourceClass}アノテーションのtype属性が指定されている場合はそれがリソースタイプになります.<br/>
	 *
	 * @param resourceClass リソースクラスのClassオブジェクト
	 * @return　リソースタイプ
	 */
	private void putAnnotatedResourceByType(Class<?> resourceClass) {

		// リソースチェーン対応時に再確認

		//		ResourceClass annotation = resourceClass.getAnnotation(ResourceClass.class);
		//		if (annotation != null && annotation.type().length == 0) {
		//			for (String aType : annotation.type()) {
		//				if (aType.isEmpty()) {
		//					resourceTypeMap.add(aType, resourceClass);
		//					LOGGER.debug("[resourcefw] Resource Class added. acceptable type = " + aType);
		//				}
		//			}
		//		} else {
		//			resourceTypeMap.add(ResourceClass.ALL_TYPES, resourceClass);
		//			LOGGER.debug("[resourcefw] Resource Class added. acceptable type = " + ResourceClass.ALL_TYPES);
		//		}
	}

	/**
	 * @return the messageMetadata
	 */
	protected MessageMetadata getMessageMetadata() {
		return messageMetadata;
	}

	/**
	 * @see ResourceManager#setMessageMetadata(MessageMetadata)
	 */
	@Override
	public void setMessageMetadata(MessageMetadata messageMetadata) {
		this.messageMetadata = messageMetadata;
	}

	/**
	 * @return the resourceInterfaceList
	 */
	protected List<Class<?>> getResourceInterfaceList() {
		return resourceInterfaceList;
	}

	/**
	 * @see ResourceManager#setResourceInterfaceList(List)
	 */
	@Override
	public void setResourceInterfaceList(List<Class<?>> resourceInterfaceList) {
		this.resourceInterfaceList = resourceInterfaceList;
	}

	/**
	 * @return the resourceDef
	 */
	protected Properties getResourceDef() {
		return resourceDef;
	}

	/**
	 * @see ResourceManager#setResourceDef(Properties)
	 */
	@Override
	public void setResourceDef(Properties resourceDef) {
		this.resourceDef = resourceDef;
	}
}
