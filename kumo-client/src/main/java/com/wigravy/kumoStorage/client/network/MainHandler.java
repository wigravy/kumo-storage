package com.wigravy.kumoStorage.client.network;


import com.wigravy.kumoStorage.common.utils.ServiceMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import com.wigravy.kumoStorage.common.utils.ListSignalBytes;
import com.wigravy.kumoStorage.common.utils.State;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


@Log4j2
public class MainHandler extends SimpleChannelInboundHandler<Object> {
    private State currentState = State.IDLE;
    private long fileSize = 0L;
    private long receivedFileSize = 0L;
    private int filenameLength = 0;
    private int commandLength = 0;
    private StringBuilder stringBuilder;
    private BufferedOutputStream out;
    @Setter
    private ServiceMessage callback;
    @Setter
    private Path currentPath;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
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
                } else {
                    currentState = State.IDLE;
                    log.error("Unknown byte command arrived.");
                    throw new IllegalArgumentException("Unknown byte command arrived: " + readByte);
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
                stringBuilder = new StringBuilder();
                while (buf.readableBytes() > 0 && commandLength != 0) {
                    commandLength--;
                    stringBuilder.append((char) buf.readByte());
                }
                currentState = State.COMMAND_DO;
            }

            if (currentState == State.COMMAND_DO) {
                String[] command = stringBuilder.toString().split("\n");
                switch (command[0]) {
                    case "/authorization":
                        callback.callback(command[1]);
                        currentState = State.IDLE;
                        break;
                    case "/FileList":
                        callback.callback(stringBuilder.toString());
                        currentState = State.IDLE;
                        break;
                    default:
                        // TODO: сделать своё исключение
                        currentState = State.IDLE;
                        throw new IllegalArgumentException("Unknown command: " + stringBuilder.toString());
                }
            }


            /*
             **      Стадия получения файла
             */
            if (currentState == State.FILE_NAME_LENGTH) {
                receivedFileSize = 0L;
                fileSize = 0L;
                if (buf.readableBytes() >= 4) {
                    filenameLength = buf.readInt();
                    currentState = State.NAME;
                    log.info(String.format("File transaction: Get file name length (%s).", filenameLength));
                }
            }


            if (currentState == State.NAME) {
                while (buf.readableBytes() >= filenameLength) {
                    byte[] filenameBytes = new byte[filenameLength];
                    buf.readBytes(filenameBytes);
                    String filename = new String(filenameBytes, StandardCharsets.UTF_8);
                    File file = new File(currentPath.toString() + "/" + filename);
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    currentState = State.FILE_SIZE;
                    log.info(String.format("File transaction: Get file name (%s).", filename));
                }
            }

            if (currentState == State.FILE_SIZE) {
                if (buf.readableBytes() >= 8) {
                    fileSize = buf.readLong();
                    currentState = State.FILE;
                    log.info(String.format("File transaction: Get file size (%s).", fileSize));
                }
            }

            if (currentState == State.FILE) {
                try {
                    if (fileSize != 0) {
                        while (buf.readableBytes() > 0) {
                            out.write(buf.readByte());
                            receivedFileSize++;
                            if (fileSize == receivedFileSize) {
                                callback.callback("/updateClientFileList");
                                log.info("File transaction end.");
                                currentState = State.IDLE;
                                out.close();
                                break;
                            }
                        }
                    } else {
                        callback.callback("/updateClientFileList");
                        currentState = State.IDLE;
                        out.close();
                    }
                } catch (Exception e) {
                    callback.callback("/updateClientFileList");
                    log.error(String.format("File transaction ERROR: [%s]: %s", e.getClass().getSimpleName(), e.getMessage()));
                    currentState = State.IDLE;
                    out.close();
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}



