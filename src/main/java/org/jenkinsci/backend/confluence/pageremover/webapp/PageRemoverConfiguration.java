/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.backend.confluence.pageremover.webapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class PageRemoverConfiguration extends Configuration {

    @NotEmpty
    private String space;

    @NotEmpty
    private String mailRecipient;

    @NotEmpty
    private String smtpServer;

    @JsonProperty
    public String getSpace() {
        return space;
    }

    @JsonProperty
    public void setSpace(String space) {
        this.space = space;
    }

    @JsonProperty
    public String getMailRecipient() {
        return mailRecipient;
    }

    @JsonProperty
    public void setMailRecipient(String mailRecipient) {
        this.mailRecipient = mailRecipient;
    }

    @JsonProperty
    public String getSmtpServer() {
        return smtpServer;
    }

    @JsonProperty
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }


}
