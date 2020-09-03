import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;


@Log4j2
public class FileHandler extends SimpleChannelInboundHandler<String> {
    EventExecutor executor;
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.startsWith("/")) {
            System.out.println(msg);
            String[] serviceCommand = msg.split(" ");
            if (serviceCommand[0].equals("/authorize")) {
                if (authorize(serviceCommand[1], serviceCommand[2])) {
                    ctx.writeAndFlush("/authorize OK");
                } else {
                    ctx.writeAndFlush("/authorize BAD");
                }
            }
            if (serviceCommand[0].equals("/upload")) {
              uploadFile(ctx, msg);
            }
        }
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



