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

    @NotEmpty
    private String newUserBaseDN;

    @NotEmpty
    private String managerDN;

    @NotEmpty
    private String managerPassword;

    @NotEmpty
    private String ldapServer;

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

    @JsonProperty
    public String getNewUserBaseDN() {
        return newUserBaseDN;
    }

    @JsonProperty
    public void setNewUserBaseDN(String newUserBaseDN) {
        this.newUserBaseDN = newUserBaseDN;
    }

    @JsonProperty
    public String getManagerDN() {
        return managerDN;
    }

    @JsonProperty
    public void setManagerDN(String managerDN) {
        this.managerDN = managerDN;
    }

    @JsonProperty
    public String getManagerPassword() {
        return managerPassword;
    }

    @JsonProperty
    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    @JsonProperty
    public String getLdapServer() {
        return ldapServer;
    }

    @JsonProperty
    public void setLdapServer(String ldapServer) {
        this.ldapServer = ldapServer;
    }


}
