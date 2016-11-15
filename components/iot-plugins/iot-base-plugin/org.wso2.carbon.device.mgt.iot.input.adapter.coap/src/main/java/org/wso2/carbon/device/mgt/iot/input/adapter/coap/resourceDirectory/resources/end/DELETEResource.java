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

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.tools.resources.RDNodeResource;

public class DELETEResource extends TagResource{
    public DELETEResource(String name, boolean visible, RDNodeResource parentNode) {
        super(name, visible, parentNode);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        exchange.respond("a DELETE request handling resource "+this.getPath());


    }

}
