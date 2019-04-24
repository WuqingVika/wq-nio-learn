package com.wq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端线程类
 * 专门接收服务器端响应信息
 */
public class NioClientHandler implements Runnable {

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                //获取可用channel数量
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                //获取可用的channel集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();//SelectionKey的实例

                    iterator.remove();//移除Set中的当前SelectionKey


                    //7。根据就绪状态调用处理业务逻辑
                    //可读事件
                    if (selectionKey.isReadable()) {
                        readHandler(selectionKey, selector);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        //要从selectionKey中获取已经就绪的channel
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        //创建buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //循环读取服务端响应信息
        String response = "";
        while (channel.read(byteBuffer) > 0) {
            /**
             * 切换buffer为读模式
             *
             */
            byteBuffer.flip();
            response += Charset.forName("UTF-8").decode(byteBuffer);
            //把channel再次注册到selector上
            channel.register(selector, SelectionKey.OP_READ);

            if (response.length() > 0) {
                //将服务器端响应信息打印出来
                System.out.println(response);
            }

        }
        //将channel再次注册到selector上，监听他的可读事件
        //将客户端 发送的请求信息广播给其他客户端
    }
}
