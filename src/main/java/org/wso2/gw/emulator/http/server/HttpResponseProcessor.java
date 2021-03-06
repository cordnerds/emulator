/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.gw.emulator.http.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.wso2.gw.emulator.http.dsl.consumer.HttpServerBuilderContext;
import org.wso2.gw.emulator.http.dsl.params.Cookie;
import org.wso2.gw.emulator.http.dsl.params.Header;
import org.wso2.gw.emulator.http.dsl.consumer.IncomingMessage;
import org.wso2.gw.emulator.http.dsl.consumer.OutgoingMessage;

import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;


public class HttpResponseProcessor {
    private HttpServerBuilderContext consumerContext;

    public HttpResponseProcessor(HttpServerBuilderContext consumerContext) {
        this.consumerContext = consumerContext;
    }

    public void process(HttpRequestContext requestContext, ChannelHandlerContext ctx) {
        OutgoingMessage outgoing = getMatchResource(requestContext);
        if (outgoing == null) {
            if (!write404NotFoundResponse(requestContext, ctx)) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            if (!writeResponse(requestContext, outgoing, ctx)) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private boolean writeResponse(HttpRequestContext requestContext, OutgoingMessage outgoing, ChannelHandlerContext
            ctx) {
        boolean keepAlive = requestContext.isKeepAlive();

        HttpVersion httpVersion = consumerContext.getHttpVersion();
        HttpResponseStatus httpResponseStatus = outgoing.getStatusCode();
        FullHttpResponse response = new DefaultFullHttpResponse(
                httpVersion, httpResponseStatus,
                Unpooled.copiedBuffer(outgoing.getBody(), CharsetUtil.UTF_8));

        populateHttpHeaders(response, outgoing);
        populateCookies(response, outgoing);

        if (!keepAlive) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(response);
        }
        return keepAlive;
    }

    private void populateHttpHeaders(FullHttpResponse response, OutgoingMessage outgoing) {
        if (outgoing.getHeaders() != null) {
            for (Header header : outgoing.getHeaders()) {
                response.headers().add(header.getName(), header.getValue());
            }
        }
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
    }

    private void populateCookies(FullHttpResponse response, OutgoingMessage outgoing) {
        if (outgoing.getCookies() != null) {
            for (Cookie cookie : outgoing.getCookies()) {
                response.headers().add(HttpHeaders.Names.SET_COOKIE, ServerCookieEncoder.encode(cookie.getName(), cookie.getValue()));
            }
        }
    }

    private boolean write404NotFoundResponse(HttpRequestContext requestContext, ChannelHandlerContext ctx) {
        boolean keepAlive = requestContext.isKeepAlive();
        HttpVersion httpVersion = consumerContext.getHttpVersion();
        FullHttpResponse response = new DefaultFullHttpResponse(
                httpVersion, NOT_FOUND);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(response);
        }
        return keepAlive;
    }

    private OutgoingMessage getMatchResource(HttpRequestContext requestContext) {
        for (Map.Entry<IncomingMessage, OutgoingMessage> entry : consumerContext.getInOutCorrelation().entrySet()) {
            if (entry.getKey().isMatch(requestContext)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
