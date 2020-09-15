package com.wigravy.kumoStorage.server.main;


import com.wigravy.kumoStorage.common.utils.FileInfo;
import com.wigravy.kumoStorage.common.utils.FileService;
import com.wigravy.kumoStorage.common.utils.ListSignalBytes;
import com.wigravy.kumoStorage.common.utils.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Log4j2
public class MainHandler extends SimpleChannelInboundHandler<Object> {
    private State currentState = State.IDLE;
    private long fileLength = 0L;
    private int filenameLength = 0;
    private int commandLength = 0;
    private StringBuilder stringBuilder = new StringBuilder();
    private BufferedOutputStream out;
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
                stringBuilder = new StringBuilder();
                while (buf.readableBytes() > 0 || commandLength != 0) {
                    commandLength--;
                    stringBuilder.append((char) buf.readByte());
                }
                System.out.println(stringBuilder);
                currentState = State.COMMAND_DO;
            }
        }
        if (currentState == State.COMMAND_DO) {
            String[] command = stringBuilder.toString().split(" ");
            System.out.println(ctx.channel().isOpen());
            if (command[0].equals("/authorization")) {
                System.out.println("OK 2");

//                        currentPath = Path.of("storage", "wigravy");
                FileService.sendCommand(ctx.channel(), "/authorization OK");
                currentState = State.FILE_LIST;
//                        currentPath = CommandService.authorization(command[1], command[2], ctx.channel());
            } else if (command[0].equals("/download")) {
                CommandService.downloadFile(ctx.channel(), currentPath.resolve(command[1]));
            } else if (command[0].equals("/enterToDirectory")) {
                if (CommandService.enterToDirectory(currentPath, command[1])) {
                    currentState = State.FILE_LIST;
                }
            } else {
                throw new RuntimeException("Unknown command: " + stringBuilder.toString());
            }

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
            stringBuilder = new StringBuilder();
            while (buf.readableBytes() >= filenameLength) {
                byte[] filename = new byte[filenameLength];
                buf.readBytes(filename);
                stringBuilder.append(Arrays.toString(filename));
                out = new BufferedOutputStream(new FileOutputStream(currentPath + stringBuilder.toString()));
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
        /*
         **      Передача списка файлов
         */

        if (currentState == State.FILE_LIST) {
            List<FileInfo> serverFiles = Files.list(currentPath)
                    .map(FileInfo::new)
                    .collect(Collectors.toList());
            stringBuilder.setLength(0);
            stringBuilder.trimToSize();
            for (FileInfo fileInfo : serverFiles) {
                stringBuilder.append(String.format("%s,%d,%s,%s\n", fileInfo.getFileName(), fileInfo.getSize(), fileInfo.getFileType(), fileInfo.getLastModified()));
            }
            FileService.sendCommand(ctx.channel(), "/FileList " + stringBuilder.toString());
            currentState = State.IDLE;
        }
    }




    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel is close");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("channel error");
        cause.printStackTrace();
        ctx.close();
    }
}



