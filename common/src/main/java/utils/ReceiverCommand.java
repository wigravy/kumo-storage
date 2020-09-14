package utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class ReceiverCommand {
    public enum State {
        IDLE, COMMAND_LENGTH, COMMAND
    }

    private State currentState = State.IDLE;
    private int commandLength;
    private byte command;
    private byte[] cmd;

    public void receive(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) {

    }
}
