package network;

import Utils.Messages.AbstractMessage;
import Utils.Messages.ServiceMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.io.IOException;
import java.io.RandomAccessFile;


@Log4j2
public class FileHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private Callback callback;

    public FileHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        if (callback != null) {
            System.out.println(abstractMessage.getClass().getSimpleName());
            callback.call(abstractMessage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}



