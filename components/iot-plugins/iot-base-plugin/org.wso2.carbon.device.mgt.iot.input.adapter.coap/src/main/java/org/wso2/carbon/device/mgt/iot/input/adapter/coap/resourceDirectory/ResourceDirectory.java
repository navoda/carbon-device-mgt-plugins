/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.iot.input.adapter.coap.resourceDirectory;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.config.NetworkConfig;


public class ResourceDirectory extends CoapServer {

    private RDResource rdResource;

    public ResourceDirectory() {
        super();
        init();
    }

    public ResourceDirectory(int... ports) {
        super(ports);
        init();
    }

    public ResourceDirectory(NetworkConfig config, int... ports) {
        super(config, ports);
        init();
    }

    public void init() {
        //directory adds ResourceDiirectory resource and the ResourseDirectoryLookup resource
        rdResource = new RDResource();
        this.add(rdResource);
        this.add(new RDLookUpTopResource(rdResource));

    }


}
