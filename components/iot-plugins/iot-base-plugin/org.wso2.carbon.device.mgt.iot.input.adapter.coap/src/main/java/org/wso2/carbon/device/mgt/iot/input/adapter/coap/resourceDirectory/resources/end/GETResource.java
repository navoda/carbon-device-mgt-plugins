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

import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicRequestLine;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.proxy.HttpTranslator;
import org.eclipse.californium.proxy.TranslationException;
import org.eclipse.californium.tools.resources.RDNodeResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GETResource extends TagResource {

    public GETResource(String name, boolean visible, RDNodeResource parentNode) {
        super(name, visible, parentNode);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {

        //TODO the post method is used to extrace auth information

        Request request = exchange.advanced().getRequest();
        String payload = request.getPayloadString(); //get payload
        exchange.advanced().getRequest().setPayload(""); //make payload null
        URL proxyUri = null;
        HttpRequest httpRequest = null;

        //set http URI by removing 'rd' part in the reousrce path
        try {

            if (request.getOptions().getProxyUri() == null) {
                proxyUri = new URL(this.getParentNode().getContext() + this.getPath().substring(this.getParentNode().getParent().getName().length() + 1)+this.getName());
            } else
                proxyUri = new URL(request.getOptions().getProxyUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //get hosts
        HttpHost httpHost = new HttpHost(proxyUri.getHost(), proxyUri.getPort());

        //create requestline
        RequestLine requestLine = new BasicRequestLine("GET", proxyUri.toString(), HttpVersion.HTTP_1_1);

        //get http entity
        try {
            HttpEntity httpEntity = HttpTranslator.getHttpEntity(request);

            // create the http request
            if (httpEntity == null) {
                httpRequest = new BasicHttpRequest(requestLine);
            } else {
                httpRequest = new BasicHttpEntityEnclosingRequest(requestLine);
                ((HttpEntityEnclosingRequest) httpRequest).setEntity(httpEntity);

                // get the content-type from the entity and set the header
                ContentType contentType = ContentType.get(httpEntity);
                httpRequest.setHeader("content-type", contentType.toString());
            }
        } catch (TranslationException e) {
            e.printStackTrace();
        }

        //set default headers
        Header[] headers = HttpTranslator.getHttpHeaders(request.getOptions().asSortedList());
        for (Header header : headers) {
            httpRequest.addHeader(header);
        }

        //add oAuth Header

        final Pattern HEADER_PATTERN = Pattern.compile("[H|h]eader\\{(.*?)\\}");
        final String HEADER_TITLE = "Header";
        Scanner scanner = new Scanner(payload);
        String headerLine;
        while (scanner.hasNext())//can be used to set other parameters in payload [only header atm]
        {
            if ((headerLine = scanner.findInLine(HEADER_PATTERN)) != null) {
                headerLine = headerLine.substring(HEADER_TITLE.length() + 1, headerLine.length() - 1); //trim Header{ & }
                Scanner headerScanner = new Scanner(headerLine);
                String attribute = null;
                while (headerScanner.findWithinHorizon(LinkFormat.DELIMITER, 1) == null && (attribute = headerScanner.findInLine(LinkFormat.WORD)) != null) {
                    if (headerScanner.findWithinHorizon("=", 0) != null) {
                        String value = null;
                        if ((value = headerScanner.findInLine(LinkFormat.QUOTED_STRING)) != null) {
                            value = value.substring(1, value.length() - 1); // trim " "
                        } else {
                            value = headerScanner.next();

                        }

                        httpRequest.addHeader(setHeader(attribute, value));

                    }
                }
                break; //as no other parameters to set
            }


        }
        Response coapResponse = null;
        try {
            HttpResponse httpResponse = NodeResource.HTTP_CLIENT.execute(httpHost, httpRequest);
            coapResponse = HttpTranslator.getCoapResponse(httpResponse, request);
        } catch (TranslationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        exchange.respond(coapResponse);
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        Request request = exchange.advanced().getRequest();
        URL proxyUri = null;
        HttpRequest httpRequest = null;

        //set http URI by removing 'rd' part in the reousrce path
        try {

            if (request.getOptions().getProxyUri() == null) {
                proxyUri = new URL(this.getParentNode().getContext() + this.getPath().substring(this.getParentNode().getParent().getName().length() + 1));
            } else
                proxyUri = new URL(request.getOptions().getProxyUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //create http request
        try {
            httpRequest = HttpTranslator.getHttpRequest(request);
        } catch (TranslationException e) {
            e.printStackTrace();
        }


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
