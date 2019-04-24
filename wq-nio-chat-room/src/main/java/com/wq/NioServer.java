package com.wq;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * Nio服务器端
 */
public class NioServer {
    /**
     * 启动
     */
    public void start() throws IOException {

        //1.创建Selector
        Selector selector = Selector.open();

        //2.通过ServerSocketChannel创建channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3.为通道绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8000));
        //4。设置channel为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //5.将channel注册到selector上。监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功");
        //6。循环等待新接入的连接
        for (; ; ) {
            //获取可用channel数量
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                //判断是为了防止空轮询
                continue;
            }
            //获取可用的channel集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();//SelectionKey的实例

                iterator.remove();//移除Set中的当前SelectionKey


                //7。根据就绪状态调用处理业务逻辑
                //接入事件
                if (selectionKey.isAcceptable()) {
                    acceptHandler(serverSocketChannel, selector);
                }
                //可读事件
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey, selector);
                }
            }

        }


    }

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        //创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        //将socketChannel设置为非阻塞工作模式
        socketChannel.configureBlocking(false);
        //将channel注册到selector上，监听可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);
        //回复客户端提示信息
        socketChannel.write(Charset.forName("UTF-8").encode("您已经加入我们的聊天室，请注意聊天内容合法性！"));

    }

    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        //要从selectionKey中获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        //创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //循环读取客户端 请求信息
        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             * 切换buffer为读模式
             *
             */
            byteBuffer.flip();
            request += Charset.forName("UTF-8").decode(byteBuffer);
            //把channel再次注册到selector上
            socketChannel.register(selector, SelectionKey.OP_READ);

            if (request.length() > 0) {
                //广播给其他客户端
                System.out.println(request);
                broadCast(selector,socketChannel,request);
            }

        }
        //将channel再次注册到selector上，监听他的可读事件
        //将客户端 发送的请求信息广播给其他客户端
    }

    private void broadCast(Selector selector,SocketChannel sourceChannel,String request){
        //广播给其它客户端
        //获取所有已经接入的客户端Channel
        Set<SelectionKey> selectionKeySet = selector.keys();
        selectionKeySet.forEach(selectionKey -> {
            //循环向channel广播信息
            SelectableChannel targetChannel = selectionKey.channel();
            if(targetChannel instanceof SocketChannel && targetChannel!=sourceChannel){
                //剔除发消息的客户端
                try {
                    //将消息发送到targetChannel
                    ((SocketChannel)targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * 主方法
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
