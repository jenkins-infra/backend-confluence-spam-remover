package org.jenkinsci.backend.confluence.pageremover.webapp;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jenkinsci.backend.ldap.AccountServer;

import java.io.IOException;

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
    public void run(PageRemoverConfiguration configuration, Environment environment) throws IOException {

        final PageRemoverResource resource = new PageRemoverResource(
                configuration.getSpace(),
                configuration.getMailRecipient(),
                configuration.getSmtpServer(),
                new AccountServer(configuration.getLdapServerConfiguration()),
                configuration.getLanguageBlockingConfiguration().getLanguages(),
                configuration.getLanguageBlockingConfiguration().getProbability()
        );

        environment.jersey().register(resource);
    }
}
