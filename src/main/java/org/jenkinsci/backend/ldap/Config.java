package org.jenkinsci.backend.ldap;

/**
 * Configuration to this app.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Config {
    /**
     * string like "ou=people,dc=acme,dc=com" that decides where new users are created.
     */
    String newUserBaseDN();

    /**
     * Coordinates to access LDAP.
     */
    String managerDN();
    String managerPassword();
    String server();
}
