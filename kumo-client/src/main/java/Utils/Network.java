package Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import java.io.*;

public class Network {
    private SocketChannel channel;
    private final String HOST;
    private final int PORT;

    public Network(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    public void connectToServer() {
        Thread thread = new Thread(() -> {
            EventLoopGroup clientGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(clientGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline()
                                .addLast(
                                        new StringEncoder(CharsetUtil.UTF_8),
                                        new LineBasedFrameDecoder(8192),
                                        new StringDecoder(CharsetUtil.UTF_8),
                                        new ChunkedWriteHandler(),
                                        new ClientHandler());
                            }
                        });
                ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                clientGroup.shutdownGracefully();
            }
        });
        thread.start();
    }

    public void authorize(String username, String password) {
        channel.writeAndFlush("/authorize " + username + " " + password);
    }

    public void sendFile(File file) {

    }
}
