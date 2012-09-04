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
package com.htmlhifive.sync.jsonctrl.upload;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.htmlhifive.sync.jsonctrl.ResponseBody;
import com.htmlhifive.sync.resource.SyncMethod;
import com.htmlhifive.sync.resource.SyncResponse;

/**
 * クライアントからの上り更新リクエストに対するレスポンスデータの抽象クラス.
 *
 * @author kishigam
 */
public abstract class UploadResponse implements ResponseBody {

    /**
     * 上り更新レスポンスの本体となるメッセージオブジェクトのリスト.
     */
    private List<UploadResponseMessage> dataList;

    /**
     * 上り更新レスポンスを生成します.
     *
     * @param datalList
     *            同期レスポンスオブジェクトのリスト
     */
    public UploadResponse(Set<SyncResponse<?>> responseSet) {

        dataList = new ArrayList<>();

        for (SyncResponse<?> response : responseSet) {

            UploadResponseMessage message =
                    response.getHeader().getSyncMethod() == SyncMethod.POST
                            ? new UploadResponseMessageForNewData(response)
                            : new UploadResponseMessageOrdinary(response);

            dataList.add(message);
        }
    }

    /**
     * @return dataList
     */
    public List<UploadResponseMessage> getDataList() {
        return dataList;
    }

    /**
     * @param dataList
     *            セットする dataList
     */
    public void setDataList(List<UploadResponseMessage> dataList) {
        this.dataList = dataList;
    }
}
