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
import org.eclipse.californium.tools.resources.RDResource;

public class RDRootResource extends RDResource{

    @Override
    public void handlePOST(CoapExchange exchange) {
        super.handlePOST(exchange);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        super.handleGET(exchange);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        super.handlePUT(exchange);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        super.handleDELETE(exchange);
    }
}
