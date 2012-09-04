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
package com.htmlhifive.sync.sample.person;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.htmlhifive.sync.exception.DuplicateElementException;
import com.htmlhifive.sync.exception.NotFoundException;
import com.htmlhifive.sync.resource.OptimisticLockManager;
import com.htmlhifive.sync.resource.SyncResourceService;
import com.htmlhifive.sync.resource.separated.SeparatedCommonDataSyncResource;

/**
 * personデータモデルの情報を同期リソースとして公開するためのリソースクラス.<br>
 * 同期データを専用サービスで管理する抽象リソースクラスをこのリソース用に実装します.
 */
@SyncResourceService(syncDataModel = "person", lockManager = OptimisticLockManager.class)
@Transactional(propagation = Propagation.MANDATORY)
public class PersonResource extends SeparatedCommonDataSyncResource<String, PersonResourceElement> {

    /**
     * リソースID文字列を生成するためのprefix.
     */
    private final String TARGET_ID_PREFIX = this.getClass().getName();

    /**
     * エンティティの永続化を担うリポジトリ.
     */
    @Resource
    private PersonRepository repository;

    /**
     * 単一データGETメソッドのリソース別独自処理. <br>
     * エンティティをリポジトリから取得し、elementに設定して返します.
     *
     * @param resourceIdStr
     *            リソースID文字列
     * @return リソースエレメント
     * @throws NotFoundException
     *             指定されたIDのエンティティが存在しない場合
     */
    @Override
    protected PersonResourceElement getImpl(String resourceIdStr) {

        PersonBean gotBean = findBean(resourceIdStr);

        PersonResourceElement element = new PersonResourceElement();
        element.setId(gotBean.getId());
        element.setName(gotBean.getName());
        element.setAge(gotBean.getAge());
        element.setOrganization(gotBean.getOrganization());
        return element;
    }

    /**
     * 複数データGETメソッドのリソース別独自処理. <br>
     * 単一データの取得をループし、Mapに格納して返します.
     *
     * TODO: リポジトリアクセスは一回で済ませる改善
     *
     * @param resourceIdStrSet
     *            リソースID文字列のSet
     * @param queryMap
     *            クエリMap
     * @return リソースエレメント(IDをKeyとするMap)
     * @throws NotFoundException
     *             指定されたIDのエンティティが存在しない場合
     */
    @Override
    protected Map<String, PersonResourceElement> getImpl(
            Set<String> resourceIdStrSet,
            Map<String, String> queryMap) {

        Map<String, PersonResourceElement> elementMap = new HashMap<>();
        for (String resourceIdStr : resourceIdStrSet) {

            PersonResourceElement element = getImpl(resourceIdStr);

            // TODO: queryの適用

            elementMap.put(resourceIdStr, element);
        }
        return elementMap;
    }

    /**
     * PUTメソッドのリソース別独自処理. <br>
     * エンティティをリポジトリから取得し、更新します.
     *
     * @param resourceIdStr
     *            リソースID文字列
     * @param element
     *            更新内容を含むリソースエレメント
     */
    @Override
    protected void putImpl(String resourceIdStr, PersonResourceElement element) {

        PersonBean updatingEntity = findBean(resourceIdStr);

        updatingEntity.setName(element.getName());
        updatingEntity.setAge(element.getAge());
        updatingEntity.setOrganization(element.getOrganization());

        repository.save(updatingEntity);
    }

    /**
     * DELETEメソッドのリソース別独自処理. <br>
     * エンティティをリポジトリから取得し、物理削除します.
     *
     * @param resourceIdStr
     *            リソースID文字列
     */
    @Override
    protected void deleteImpl(String resourceIdStr) {

        PersonBean removingEntity = findBean(resourceIdStr);

        repository.delete(removingEntity);
    }

    /**
     * POSTメソッドのリソース別独自処理. <br>
     * エンティティを新規生成、保存し、採番されたリソースIDを返します.
     *
     * @param newElement
     *            生成内容を含むリソースエレメント
     * @return 採番されたリソースID文字列
     */
    @Override
    protected String postImpl(PersonResourceElement newElement) {

        if (repository.exists(newElement.getId())) {

            throw new DuplicateElementException(
                    getImpl(generateNewResourceIdStr(newElement.getId())));
        }

        PersonBean newEntity = new PersonBean(newElement.getId());

        newEntity.setName(newElement.getName());
        newEntity.setAge(newElement.getAge());
        newEntity.setOrganization(newElement.getOrganization());

        repository.save(newEntity);

        return generateNewResourceIdStr(newEntity.getId());
    }

    /**
     * リソースID文字列から1件のエンティティをリポジトリを検索して取得します. <br>
     * エンティティのIDは{@link this#resolveResourceId(String)}で与えられます.<br>
     * 取得できない場合、{@link NotFoundException}をスローします.
     *
     * @param resourceIdStr
     *            リソースID文字列
     * @return Personエンティティ
     * @throws NotFoundException
     *             IDからエンティティが取得できなかったとき
     */
    private PersonBean findBean(String resourceIdStr) {
        String personId = resolveResourceId(resourceIdStr);

        PersonBean found = repository.findOne(personId);
        if (found == null) {
            throw new NotFoundException("entity not found :" + personId);
        }
        return found;
    }

    /**
     * リソースIDからprefixを除去し、エンティティのIDを返します. <br>
     *
     * @param resourceIdStr
     *            リソースID文字列
     * @return エンティティのID
     */
    @Override
    protected String resolveResourceId(String resourceIdStr) {

        // クラス名を除去し、実データのID文字列を抽出する
        return resourceIdStr.replace(TARGET_ID_PREFIX, "");
    }

    /**
     * エンティティのIDからリソースIDを生成します.<br>
     * リソースID文字列はリソースごとの生成ルールによって生成された文字列で、リソースエレメントを一意に識別します.
     *
     * @param id
     *            エンティティのID
     * @return リソースID
     */
    @Override
    protected String generateNewResourceIdStr(String id) {

        // クラス名＋実データのID文字列で実データIDとする
        return TARGET_ID_PREFIX + id;
    }
}
