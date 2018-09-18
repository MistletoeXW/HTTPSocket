package webservices;

import OV.User;
import Tool.AuthTool;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: httpSocket
 * @description: HTTP服务器端
 * @author: xw
 * @create: 2018-09-16 11:40
 */
public class HttpService extends Thread{
    private Socket socketclient;
    private String encoding= "UTF-8";
    private String home="www";
    private PrintStream out;

    HttpService(Socket socket){
        this.socketclient = socket;
        try{
            out = new PrintStream(socket.getOutputStream(), true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try{
            InputStream inputStream = socketclient.getInputStream();
            System.out.println("[Client] http请求内容如下");
            //System.out.println("-----------http内容开始-----------");
            // 读取第一行, 请求地址
            String line = getline(inputStream, 0);
            System.out.print(line);
            // 获取请求方法, GET 或者 POST
            String method = new StringTokenizer(line).nextElement().toString();
            String resourcepath = line.substring(line.indexOf('/'), line.lastIndexOf('/') - 5);
            //获得请求的资源的地址
            resourcepath = URLDecoder.decode(resourcepath, encoding);//解码URL 地址
            //System.out.println(resourcepath+"   "+method);
            int contentlength=0;
            do {
                line = getline(inputStream, 0);
                System.out.print(line);
                //如果有Content-Length消息头时取出
                if (line.startsWith("Content-Length")) {
                    contentlength = Integer.parseInt(line.split(":")[1]
                            .trim());
                }
                //回车换行表示请求头结束
            } while (!line.equals("\r\n"));
            //System.out.println("-----------http内容结束-----------\n\n");
            if("GET".equalsIgnoreCase(method)){
                //判断定位于文件夹或文件
                if (resourcepath.endsWith("/")) {
                    respondGET("/index.html", socketclient);
                }
                else {
                    respondGET(resourcepath,socketclient);
                }

            }
            else if("POST".equalsIgnoreCase(method)){
                String str=getline(inputStream,contentlength);
                System.out.println("客户端发送数据");
                respondPOST(str);
            }
            inputStream.close();
            try {
                socketclient.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("[Server] 完成http请求");


        } catch(IOException e) {
            e.printStackTrace();
        }

}

    public void respondGET(String filename, Socket socket){
        try{
            String type = "text/plain";
            Path path = Paths.get(filename);
                try{
                    type = Files.probeContentType(path);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.print("[Server] 返回GET请求文件 => "+ filename);
            File fileToSend = new File(home+filename);
            if (fileToSend.exists() && !fileToSend.isDirectory()) {
                out.println("HTTP/1.0 200 OK");//返回应答消息
                out.println("Content-Type: "+type+";charset=" + encoding);
                out.println("Content-Length: " + fileToSend.length());// 返回内容字节数
                out.println();// 根据 HTTP 协议, 空行将结束头信息

                FileInputStream fis = new FileInputStream(fileToSend);
                byte[] tmpByteArr = new byte[100];
                while (fis.available() > 0) {
                    int readCount = fis.read(tmpByteArr);
                    out.write(tmpByteArr, 0, readCount);
                }
                //文件下载完后关闭socket流
                fis.close();
            }
            else {
                fileToSend = new File(home+"/html/error/index.html");
                out.println("HTTP/1.0 200 OK");//返回应答消息
                out.println("Content-Type: "+"text/html"+";charset=" + encoding);
                out.println("Content-Length: " + fileToSend.length());// 返回内容字节数
                out.println();// 根据 HTTP 协议, 空行将结束头信息

                FileInputStream fis = new FileInputStream(fileToSend);
                byte[] tmpByteArr = new byte[100];
                while (fis.available() > 0) {
                    int readCount = fis.read(tmpByteArr);
                    out.write(tmpByteArr, 0, readCount);
                }
                //文件下载完后关闭socket流
                fis.close();
            }
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
            out.println("HTTP/1.0 404 not found");//返回应答消息
            out.println();
        }
    }

    private String getline(InputStream is, int contentLe) throws IOException {
        ArrayList<Byte> lineByteList = new ArrayList<Byte>();
        byte readByte;
        int total = 0;
        if (contentLe != 0) {
            do {
                readByte = (byte) is.read();
                lineByteList.add(Byte.valueOf(readByte));
                total++;
            } while (total < contentLe);//消息体读还未读完
        } else {
            do {
                readByte = (byte) is.read();
                lineByteList.add(Byte.valueOf(readByte));
            } while (readByte != 10);
        }
        byte[] tmpByteArr = new byte[lineByteList.size()];
        for (int i = 0; i < lineByteList.size(); i++) {
            tmpByteArr[i] = ((Byte) lineByteList.get(i)).byteValue();
        }
        lineByteList.clear();
        String tmpStr = new String(tmpByteArr, encoding);
        return tmpStr;
    }

    public void respondPOST(String content) {
        System.out.println("[Server] 回应POST请求\n ");
        try {

            out.println("HTTP/1.1 200 OK");
            out.println("charset:UTF-8");
            out.println("Content-Type:text/plain");
            out.println();

            String pattern = "userId=(\\d*)&password=(.*)";

            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(content);
            if (m.find()) {
                String userId = m.group(1);
                String password = m.group(2);
                if(AuthTool.getAuth(userId,password)){
                    User user = AuthTool.getInfo(userId);
                    out.print(user.getUserId()+" "+user.getUserName()+" "+user.getDuty()+" "+user.getDepartment());
                }
                out.flush();
                out.close();
            }
        }catch (Exception e) {
                out.println("HTTP/1.0 404 not found");//返回应答消息
                out.println();
            }

        }

}
