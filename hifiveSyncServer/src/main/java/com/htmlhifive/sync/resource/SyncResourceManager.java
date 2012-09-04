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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.resource.separated.SeparatedCommonDataSyncResource;

/**
 * アプリケーション内に存在するリソースを管理するサービスオブジェクト.<br>
 * リソースをクラス内に保持し、必要に応じて呼び出すことができます.
 *
 * @author kishigam
 */
@Service
public class SyncResourceManager {

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
     * クラスパス上のリソース検索し、Mapに保持してこのクラスのインスタンスを生成します.<br>
     * {@link SyncResource} インターフェースを実装し、{@link SyncResourceService}
     * アノテーションを付与したクラスをリソースとします.
     */
    @SuppressWarnings("unchecked")
    public SyncResourceManager() {

        this.resourceMap = new HashMap<>();
        try {
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(SyncResourceService.class));

            // TODO ターゲットパッケージの指定方法検討
            Set<BeanDefinition> beans = scanner.findCandidateComponents("");

            for (BeanDefinition def : beans) {

                Class<? extends SeparatedCommonDataSyncResource<?, ?>> resourceClass;

                // null,interface,SeparatedSyncResourceのサブタイプ以外,abstractのクラスを除外
                Class<?> found = Class.forName(def.getBeanClassName());
                if (found == null
                        || found.isInterface()
                        || !SeparatedCommonDataSyncResource.class.isAssignableFrom(found)
                        || Modifier.isAbstract(found.getModifiers())) {
                    continue;
                } else {
                    resourceClass = (Class<? extends SeparatedCommonDataSyncResource<?, ?>>)found;
                }

                // データモデルを特定する
                String dataModelName =
                        resourceClass.getAnnotation(SyncResourceService.class).syncDataModel();
                if (dataModelName == null || dataModelName.isEmpty()) {
                    continue;
                }

                resourceMap.put(dataModelName, resourceClass);
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("An Exception thrown by SyncResourceLocator", e);
        }
    }

    /**
     * データモデルから対応するリソースを返します.
     *
     * @param dataModelName
     *            データモデル名
     * @return リソースクラス
     */
    public SyncResource<?> locateSyncResource(String dataModelName) {

        SyncResource<?> sr = context.getBean(resourceMap.get(dataModelName));

        // LockManagerのセット
        SyncResourceService resourceServiceAnnotation =
                sr.getClass().getAnnotation(SyncResourceService.class);
        try {
            Class<? extends LockManager> lockManagerClazz = resourceServiceAnnotation.lockManager();
            LockManager lm = lockManagerClazz.newInstance();
            sr.setLockManager(lm);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return sr;
    }

    /**
     * このクラスが管理するリソースに対応する全てのデータモデル名を返します.<br>
     *
     * @return データモデル名のセット
     */
    public Set<String> getAllDataModelNames() {

        return resourceMap.keySet();
    }
}
