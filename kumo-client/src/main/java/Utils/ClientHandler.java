package Utils;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.io.RandomAccessFile;


@Log4j2
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
//        RandomAccessFile file = null;
//        long length = -1;
//        try {
//            file = new RandomAccessFile(msg, "r");
//            length = file.length();
//        } catch (Exception e) {
//            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
//            return;
//        } finally {
//            if (length < 0 && file != null) {
//                file.close();
//            }
//        }
//        ctx.write("OK: " + file.length() + '\n');
//        ctx.write(new DefaultFileRegion(file.getChannel(), 0, length));
//        ctx.writeAndFlush("\n");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
        ctx.close();
    }
}



