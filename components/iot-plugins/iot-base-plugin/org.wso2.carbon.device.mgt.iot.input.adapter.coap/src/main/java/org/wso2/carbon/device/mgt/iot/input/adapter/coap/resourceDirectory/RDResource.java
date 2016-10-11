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
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;



import java.util.List;

public class RDResource extends CoapResource {

    public RDResource() {
        this("rd");
    }

    public RDResource(String resourceIdentifier) {
        super(resourceIdentifier);
        getAttributes().addResourceType("core.rd");
    }

    /*
     * POSTs a new sub-resource to this resource. The name of the new
     * sub-resource is a random number if not specified in the Option-query.
     */
    @Override
    public void handlePOST(CoapExchange exchange) {

        // get name and lifetime from option query
        LinkAttribute attr;
        String endpointIdentifier = "";
        String domain = "local";
        RDNodeResource resource = null;

        CoAP.ResponseCode responseCode;

        LOGGER.info("Registration request: "+exchange.getSourceAddress());

        List<String> query = exchange.getRequestOptions().getUriQuery();
        for (String q:query) {
            // FIXME Do not use Link attributes for URI template variables
            attr = LinkAttribute.parse(q);

            if (attr.getName().equals(LinkFormat.END_POINT)) {
                endpointIdentifier = attr.getValue();
            }

            if (attr.getName().equals(LinkFormat.DOMAIN)) {
                domain = attr.getValue();
            }
        }

        if (endpointIdentifier.equals("")) {
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Missing endpoint (?ep)");
            LOGGER.info("Missing endpoint: "+exchange.getSourceAddress());
            return;
        }

        for (Resource node : getChildren()) {
            if (((RDNodeResource) node).getEndpointIdentifier().equals(endpointIdentifier) && ((RDNodeResource) node).getDomain().equals(domain)) {
                resource = (RDNodeResource) node;
            }
        }

        if (resource==null) {

            String randomName;
            do {
                randomName = Integer.toString((int) (Math.random() * 10000));
            } while (getChild(randomName) != null);

            resource = new RDNodeResource(endpointIdentifier, domain);
            add(resource);

            responseCode = CoAP.ResponseCode.CREATED;
        } else {
            responseCode = CoAP.ResponseCode.CHANGED;
        }

        // set parameters of resource
        if (!resource.setParameters(exchange.advanced().getRequest())) {
            resource.delete();
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            return;
        }

        LOGGER.info("Adding new endpoint: "+resource.getContext());

        // inform client about the location of the new resource
        exchange.setLocationPath(resource.getURI());

        // complete the request
        exchange.respond(responseCode);
    }

}
