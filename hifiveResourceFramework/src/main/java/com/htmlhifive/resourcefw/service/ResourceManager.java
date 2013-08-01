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

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.htmlhifive.resourcefw.config.MessageMetadata;
import com.htmlhifive.resourcefw.exception.AbstractResourceException;
import com.htmlhifive.resourcefw.message.RequestMessage;
import com.htmlhifive.resourcefw.resource.ResourceMethodInvoker;

/**
 * アプリケーション内に存在するリソースを管理するマネージャインターフェース.<br>
 *
 * @author kishigam
 * @see DefaultResourceManager
 */
public interface ResourceManager {

	/**
	 * リソースを特定し、{@link ResourceMethodInvoker ResourceMethodInvoker}オブジェクトを返します.<br>
	 * リソース名とアクションによってリソースクラスとメソッドが決定されます.<br>
	 *
	 * @param name リソース名
	 * @param action アクション名
	 * @param requestMessage リクエストメッセージ
	 * @return 特定されたリソースでactionを実行するためのResourceMethodInvoker
	 * @throws AbstractResourceException
	 */
	ResourceMethodInvoker getResourceMethodByName(String name, String action, RequestMessage requestMessage)
			throws AbstractResourceException;

	/**
	 * 指定されたリソースのタイプ(Content-Type)とアクション名から、{@link ResourceMethodInvoker ResourceMethodInvoker}オブジェクトを返します.
	 * リソースのタイプとアクションによってリソースクラスとメソッドが決定されます.<br>
	 */
	ResourceMethodInvoker getResourceMethodByType(String type, String action, RequestMessage requestMessage)
			throws AbstractResourceException;

	/**
	 * 指定されたactionを実行できる全てのリソースクラスのクラスオブジェクトを返します.
	 *
	 * @return リソースのクラスオブジェクトのSet
	 */
	Set<Class<?>> getResourceInfoFor(String action);

	/**
	 * プロパティファイルによるリソース定義を読み込みます.
	 *
	 * @param resourceDef
	 */
	void setResourceDef(Properties resourceDef);

	/**
	 * リソースのメソッドを定義している型のリストを設定します.<br>
	 * 指定された型で定義されたメソッドはアクションを実行できるメソッドとして検索対象になります.
	 *
	 * @param resourceInterfaceList
	 */
	void setResourceInterfaceList(List<Class<?>> resourceInterfaceList);

	/**
	 * メッセージメタデータオブジェクトを設定します.
	 *
	 * @param messageMetadata
	 */
	void setMessageMetadata(MessageMetadata messageMetadata);
}
