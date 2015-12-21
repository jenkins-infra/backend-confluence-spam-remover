package org.jenkinsci.backend.ldap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Represents our account server.
 *
 * @author Kohsuke Kawaguchi
 */
public class AccountServer {
    private final Config config;

    public AccountServer(Config config) {
        this.config = config;
    }

    public LdapContext connect() throws NamingException {
        return connect(config.managerDN(), config.managerPassword());
    }

    public LdapContext connect(String dn, String password) throws NamingException {
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, config.server());
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialLdapContext(env, null);
    }

    /**
     * Deletes an account by its user ID like 'kohsuke'.
     */
    public void delete(String cn) throws NamingException {
        final DirContext con = connect();
        try {
            List<SearchResult> all = Collections.list(con.search(config.newUserBaseDN(), "cn={0}", new Object[]{cn}, new SearchControls()));
            if (all.isEmpty())
                throw new IllegalArgumentException("No such user: "+cn);
            if (all.size()>1)
                throw new IllegalArgumentException("Too many users matching "+cn);

            con.destroySubcontext(all.get(0).getNameInNamespace());
        } finally {
            con.close();
        }
    }

//    public static void main(String[] args) throws Exception {
//        AccountServer app = new AccountServer(ConfigurationLoader.from(new File("./config.properties")).as(Config.class));
//        app.delete("kktest11");
//    }
}
