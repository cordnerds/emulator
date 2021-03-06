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
package org.wso2.gw.emulator.sampletcp.dsl.dao;

import org.wso2.gw.emulator.core.AbstractBuilderContext;
import org.wso2.gw.emulator.core.AbstractProtocolEmulator;
import org.wso2.gw.emulator.sampletcp.TCPProtocolEmulator;
import org.wso2.gw.emulator.sampletcp.dsl.IncomingMessage;
import org.wso2.gw.emulator.sampletcp.dsl.OutgoingMessage;

public class TcpConsumerContext extends AbstractBuilderContext {

    private TCPProtocolEmulator tcpProtocolEmulator;
    private IncomingMessage incoming;
    private OutgoingMessage outgoingMessage;

    public TcpConsumerContext(TCPProtocolEmulator tcpProtocolEmulator) {
        this.tcpProtocolEmulator = tcpProtocolEmulator;
    }

    public TcpConsumerContext host(String host) {
        super.host(host);
        return this;
    }

    public TcpConsumerContext port(int port) {
        super.port(port);
        return this;
    }

    public TcpConsumerContext when(IncomingMessage incoming) {
        this.incoming = incoming;
        return this;
    }

    public TcpConsumerContext respond(OutgoingMessage outgoingMessage) {
        this.outgoingMessage = outgoingMessage;
        return this;
    }

    public IncomingMessage getIncoming() {
        return incoming;
    }

    public OutgoingMessage getOutgoingMessage() {
        return outgoingMessage;
    }

    public AbstractProtocolEmulator operations() {
       // return tcpProtocolEmulator;
        return null;
    }
}
