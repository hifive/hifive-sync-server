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

/**
 * 同期(上り更新)リクエストに対して指定されている同期アクションを表す列挙型.
 *
 * @author kishigam
 */
public enum SyncAction {

    /**
     * 新規リソースエレメントの生成.
     */
    CREATE,

    /**
     * 既存リソースエレメントの更新.
     */
    UPDATE,

    /**
     * 既存リソースエレメントの削除.
     */
    DELETE;
}
