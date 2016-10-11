/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.device.mgt.iot.input.adapter.coap.resourceDirectory;

//attributes of the link format

import java.net.URLDecoder;

public class LinkAttribute {

    private String name;
    private String value;

    public LinkAttribute() {

    }

    public LinkAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public LinkAttribute(String name, int value) {
        this.name = name;
        this.value = Integer.valueOf(value).toString();
    }

    public LinkAttribute(String name) {
        this.name = name;
        this.value = "";
    }

    public static LinkAttribute parse(String str) {

        LinkAttribute attr = new LinkAttribute();

        int seperator = str.indexOf("=");
        if (seperator > 0)
            attr.name = URLDecoder.decode(str.substring(0, seperator));
        if (str.length() > seperator + 1)
            attr.value = URLDecoder.decode(str.substring(seperator + 1));

        //check value has double qoutes

        return attr;

    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getIntValue() {
        return Integer.parseInt(value);
    }
}
