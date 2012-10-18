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
 * リソースとしてのサービスを提供するクラスであることを指示するアノテーション.<br>
 *
 * @author kishigam
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface SyncResourceService {

	/**
	 * 対象リソース名.<br>
	 *
	 * @return リソース名
	 */
	String resourceName();

	/**
	 * リソースが採用する(要求する)ロック状態のタイプ.
	 *
	 * @return ロック状態タイプ
	 */
	ResourceLockStatusType requiredLockStatus() default ResourceLockStatusType.UNLOCK;

	/**
	 * リソースが使用するロック戦略の実装クラス.<br>
	 * requiredLockStatusが{@link ResourceLockStatusType#UNLOCK}の場合は使用されません.
	 *
	 * @return LockManagerのクラスオブジェクト
	 */
	Class<? extends LockStrategy> lockStrategy() default ResourceItemCommonLockStrategy.class;

	/**
	 * リソースが使用する競合発生時のリソースアイテム更新戦略の実装クラス.<br>
	 * requiredLockStatusが{@link ResourceLockStatusType#UNLOCK}の場合のみ使用されます.
	 *
	 * @return LockManagerのクラスオブジェクト
	 */
	Class<? extends UpdateStrategy> updateStrategy() default ClientResolvingStrategy.class;
}
