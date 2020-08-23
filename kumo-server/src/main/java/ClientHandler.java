import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        log.info("Client connected: " + ctx.name());
        System.out.println("Client connected: " + ctx.name());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        while (buf.readableBytes() > 0) {
//            log.info((char) buf.readByte());
            System.out.print((char) buf.readByte());
        }
        buf.release();
    }


    //Если в процессе работы с клиентом кидается исключение, то мы закрываем с ним соеденение.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        log.error(cause.getMessage() + ". Client was disconnected.");
        ctx.close();
    }
}
