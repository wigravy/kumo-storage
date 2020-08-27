import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.RandomAccessFile;


@Log4j2
public class FileHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
        if (msg.startsWith("/")) {
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

    //Если в процессе работы с клиентом кидается исключение, то мы закрываем с ним соеденение.
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



