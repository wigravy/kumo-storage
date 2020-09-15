package com.wigravy.kumoStorage.client.network;


import com.wigravy.kumoStorage.common.utils.FileService;
import com.wigravy.kumoStorage.common.utils.ServiceMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import com.wigravy.kumoStorage.common.utils.ListSignalBytes;
import com.wigravy.kumoStorage.common.utils.State;


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;


@Log4j2
public class MainHandler extends SimpleChannelInboundHandler<Object> {
    private State currentState = State.IDLE;
    private long fileLength = 0L;
    private int filenameLength = 0;
    private int commandLength = 0;
    private StringBuilder stringBuilder = new StringBuilder();
    private BufferedOutputStream out;
    @Setter
    private ServiceMessage authCallback;
    @Setter
    private ServiceMessage serviceCallback;



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {
            while (buf.readableBytes() > 0) {
                /*
                 **      Получаем сигнальный байт
                 */
                if (currentState == State.IDLE) {
                    byte readByte = buf.readByte();
                    if (readByte == ListSignalBytes.CMD_SIGNAL_BYTE) {
                        currentState = State.COMMAND;
                    } else if (readByte == ListSignalBytes.FILE_SIGNAL_BYTE) {
                        currentState = State.FILE_NAME_LENGTH;
                    } else if (readByte == ListSignalBytes.LIST_SIGNAL_BYTE) {

                    } else {
                        throw new RuntimeException("Unknown byte command arrived: " + readByte);
                    }
                }
                /*
                 **      Стадия получения команды
                 */
                if (currentState == State.COMMAND) {
                    if (buf.readableBytes() >= 4) {
                        commandLength = buf.readInt();
                        currentState = State.COMMAND_READ;
                    }
                }
                if (currentState == State.COMMAND_READ) {
                    stringBuilder.setLength(0);
                    stringBuilder.trimToSize();
                    while (buf.readableBytes() > 0) {
                        stringBuilder.append(buf.readByte());
                        commandLength--;
                        if (commandLength == 0) {
                            currentState = State.COMMAND_DO;
                            break;
                        }
                    }
                }
                if (currentState == State.COMMAND_DO) {
                    String[] command = stringBuilder.toString().split(" ");
                    if (command[0].startsWith("/authorization ")) {
                        authCallback.callback(command[1]);
                    } else if (command[0].startsWith("/FileList ")) {
                        FileService.createFileList(stringBuilder.toString().split(" ", 2)[1]);
                        serviceCallback.callback(command[1]);
                    } else {
                        throw new RuntimeException("Unknown command: " + stringBuilder.toString());
                    }
                    currentState = State.IDLE;
                }

                /*
                 **      Стадия получения файла
                 */
                if (currentState == State.FILE_NAME_LENGTH) {
                    if (buf.readableBytes() >= 4) {
                        filenameLength = buf.readInt();
                        currentState = State.NAME;
                    }
                }

                if (currentState == State.NAME) {
                    stringBuilder.setLength(0);
                    stringBuilder.trimToSize();
                    while (buf.readableBytes() >= filenameLength) {
                        byte[] filename = new byte[filenameLength];
                        buf.readBytes(filename);
                        stringBuilder.append(filename);
                        out = new BufferedOutputStream(new FileOutputStream(stringBuilder.toString()));
                        currentState = State.FILE_SIZE;
                    }
                }

                if (currentState == State.FILE_SIZE) {
                    if (buf.readableBytes() >= 8) {
                        fileLength = buf.readLong();
                        currentState = State.FILE;
                    }
                }

                if (currentState == State.FILE) {
                    while (buf.readableBytes() > 0) {
                        out.write(buf.readByte());
                        fileLength--;
                        if (fileLength == 0) {
                            currentState = State.IDLE;
                            out.close();
                            break;
                        }
                    }
                }
            }
        } finally {
            buf.release();
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}



