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

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class PageRemoverApplication extends Application<PageRemoverConfiguration> {
    public static void main(String[] args) throws Exception {
        new PageRemoverApplication().run(args);
    }

    @Override
    public String getName() {
        return "page-remover";
    }

    @Override
    public void initialize(Bootstrap<PageRemoverConfiguration> bootstrap) {

    }

    @Override
    public void run(PageRemoverConfiguration configuration, Environment environment) {
        final PageRemoverResource resource = new PageRemoverResource(
                configuration.getSpace(),
                configuration.getMailRecipient(),
                configuration.getSmtpServer()
        );
        environment.jersey().register(resource);
    }
}
