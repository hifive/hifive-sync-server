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
package com.htmlhifive.sync.jsonctrl;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.resource.SyncMethod;

/**
 * JSON形式のリクエスト、レスポンスをリソースに対する同期メソッド、エレメントと相互変換するクラス.
 *
 * @author kishigam
 */
@Service
public class JsonDataConvertor {

    /**
     * JSONデータに含まれる同期アクションからリソースに対する同期メソッドに変換するためのテーブル.
     */
    private static final Map<SyncAction, SyncMethod> actionToMethodMap;

    /**
     * リソースに対する同期メソッドからJSONデータに含まれる同期アクションに変換するためのテーブル.
     */
    private static final Map<SyncMethod, SyncAction> methodToActionMap;

    static {
        actionToMethodMap = new HashMap<>();
        actionToMethodMap.put(SyncAction.UPDATE, SyncMethod.PUT);
        actionToMethodMap.put(SyncAction.DELETE, SyncMethod.DELETE);
        actionToMethodMap.put(SyncAction.CREATE, SyncMethod.POST);

        methodToActionMap = new HashMap<>();
        methodToActionMap.put(SyncMethod.PUT, SyncAction.UPDATE);
        methodToActionMap.put(SyncMethod.DELETE, SyncAction.DELETE);
        methodToActionMap.put(SyncMethod.POST, SyncAction.CREATE);
    }

    /**
     * JSON形式のエレメントデータをエレメント型に変換して返します.
     *
     * @param elementObj
     *            エレメントデータ(JSON形式)
     * @return エレメント
     * @throws BadRequestException
     *             エレメントデータがエレメントの型に適合しないとき
     */
    public static <E> E convertJSONToElement(Object elementObj, Class<E> convertTo) {

        Object element = null;
        try {
            // JSONデータからの型へ変換
            element = new ObjectMapper().convertValue(elementObj, convertTo);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new BadRequestException("JSON data of sync resource cannot accept. ", e);
        }

        return convertTo.cast(element);
    }

    /**
     * クライアントとのリクエスト、レスポンスに含まれる同期アクションを、リソースに対する同期メソッドに変換します.
     *
     * @param action
     *            同期アクション
     * @return 対応する同期メソッド
     */
    public static SyncMethod convertActionToSyncMethod(SyncAction action) {
        return actionToMethodMap.get(action);
    }

    /**
     * リソースに対する同期メソッドを、クライアントとのリクエスト、レスポンスに含まれる同期アクションに変換します.
     *
     * @param syncMethod
     *            同期メソッド 同期
     * @return 対応する同期アクション
     */
    public static SyncAction convertSyncMethodToAction(SyncMethod syncMethod) {
        return methodToActionMap.get(syncMethod);
    }
}
