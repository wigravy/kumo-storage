package com.wigravy.kumoStorage.server.main;


import com.wigravy.kumoStorage.common.utils.FileInfo;
import com.wigravy.kumoStorage.common.utils.FileService;
import com.wigravy.kumoStorage.common.utils.ListSignalBytes;
import com.wigravy.kumoStorage.common.utils.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Log4j2
public class MainHandler extends SimpleChannelInboundHandler<Object> {
    private State currentState = State.IDLE;
    private long fileSize = 0L;
    private int filenameLength = 0;
    private int commandLength = 0;
    private StringBuilder stringBuilder;
    private BufferedOutputStream out;
    private Path currentPath = Path.of("storage", "wigravy");


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
                    System.out.println("Файл стучится");
                    currentState = State.FILE_NAME_LENGTH;
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
                while (buf.readableBytes() > 0 && commandLength != 0) {
                    commandLength--;
                    stringBuilder.append((char) buf.readByte());
                }
                currentState = State.COMMAND_DO;
            }

            if (currentState == State.COMMAND_DO) {
                String[] command = stringBuilder.toString().split(" ");
                if (command[0].equals("/authorization")) {
//                currentPath = Path.of("storage", "wigravy");
                    FileService.sendCommand(ctx.channel(), "/authorization OK");
//                        currentPath = CommandService.authorization(command[1], command[2], ctx.channel());
                    currentState = State.IDLE;
                } else if (command[0].equals("/download")) {
                    CommandService.downloadFile(ctx.channel(), currentPath.resolve(command[1]));
                    currentState = State.IDLE;
                } else if (command[0].equals("/enterToDirectory")) {
                    currentPath = currentPath.resolve(command[1]);
                    currentState = State.FILE_LIST;
                } else if (command[0].equals("/updateFileList")) {
                    currentState = State.FILE_LIST;
                } else if (command[0].equals("/delete")) {
                    FileService.deleteFile(currentPath.resolve(command[1]));
                    currentState = State.FILE_LIST;
                } else if (command[0].equals("/rename")) {
                    System.out.println(currentPath.resolve(" путь до файла: " + command[1]) + " новое имя файла: " + command[2]);
                    FileService.renameFile(currentPath.resolve(command[1]), command[2]);
                    currentState = State.FILE_LIST;
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
                    System.out.println("Длинна имени файла: " + filenameLength);
                }
            }

            if (currentState == State.NAME) {

                while (buf.readableBytes() >= filenameLength) {
                    byte[] filenameBytes = new byte[filenameLength];
                    buf.readBytes(filenameBytes);
                    String filename = new String(filenameBytes, StandardCharsets.UTF_8);
                    File file = new File(currentPath + "/" + filename);
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    currentState = State.FILE_SIZE;
                    System.out.println("Имя файла: " + filename);
                }
            }

            if (currentState == State.FILE_SIZE) {
                if (buf.readableBytes() >= 8) {
                    fileSize = buf.readLong();
                    currentState = State.FILE;
                    System.out.println("Размер файла: " + fileSize);
                }
            }

            if (currentState == State.FILE) {
                long receivedFileSize = 0;
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileSize++;
                    if (fileSize == receivedFileSize) {
                        System.out.println("Готово");
                        currentState = State.FILE_LIST;
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
                stringBuilder = new StringBuilder();
                for (FileInfo fileInfo : serverFiles) {
                    stringBuilder.append(String.format("%s,%d,%s,%s\n", fileInfo.getFileName(), fileInfo.getSize(), fileInfo.getFileType(), fileInfo.getLastModified()));
                }
                FileService.sendCommand(ctx.channel(), "/FileList " + stringBuilder.toString());
                currentState = State.IDLE;
            }
        }
    }

        @Override
        public void channelActive (ChannelHandlerContext ctx) throws Exception {
            System.out.println(ctx.channel().remoteAddress() + " channel is connected.");
        }

        @Override
        public void channelInactive (ChannelHandlerContext ctx) throws Exception {
            System.out.println(ctx.channel().remoteAddress() + " channel is disconnected.");
        }

        @Override
        public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                FileService.sendCommand(ctx.channel(), "ERR: " +
                        cause.getClass().getSimpleName() + ": " +
                        cause.getMessage() + '\n');
            }
        }
    }



