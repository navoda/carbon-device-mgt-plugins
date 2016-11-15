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


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.eclipse.californium.tools.resources.RDNodeResource;
import org.eclipse.californium.tools.resources.RDTagResource;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public class NodeResource extends RDNodeResource {

    private static final Logger LOGGER = Logger.getLogger(NodeResource.class.getCanonicalName());

    //parameter constants
    public static final String URI_TEMPLATE = "ut";

    //interface discription constants
    public static final String GET_RESOURCE = "GET";
    public static final String POST_RESOURCE = "POST";
    public static final String PUT_RESOURCE = "PUT";
    public static final String DELETE_RESOURCE = "DELETE";

    //http client for each endpoint
    public static final HttpClient HTTP_CLIENT= HttpClientBuilder.create().build();

    //FIXME- a hard coded temparary access token
    public String accessToken=null;

    public Set<String> URITemplates;


    public NodeResource(String ep, String domain) {
        super(ep, domain);
        URITemplates = new LinkedHashSet<>();
    }


    @Override
    public CoapResource addEndResource(String name, ResourceAttributes attributes) {

        CoapResource endResource;
        if (attributes.containsAttribute(LinkFormat.INTERFACE_DESCRIPTION)) {
            switch (attributes.getAttributeValues(LinkFormat.INTERFACE_DESCRIPTION).get(0)) {

                case POST_RESOURCE:
                    endResource = new POSTResource(name, true, this);
                    break;
                case PUT_RESOURCE:
                    endResource = new PUTResource(name, true, this);
                    break;
                case DELETE_RESOURCE:
                    endResource = new DELETEResource(name, true, this);
                    break;
                case GET_RESOURCE:
                    endResource = new GETResource(name, true, this);
                    break;
                default:
                    endResource = new RDTagResource(name, true, this);

            }
        } else {
            endResource = new RDTagResource(name, true, this);
        }

        return endResource;
    }






}
