package com.wq;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Nio客户端
 */
public class NioClient {

    public void start(String nickname) throws IOException {
        //连接服务器端
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));
        //接收服务端 响应
        /**
         * 新开线程，专门负责来接收服务端的响应数据
         * selector ，socketChannel,注册
         */

        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();

        //向服务器端发送数据
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String req = scanner.nextLine();
            if (req != null && req.length() > 0) {
                socketChannel.write(Charset.forName("UTF-8").encode(nickname+"---发送消息:"+req));
            }
        }
    }


    public static void main(String[] args) throws IOException {
        //new NioClient().start();
    }
}
