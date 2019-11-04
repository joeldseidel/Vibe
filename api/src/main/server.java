package main;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import main.data.DatabaseInteraction;
import main.handlers.*;
import main.listeners.AssignListener;
import main.listeners.CardListener;
import main.listeners.PathListener;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class server {
    /**
     * Entry point for the API
     * @param args provided string args
     */
    public static void main(String args[]){
        try {
            initServer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Init HTTP server and set context paths
     * @throws IOException io exception that may, but almost certainly will not, pop up
     */
    private static void initServer() throws Exception {
        HttpsServer server;
        //Create server and address
        server = HttpsServer.create(new InetSocketAddress(6969), 0);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        //Initialize keystore
        char[] password = "password".toCharArray();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream = DatabaseInteraction.class.getClassLoader().getResourceAsStream("main/data/testkey.jks");
        keyStore.load(inputStream, password);
        //Create key manager factory and init
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, password);
        //Create trust manager factory and init
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);
        //Create HTTPS context and parameters
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext){
            public void configure(HttpsParameters params){
                try{
                    //Init SSL context
                    SSLContext context = SSLContext.getDefault();
                    SSLEngine engine = context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());
                    //Fetch default parameters
                    SSLParameters defaultSSLParams = context.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParams);
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        startTransactionListeners();
        server = createHandlerContexts(server);
        server.setExecutor(null);
        server.start();
    }

    private static void startTransactionListeners(){
        CardListener.startListener();
        PathListener.startListener();
        AssignListener.startListener();
    }

    private static HttpsServer createHandlerContexts(HttpsServer server){
        server.createContext("/token/get", new RequestTokenHandler());
        server.createContext("/user/auth", new AuthenticateUserHandler());
        server.createContext("/user/data/get", new GetUserDataHandler());
        server.createContext("/project/artifacts/get", new GetProjectArtifactsHandler());
        server.createContext("/card/update", new CardUpdateHandler());
        server.createContext("/card/create", new CreateCardHandler());
        server.createContext("/card/get", new GetCardDetailHandler());
        server.createContext("/card/assign", new AssignCardHandler());
        server.createContext("/card/artifacts/get", new GetDrawingArtifactHandler());
        server.createContext("/card/artifact/add", new AddPathHandler());
        server.createContext("/user/register", new RegisterUserHandler());
        server.createContext("/user/projects/get", new GetUserProjectsHandler());
        server.createContext("/user/giveaccess", new GiveProjectAccessHandler());
        return server;
    }
}
