package org.jenkinsci.backend.confluence.pageremover.webapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PageRemoverConfiguration extends Configuration {

    @JsonProperty
    @NotEmpty
    private String space;

    @JsonProperty
    @NotEmpty
    private String mailRecipient;

    @JsonProperty
    @NotEmpty
    private String smtpServer;

    @JsonProperty("ldapServer")
    @Valid
    @NotNull
    private LdapServerConfiguration ldapServerConfiguration = new LdapServerConfiguration();

    @JsonProperty("languageBlocking")
    @NotNull
    @Valid
    private LanguageBlockingConfiguration languageBlockingConfiguration = new LanguageBlockingConfiguration();

    public String getSpace() {
        return space;
    }

    public String getMailRecipient() {
        return mailRecipient;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public LdapServerConfiguration getLdapServerConfiguration() {
        return ldapServerConfiguration;
    }

    public LanguageBlockingConfiguration getLanguageBlockingConfiguration() {
        return languageBlockingConfiguration;
    }
}
