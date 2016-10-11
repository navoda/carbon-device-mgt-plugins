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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RDLookUpResResource extends CoapResource {

    private RDResource rdResource = null;

    public RDLookUpResResource(String resourceIdentifier, RDResource rd) {
        super(resourceIdentifier);
        this.rdResource = rd;
    }


    @Override
    public void handleGET(CoapExchange exchange) {
        Collection<Resource> resources = rdResource.getChildren();
        String result = "";
        String domainQuery = "";
        String endpointQuery = "";
        List<String> toRemove = new ArrayList<String>();

        List<String> query = exchange.getRequestOptions().getUriQuery();

        for (String q : query) {
            LinkAttribute attr = LinkAttribute.parse(q);
            if(attr.getName().equals(LinkFormat.DOMAIN)){
                domainQuery=attr.getValue();
                if(domainQuery==null){
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
                    return;
                }
                toRemove.add(q);
            }
            if(attr.getName().equals(LinkFormat.END_POINT)){
                endpointQuery = attr.getValue();
                if(endpointQuery==null){
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
                    return;
                }
                toRemove.add(q);
            }
        }


        Iterator<Resource> resIt = resources.iterator();

        query.removeAll(toRemove);

        while (resIt.hasNext()) {
            Resource res = resIt.next();
            if (res instanceof RDNodeResource) {
                RDNodeResource node = (RDNodeResource) res;
                if ( (domainQuery.isEmpty() || domainQuery.equals(node.getDomain())) &&
                        (endpointQuery.isEmpty() || endpointQuery.equals(node.getEndpointIdentifier())) ) {
                    String link = node.toLinkFormat(query);
                    result += (!link.isEmpty()) ? link+"," : "";
                }
            }
        }

        if (result.isEmpty()) {
            exchange.respond(CoAP.ResponseCode.NOT_FOUND);
        } else {
            exchange.respond(CoAP.ResponseCode.CONTENT, result.substring(0, result.length() - 1), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        }

    }
}
