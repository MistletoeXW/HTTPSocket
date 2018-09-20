package webservices;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: httpSocket
 * @description: 启动服务器
 * @author: xw
 * @create: 2018-09-16 12:02
 */


public class HttpServer {
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Socket socket = null;
        try {
            ServerSocket s = new ServerSocket(8080,5);
            System.out.println("[Server] 服务器正常启动，等待连接...\n");
            while (true) {
                socket = s.accept();
                System.out.println("[Server] 建立一个客户端连接 => 源IP："+socket.getInetAddress()+ "源端口："+socket.getPort());
                new HttpService(socket).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
