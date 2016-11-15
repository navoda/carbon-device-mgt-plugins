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
package org.wso2.carbon.device.mgt.iot.input.adapter.coap.resourceDirectory.resources.end;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.californium.tools.resources.RDNodeResource;
import org.wso2.carbon.device.mgt.iot.input.adapter.coap.resourceDirectory.resources.TagResource;

import java.util.regex.Pattern;

/**
 * resource that add at the end of a path which starts from a NodeResource
 */

public class EndResource extends TagResource{

	public EndResource(String name, boolean visible, RDNodeResource parentNode) {
		super(name, visible, parentNode);
	}

	public Header setHeader(String attribute, String value) {
		Header header = null;

		//header
		final String AUTHORIZATION_HEADER = "Authorization"; //can be used to set other header parameters [only authorization atm]

		//header values
		final Pattern AUTHORIZATION_PATTERN = Pattern.compile("[B|b]earer\\s");

		if (attribute.equalsIgnoreCase(AUTHORIZATION_HEADER)) {
			if (AUTHORIZATION_PATTERN.matcher(value).find()) {
				header = new BasicHeader(AUTHORIZATION_HEADER, value);
			}
		}

		return header;
	}
}
