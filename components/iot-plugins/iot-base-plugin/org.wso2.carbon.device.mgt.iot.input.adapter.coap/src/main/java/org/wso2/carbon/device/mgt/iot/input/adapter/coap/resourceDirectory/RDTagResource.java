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

import org.eclipse.californium.core.CoapResource;

import java.util.HashMap;
import java.util.HashSet;

public class RDTagResource extends CoapResource {

    private HashMap<String, String> tagsMap;
    private RDNodeResource parentNode;

    public RDTagResource(String resourceIdentifier, boolean hidden, RDNodeResource parent) {
        super(resourceIdentifier, hidden);
        tagsMap = new HashMap<String,String>();
        parentNode = parent;

        setVisible(false);
    }

    public boolean containsTag(String tag, String value){
        if(tagsMap.containsKey(tag.toLowerCase())){
            return tagsMap.get(tag.toLowerCase()).equals(value.toLowerCase());
        }
        return false;
    }

    public boolean containsMultipleTags(HashMap<String, String> tags){
        for(String tag : tags.keySet()){
            if(!containsTag(tag, tags.get(tag))){
                return false;
            }
        }
        return true;
    }

    public HashMap<String, String> getTags(){
        return tagsMap;
    }

    public void addTag(String tag, String value){
        tagsMap.put(tag.toLowerCase(), value.toLowerCase());
    }



    public void addMultipleTags(HashMap<String, String> tags){
        for(String tag : tags.keySet()){
            addTag(tag, tags.get(tag));
        }
    }


    public void removeMultipleTags(HashSet<String> tags){
        for(String tag : tags){
            tagsMap.remove(tag.toLowerCase());
        }
    }

    public RDNodeResource getParentNode(){
        return parentNode;
    }

}

