package org.jenkinsci.backend.confluence.pageremover.webapp;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jenkinsci.backend.ldap.AccountServer;
import org.jenkinsci.backend.ldap.Config;
import org.kohsuke.stapler.config.ConfigurationLoader;

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
        final Config ldapConfig = ConfigurationLoader.from(ImmutableMap.of(
                "newUserBaseDN", configuration.getNewUserBaseDN(),
                "managerDN", configuration.getManagerDN(),
                "managerPassword", configuration.getManagerPassword(),
                "server", configuration.getLdapServer()
        )).as(Config.class);

        final PageRemoverResource resource = new PageRemoverResource(
                configuration.getSpace(),
                configuration.getMailRecipient(),
                configuration.getSmtpServer(),
                new AccountServer(ldapConfig)
        );

        environment.jersey().register(resource);
    }
}
