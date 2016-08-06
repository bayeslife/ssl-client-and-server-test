package bayeslife;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.OutboundHttpRequest.outboundRequest;

import org.mockserver.model.Delay;
import org.mockserver.model.Header;

import java.util.concurrent.TimeUnit;


import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public class HttpsClientTest {

    private ClientAndProxy proxy;
    private ClientAndServer mockServer;

    @Before
    public void setup() throws Exception
    {
            mockServer = startClientAndServer(4444);


            mockServer.when(
                    request()
                            .withMethod("GET")
                            .withPath("/api/.*")
            )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withHeaders(
                                            new Header("Content-Type", "application/json; charset=utf-8"),
                                            new Header("Cache-Control", "public, max-age=86400")
                                    )
                                    .withBody("{ message: 'success' }")

                    );

            KeyStore keyStore = KeyStore.getInstance("JKS");
            String fileName = "trust.jks"; // cerrtification file path
            System.setProperty("javax.net.ssl.trustStore", fileName);
            System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }


    @After
    public void teardown()  {
        mockServer.stop();
    }

    @Test
    public void testIt(){

        //String https_url = "https://www.google.com/";
        String https_url = "https://localhost:1080/api/foo";
        URL url;
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

            //dumpl all cert info
            print_https_cert(con);

            //dump all the content
            print_content(con);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void print_https_cert(HttpsURLConnection con){

        if(con!=null){

            try {

                System.out.println("Response Code : " + con.getResponseCode());
                System.out.println("Cipher Suite : " + con.getCipherSuite());
                System.out.println("\n");

                Certificate[] certs = con.getServerCertificates();
                for(Certificate cert : certs){
                    System.out.println("Cert Type : " + cert.getType());
                    System.out.println("Cert Hash Code : " + cert.hashCode());
                    System.out.println("Cert Public Key Algorithm : "
                            + cert.getPublicKey().getAlgorithm());
                    System.out.println("Cert Public Key Format : "
                            + cert.getPublicKey().getFormat());
                    System.out.println("\n");
                }

            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

        }

    }

    private void print_content(HttpsURLConnection con){
        if(con!=null){

            try {

                System.out.println("****** Content of the URL ********");
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(con.getInputStream()));

                String input;

                while ((input = br.readLine()) != null){
                    System.out.println(input);
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}