package webservices;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.StringTokenizer;

/**
 * @program: httpSocket
 * @description: Https
 * @author: xw
 * @create: 2018-09-17 21:52
 */
public class HttpsService extends Thread{

    public static void main(String[] args) throws Exception {
        String host = null;
        int port = -1;
        String path = null;

        try {

            host = "github.com";
//            host = "blog.csdn.net";
//            host = "www.baidu.com";
            port = 443;
            path = "/";
        } catch (IllegalArgumentException e) {
            System.out.println("USAGE: java SSLSocketClientWithClientAuth host port requestedfilepath");
            System.exit(-1);
        }

        try {

            /*
             * 使用jdk自带的keystore，里面包含官方的证书，主流网站都有
             */
            SSLSocketFactory factory = null;
            try {
                SSLContext ctx;
                KeyManagerFactory kmf;
                KeyStore ks;
                char[] passphrase = "changeit".toCharArray();

                ctx = SSLContext.getInstance("TLS");
                kmf = KeyManagerFactory.getInstance("SunX509");
                ks = KeyStore.getInstance("JKS");

                ks.load(new FileInputStream("/usr/lib/jvm/java-8-openjdk-amd64" + "/jre/lib/security/cacerts"), passphrase);

                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);

                factory = ctx.getSocketFactory();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);


            /*
             * send http request
             *
             * See SSLSocketClient.java for more information about why
             * there is a forced handshake here when using PrintWriters.
             */
            System.out.println("start https request:" + host + " " + port + " " + path);
            socket.startHandshake();

            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.println("GET " + path + " HTTP/1.1");
            out.println("Host: " + host);
            out.println();
            out.flush();

            /*
             * Make sure there were no surprises
             */
            if (out.checkError())
                System.out.println(
                        "SSLSocketClient: java.io.PrintWriter error");

            /* read response */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            String inputLine;

            //最好没有空行会阻塞在这里
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);

            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
