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


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class RDLookUpDomainResource extends CoapResource {

    private RDResource rdResource = null;

    public RDLookUpDomainResource(String resourceIdentifier, RDResource rd) {
        super(resourceIdentifier);
        this.rdResource = rd;
    }


    @Override
    public void handleGET(CoapExchange exchange) {

        Collection<Resource> resources = rdResource.getChildren();
        TreeSet<String> availableDomains = new TreeSet<String>();
        String domainQuery = "";
        Iterator<Resource> resIt = resources.iterator();
        String result = "";

        List<String> queries = exchange.getRequestOptions().getUriQuery();
        for (String query : queries) {
            LinkAttribute attr = LinkAttribute.parse(query);
            if (attr.getName().equals(LinkFormat.DOMAIN))
                domainQuery = attr.getValue();
        }

        while (resIt.hasNext()) {
            Resource res = resIt.next();
            if (res.getClass() == RDNodeResource.class) {
                RDNodeResource node = (RDNodeResource) res;
                if ((domainQuery.isEmpty() || domainQuery.equals(node.getDomain()))) {
                    availableDomains.add(node.getDomain());
                }
            }
        }
        if (availableDomains.isEmpty()) {
            exchange.respond(CoAP.ResponseCode.NOT_FOUND);

        } else {
            Iterator<String> domIt = availableDomains.iterator();

            while (domIt.hasNext()) {
                String dom = domIt.next();
                result += "</rd>;" + LinkFormat.DOMAIN + "=\"" + dom + "\",";
            }

            exchange.respond(CoAP.ResponseCode.CONTENT, result.substring(0, result.length() - 1), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        }

    }

}
