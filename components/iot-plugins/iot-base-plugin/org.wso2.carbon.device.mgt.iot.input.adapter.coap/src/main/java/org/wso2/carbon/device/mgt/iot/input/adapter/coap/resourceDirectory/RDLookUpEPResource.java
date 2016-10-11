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
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;


import java.util.*;

public class RDLookUpEPResource extends CoapResource {

    private RDResource rdResource = null;

    public RDLookUpEPResource(String resourceIdentifier, RDResource rd) {
        super(resourceIdentifier);
        this.rdResource = rd;
    }


    @Override
    public void handleGET(CoapExchange exchange) {
        Collection<Resource> resources = rdResource.getChildren();
        List<String> query = exchange.getRequestOptions().getUriQuery();
        String result = "";
        String domainQuery = "";
        String endpointQuery = "";
        TreeSet<String> endpointTypeQuery = new TreeSet<String>();

        for (String q:query) {
            LinkAttribute attr = LinkAttribute.parse(q);
            if(attr.getName().equals(LinkFormat.DOMAIN)){
                domainQuery = attr.getValue();
            }
            if(attr.getName().equals(LinkFormat.END_POINT)){
                endpointQuery = attr.getValue();

            }
            if(attr.getName().equals(LinkFormat.END_POINT_TYPE)){
                Collections.addAll(endpointTypeQuery, attr.getValue().split(" "));
            }
        }

        Iterator<Resource> resIt = resources.iterator();

        while (resIt.hasNext()){
            Resource res = resIt.next();
            if (res.getClass() == RDNodeResource.class){
                RDNodeResource node = (RDNodeResource) res;
                if ( (domainQuery.isEmpty() || domainQuery.equals(node.getDomain())) &&
                        (endpointQuery.isEmpty() || endpointQuery.equals(node.getEndpointIdentifier())) &&
                        (endpointTypeQuery.isEmpty() || endpointTypeQuery.contains(node.getEndpointType()))) {

                    result += "<"+node.getContext()+">;"+LinkFormat.END_POINT+"=\""+node.getEndpointIdentifier()+"\"";
                    result += ";"+LinkFormat.DOMAIN+"=\""+node.getDomain()+"\"";
                    if(!node.getEndpointType().isEmpty()){
                        result += ";"+LinkFormat.RESOURCE_TYPE+"=\""+node.getEndpointType()+"\"";
                    }

                    result += ",";
                }
            }
        }
        if(result.isEmpty()){
            exchange.respond(CoAP.ResponseCode.NOT_FOUND);
        }
        else{
            exchange.respond(CoAP.ResponseCode.CONTENT, result.substring(0,result.length()-1), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        }

    }
}


