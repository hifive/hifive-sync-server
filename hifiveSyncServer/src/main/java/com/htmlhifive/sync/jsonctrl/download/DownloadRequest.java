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
package com.htmlhifive.sync.jsonctrl.download;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * JSON形式の下り更新リクエストデータを表現するデータクラス.
 *
 * @author kishigam
 */
public class DownloadRequest {

    /**
     * 同期対象を表すメッセージオブジェクトのリスト.
     */
    private List<DownloadRequestMessage> resources;

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (!(obj instanceof DownloadRequest))
            return false;

        DownloadRequest request = (DownloadRequest)obj;

        return new EqualsBuilder().append(this.resources, request.resources).isEquals();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 37).append(this.resources).hashCode();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return resouces
     */
    public List<DownloadRequestMessage> getResources() {
        return resources;
    }

    /**
     * @param resouces
     *            セットする resouces
     */
    public void setResources(List<DownloadRequestMessage> resouces) {
        this.resources = resouces;
    }
}