package com.wigravy.kumoStorage.server.main;


import com.wigravy.kumoStorage.common.utils.FileInfo;
import com.wigravy.kumoStorage.common.utils.FileService;
import com.wigravy.kumoStorage.common.utils.ListSignalBytes;
import com.wigravy.kumoStorage.common.utils.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


@Log4j2
public class MainHandler extends SimpleChannelInboundHandler<Object> {
    private State currentState = State.IDLE;
    private long fileSize = 0L;
    private long receivedFileSize = 0L;
    private int filenameLength = 0;
    private int commandLength = 0;
    private StringBuilder stringBuilder;
    private BufferedOutputStream out;
    private Path currentPath = Path.of("storage", "wigravy");
    FileService fileService = new FileService();
    CommandService commandService = new CommandService();


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
                    log.info(String.format("[ip: %s]: Begins file transfer.", ctx.channel().remoteAddress()));
                    currentState = State.COMMAND;
                } else if (readByte == ListSignalBytes.FILE_SIGNAL_BYTE) {
                    log.info(String.format("[ip: %s]: Begins file transfer.", ctx.channel().remoteAddress()));
                    currentState = State.FILE_NAME_LENGTH;
                } else {
                    currentState = State.IDLE;
                    log.error(String.format("[ip: %s]: Unknown byte command arrived.", ctx.channel().remoteAddress()));
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
//                    сurrentPath = Path.of("storage", "wigravy");
                    fileService.sendCommand(ctx.channel(), "/authorization OK");
//                    currentPath = CommandService.authorization(command[1], command[2], ctx.channel());
                    currentState = State.IDLE;
                } else if (command[0].equals("/download")) {
                    log.info(String.format("[ip: %s]: Send command: Download file", ctx.channel().remoteAddress()));
                    commandService.downloadFile(ctx.channel(), currentPath.resolve(command[1]));
                    currentState = State.IDLE;
                    // TODO: сделать вход в директории на клиенте
                } else if (command[0].equals("/enterToDirectory")) {
                    log.info(String.format("[ip: %s]: Send command: Enter to directory", ctx.channel().remoteAddress()));
                    currentPath = currentPath.resolve(command[1]);
                    currentState = State.FILE_LIST;
                } else if (command[0].equals("/updateFileList")) {
                    log.info(String.format("[ip: %s]: Send command: Update file list", ctx.channel().remoteAddress()));
                    currentState = State.FILE_LIST;
                } else if (command[0].equals("/delete")) {
                    log.info(String.format("[ip: %s]: Send command: Delete file", ctx.channel().remoteAddress()));
                    fileService.deleteFile(currentPath.resolve(command[1]));
                    currentState = State.FILE_LIST;
                    // TODO: возможность переименовывать имена с пробелом. Как вариант реализовать отдельный байт для этого и удаления файла
                } else if (command[0].equals("/rename")) {
                    System.out.println(currentPath.resolve(" путь до файла: " + command[1]) + " новое имя файла: " + command[2]);
                    fileService.renameFile(currentPath.resolve(command[1]), command[2]);
                    currentState = State.FILE_LIST;
                } else {
                    // TODO: сделать свой тип ошибки
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
                    File file = new File(currentPath.toString() + "/" + filename);
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    currentState = State.FILE_SIZE;
                    System.out.println("Имя файла: " + filename);
                }
            }

            if (currentState == State.FILE_SIZE) {
                if (buf.readableBytes() >= 8) {
                    fileSize = buf.readLong();
                    currentState = State.FILE;
                    System.out.println("Общий размер файла: " + fileSize);
                }
            }

            if (currentState == State.FILE) {
                System.out.println("Приём файла начался: ");
                try {
                    while (buf.readableBytes() > 0) {
                        out.write(buf.readByte());
                        receivedFileSize++;

                        if (receivedFileSize % 1000 == 0) System.out.println("Количество байт (принято\\всего): " + receivedFileSize + "\\" + fileSize);
                        if (fileSize == receivedFileSize) {
                            System.out.println("Количество байт (принято\\всего): " + receivedFileSize + "\\" + fileSize);
                            System.out.println("Файл принят, текущая стадия: передача списка файлов");
                            receivedFileSize = 0L;
                            fileSize = 0L;
                            currentState = State.FILE_LIST;
                            out.close();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    currentState = State.IDLE;
                    out.close();
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
                fileService.sendCommand(ctx.channel(), "/FileList " + stringBuilder.toString());
                currentState = State.IDLE;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress() + " channel is connected.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress() + " channel is disconnected.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            fileService.sendCommand(ctx.channel(), "ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n');
        }
    }
}



