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


@Log4j2
public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client is connected: " + ctx.name());
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        System.out.println(abstractMessage.getClass().getSimpleName());
        if (abstractMessage instanceof ServiceMessage) {
            ServiceMessage serviceMessage = (ServiceMessage) abstractMessage;
            String msg = serviceMessage.getMessage();
            if (msg.startsWith("/")) {
                System.out.println(msg);
                String[] serviceCommand = msg.split(" ");
                if (serviceCommand[0].equals("/authorize")) {
                    if (authorize(serviceCommand[1], serviceCommand[2])) {
                        System.out.println(ctx.name() + ": [authorize OK]");
                        sendServiceMessage("/authorize OK", ctx);
                    } else {
                        System.out.println(ctx.name() + ": [authorize BAD]");
                        sendServiceMessage("/authorize BAD", ctx);
                    }
                }
            }
        }
    }

    private void sendServiceMessage(String message, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(ServiceMessage.builder()
                .message(message)
                .build());
    }

    private void uploadFile(ChannelHandlerContext ctx, String msg) throws IOException {
        RandomAccessFile file = null;
        long length = -1;
        try {
            file = new RandomAccessFile(msg, "r");
            length = file.length();
        } catch (Exception e) {
            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
            return;
        } finally {
            if (length < 0 && file != null) {
                file.close();
            }
        }
        ctx.write("OK: " + file.length() + '\n');
        ctx.write(new DefaultFileRegion(file.getChannel(), 0, length));
        ctx.writeAndFlush("\n");
    }


    private boolean authorize(String name, String password) {
        if (name.equals("1") && password.equals("1")) {
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



