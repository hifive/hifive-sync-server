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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * リソースのサービスクラスであることを指示アノテーション.<br>
 *
 * @author kishigam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface SyncResourceService {

	/**
	 * リソースが扱うデータモデル名.<br>
	 *
	 * @return データモデル名
	 */
	String syncDataModel();

	/**
	 * リソースが使用するLockManagerのクラスオブジェクト.<br>
	 *
	 * @return LockManagerのクラスオブジェクト
	 */
	Class<? extends LockManager> lockManager() default OptimisticLockManager.class;

	/**
	 * リソースが使用する更新戦略のクラスオブジェクト.<br>
	 *
	 * @return LockManagerのクラスオブジェクト
	 */
	Class<? extends UpdateStrategy> updateStrategy() default ClientResolvingStrategy.class;
}
