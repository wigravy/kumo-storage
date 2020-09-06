import Utils.Messages.FileListMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.log4j.Log4j2;

import Utils.Messages.AbstractMessage;
import Utils.Messages.ServiceMessage;
import Utils.Messages.FileMessage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;


@Log4j2
public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client is connected: " + ctx.channel().remoteAddress());
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client is disconnected: " + ctx.channel().remoteAddress());
        channels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        System.out.print(abstractMessage.getClass().getSimpleName() + " has arrived from client: [" + ctx.channel().remoteAddress() + "]: ");

        if (abstractMessage instanceof ServiceMessage) {
            ServiceMessage serviceMessage = (ServiceMessage) abstractMessage;
            String msg = serviceMessage.getMessage();
            if (msg.startsWith("/")) {
                System.out.println(msg);
                String[] serviceCommand = msg.split(" ");
                if (serviceCommand[0].equals("/authorize")) {
                    if (authorize(serviceCommand[1], serviceCommand[2])) {
                        System.out.println(ctx.channel().remoteAddress() + ": [authorize OK]");
                        sendServiceMessage("/authorize OK", ctx);
                    } else {
                        System.out.println(ctx.channel().remoteAddress() + ": [authorize BAD]");
                        sendServiceMessage("/authorize BAD", ctx);
                    }
                } else if (serviceCommand[0].equals("/updateFileList")) {
                    Path path = Paths.get("storage/wigravy/" + serviceCommand[1]);
                    updateFileList(ctx, path);
                }
            }
        }
    }

    private void sendServiceMessage(String message, ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(ServiceMessage.builder()
                .message(message)
                .build());
    }

    private void updateFileList(ChannelHandlerContext ctx, Path path) throws IOException {
        FileListMessage fileListMessage = new FileListMessage();
        fileListMessage.updateFileList(path);
        ctx.writeAndFlush(fileListMessage);
    }


    private boolean authorize(String name, String password) {
        if (name.equals("wigravy") && password.equals("1")) {
            return true;
        }
        return false;

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
    }
}



