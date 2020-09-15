package com.wigravy.kumoStorage.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

public class Network {
    private final String HOST;
    private final int PORT;
    @Getter
    private SocketChannel channel;
    @Getter
    private MainHandler mainHandler = new MainHandler();

    public Network(String host, int port) {
        this.HOST = host;
        this.PORT = port;
        Thread thread = new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline()
                                        .addLast(mainHandler);
                            }
                        });
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        });
        thread.start();
    }

    public void close() {
        channel.close();
    }
}
