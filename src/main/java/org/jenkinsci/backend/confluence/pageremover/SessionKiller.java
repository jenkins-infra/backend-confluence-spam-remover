package org.jenkinsci.backend.confluence.pageremover;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SessionKiller {

    public void kill(String username) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
        Properties props = new Properties();
        File credential = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org.spambot");
        if (!credential.exists())
            credential = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org");
        if (!credential.exists())
            throw new IOException("You need to have userName and password in " + credential);
        props.load(new FileInputStream(credential));

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope("wiki.jenkins-ci.org", 443), new UsernamePasswordCredentials(props.getProperty("userName"), props.getProperty("password")));

        CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

        try {
            URIBuilder baseUri = new URIBuilder()
                    .setScheme("https")
                    .setHost("wiki.jenkins-ci.org")
                    .setPath("/tomcat-manager/html/sessions")
                    .setParameter("path", "/");
            URI uri = baseUri.build();
            HttpGet httpGet = new HttpGet(uri);
            CloseableHttpResponse response = client.execute(httpGet);
            try {
                String body = EntityUtils.toString(response.getEntity());
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(new ByteArrayInputStream(body.getBytes()));
                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList nodes = (NodeList) xPath.evaluate("//html/body/form[@id='sessionsForm']/fieldset/table/tbody/tr/td[contains(text(), '" + username + "')]/..", document.getDocumentElement(), XPathConstants.NODESET);
                List<String> sessionIds = new ArrayList<String>();
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Element e = (Element) nodes.item(i);
                    NodeList children = (NodeList) xPath.evaluate("./td/input[@name='sessionIds']", e, XPathConstants.NODESET);
                    sessionIds.add(children.item(0).getAttributes().getNamedItem("value").getNodeValue());
                }
                for (String sessionId : sessionIds) {
                    baseUri.addParameter("sessionIds", sessionId);
                }
                URI invalidate = baseUri
                        .setParameter("action", "invalidateSessions")
                        .setParameter("invalidate", "Invalidate selected Sessions").build();
                HttpPost httpPost = new HttpPost(invalidate);
                client.execute(httpPost);
            } catch (XPathExpressionException e) {
                response.close();
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) {
        SessionKiller sessionKiller = new SessionKiller();
        try {
            sessionKiller.kill("");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

}
