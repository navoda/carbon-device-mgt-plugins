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
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class RDNodeResource extends CoapResource{
    private static final Logger LOGGER = Logger.getLogger(RDNodeResource.class.getCanonicalName());
    private Timer lifetimeTimer;
    private int lifeTime;
    private String endpointIdentifier;
    private String domain;
    private String endpointType;
    private String context;

    public RDNodeResource(String endpointID, String domain) {
        super(endpointID);
        this.endpointIdentifier = endpointID;
        this.domain = domain;
    }

    public boolean setParameters(Request request) {
        int newLifeTime = 86400;
        String newContext = "";
        List query = request.getOptions().getUriQuery();
        Iterator e = query.iterator();

        while(e.hasNext()) {
            String q = (String)e.next();
            LinkAttribute attr = LinkAttribute.parse(q);
            if(attr.getName().equals("lt")) {
                newLifeTime = attr.getIntValue();
                if(newLifeTime < 60) {
                    LOGGER.info("Enforcing minimal RD lifetime of 60 seconds (was " + newLifeTime + ")");
                    newLifeTime = 60;
                }
            }

            if(attr.getName().equals("con")) {
                newContext = attr.getValue();
            }
        }

        this.setLifeTime(newLifeTime);

        try {
            if(newContext.equals("")) {
                URI e1 = new URI("coap", "", request.getSource().getHostName(), request.getSourcePort(), "", "", "");
                this.context = e1.toString().replace("@", "").replace("?", "").replace("#", "");
            } else {
                new URI(newContext);
                this.context=newContext;
            }
        } catch (Exception var8) {
            LOGGER.warning(var8.toString());
            return false;
        }

        return this.updateEndpointResources(request.getPayloadString());
    }

    public CoapResource addNodeResource(String path) {
        Scanner scanner = new Scanner(path);
        scanner.useDelimiter("/");
        String next = "";
        boolean resourceExist = false;
        Object resource = this;

        Object subResource;
        for(subResource = null; scanner.hasNext(); resource = subResource) {
            resourceExist = false;
            next = scanner.next();
            Iterator var7 = ((Resource)resource).getChildren().iterator();

            while(var7.hasNext()) {
                Resource res = (Resource)var7.next();
                if(res.getName().equals(next)) {
                    subResource = (CoapResource)res;
                    resourceExist = true;
                }
            }

            if(!resourceExist) {
                subResource = new RDTagResource(next, true, this);
                ((Resource)resource).add((Resource)subResource);
            }
        }

        ((CoapResource)subResource).setPath(((Resource)resource).getPath());
        ((CoapResource)subResource).setName(next);
        scanner.close();
        return (CoapResource)subResource;
    }

    public void delete() {
        LOGGER.info("Removing endpoint: " + this.getContext());
        if(this.lifetimeTimer != null) {
            this.lifetimeTimer.cancel();
        }

        super.delete();
    }

    public void handleGET(CoapExchange exchange) {
        exchange.respond(CoAP.ResponseCode.FORBIDDEN, "RD update handle");
    }

    public void handlePOST(CoapExchange exchange) {
        if(this.lifetimeTimer != null) {
            this.lifetimeTimer.cancel();
        }

        LOGGER.info("Updating endpoint: " + this.getContext());
        this.setParameters(exchange.advanced().getRequest());
        exchange.respond(CoAP.ResponseCode.CHANGED);
    }

    public void handleDELETE(CoapExchange exchange) {
        this.delete();
        exchange.respond(CoAP.ResponseCode.DELETED);
    }

    public void setLifeTime(int newLifeTime) {
        this.lifeTime = newLifeTime;
        if(this.lifetimeTimer != null) {
            this.lifetimeTimer.cancel();
        }

        this.lifetimeTimer = new Timer();
        this.lifetimeTimer.schedule(new RDNodeResource.ExpiryTask(this), (long)(this.lifeTime * 1000 + 2000));
    }

    private boolean updateEndpointResources(String linkFormat) {
        Scanner scanner = new Scanner(linkFormat);
        scanner.useDelimiter(",");
        ArrayList pathResources = new ArrayList();

        while(scanner.hasNext()) {
            pathResources.add(scanner.next());
        }

        Iterator var4 = pathResources.iterator();

        while(var4.hasNext()) {
            String p = (String)var4.next();
            scanner = new Scanner(p);
            String path = "";
            String pathTemp = "";
            if((pathTemp = scanner.findInLine("</.*?>")) == null) {
                scanner.close();
                return false;
            }

            path = pathTemp.substring(1, pathTemp.length() - 1);
            CoapResource resource = this.addNodeResource(path);
            scanner.useDelimiter(";");
            Iterator attr = resource.getAttributes().getAttributeKeySet().iterator();

            while(attr.hasNext()) {
                String attribute = (String)attr.next();
                resource.getAttributes().clearAttribute(attribute);
            }

            while(scanner.hasNext()) {
                LinkAttribute attr1 = LinkAttribute.parse(scanner.next());
                if(attr1.getValue() == null) {
                    resource.getAttributes().addAttribute(attr1.getName());
                } else {
                    resource.getAttributes().addAttribute(attr1.getName(), attr1.getValue());
                }
            }

            resource.getAttributes().addAttribute("ep", this.getEndpointIdentifier());
        }

        scanner.close();
        return true;
    }

    public String toLinkFormat(List<String> query) {
        StringBuilder builder = new StringBuilder();
        this.buildLinkFormat(this, builder, query);
        if(builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    public String toLinkFormatItem(Resource resource) {
        StringBuilder linkFormat = new StringBuilder();
        linkFormat.append("<" + this.getContext());
        linkFormat.append(resource.getURI().substring(this.getURI().length()));
        linkFormat.append(">");
        return linkFormat.append(LinkFormat.serializeResource(resource).toString().replaceFirst("<.+>", "")).toString();
    }

    private void buildLinkFormat(Resource resource, StringBuilder builder, List<String> query) {
        Resource res;
        if(resource.getChildren().size() > 0) {
            for(Iterator var4 = resource.getChildren().iterator(); var4.hasNext(); this.buildLinkFormat(res, builder, query)) {
                res = (Resource)var4.next();
                if(LinkFormat.matches(res, query)) {
                    builder.append(this.toLinkFormatItem(res));
                    builder.append(',');
                }
            }
        }

    }

    public String getEndpointIdentifier() {
        return this.endpointIdentifier;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getEndpointType() {
        return this.endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    class ExpiryTask extends TimerTask {
        RDNodeResource resource;

        public ExpiryTask(RDNodeResource resource) {
            this.resource = resource;
        }

        public void run() {
            RDNodeResource.this.delete();
        }
    }
}
