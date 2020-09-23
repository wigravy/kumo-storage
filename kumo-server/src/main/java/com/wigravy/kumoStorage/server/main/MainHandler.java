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
                String[] command = stringBuilder.toString().split("\n");
                switch (command[0]) {
                    case "/authorization":
                        log.info(String.format("[ip: %s]: Command from client: Authorization", ctx.channel().remoteAddress()));
                        if (command[1].equals("test") && command[2].equals("test")) {
                            fileService.sendCommand(ctx.channel(), "/authorization\nOK");
                        } else {
                            fileService.sendCommand(ctx.channel(), "/authorization\nBAD");
                        }
                        currentState = State.IDLE;
                        break;
                    case "/download":
                        log.info(String.format("[ip: %s]: Command from client: Download file", ctx.channel().remoteAddress()));
                        fileService.uploadFile(ctx.channel(), currentPath.resolve(command[1]), null);
                        currentState = State.IDLE;
                        break;
                    case "/enterToDirectory":
                        log.info(String.format("[ip: %s]: Command from client: Enter to directory %s", ctx.channel().remoteAddress(), command[1]));
                        currentPath = currentPath.resolve(command[1]);
                        currentState = State.FILE_LIST;
                        break;
                    case "/updateFileList":
                        log.info(String.format("[ip: %s]: Command from client: Update file list", ctx.channel().remoteAddress()));
                        currentState = State.FILE_LIST;
                        break;
                    case "/delete":
                        log.info(String.format("[ip: %s]: Command from client: Delete file %s", ctx.channel().remoteAddress(), command[1]));
                        fileService.delete(currentPath.resolve(command[1]));
                        currentState = State.FILE_LIST;
                        break;
                    case "/rename":
                        log.info(String.format("[ip: %s]: Command from client: Rename file. Path to file: (%s). New name: (%s).", ctx.channel().remoteAddress(), command[1], command[2]));
                        fileService.rename(currentPath.resolve(command[1]), command[2]);
                        currentState = State.FILE_LIST;
                        break;
                    case "/upDirectory":
                        log.info(String.format("[ip: %s]: Command from client: Up directory.", ctx.channel().remoteAddress()));
                        if (currentPath.getParent().toString().equals("storage")) {
                            currentState = State.IDLE;
                        } else {
                            currentPath = currentPath.getParent();
                            currentState = State.FILE_LIST;
                        }
                        break;
                    default:
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
                    log.info(String.format("[ip: %s]: File transaction: Get file name length (%s).", ctx.channel().remoteAddress(), filenameLength));
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
                    log.info(String.format("[ip: %s]: File transaction: Get file name (%s)", ctx.channel().remoteAddress(), filename));
                }
            }

            if (currentState == State.FILE_SIZE) {
                if (buf.readableBytes() >= 8) {
                    fileSize = buf.readLong();
                    currentState = State.FILE;
                    log.info(String.format("[ip: %s]: File transaction: Get file size (%s).", ctx.channel().remoteAddress(), fileSize));
                }
            }

            if (currentState == State.FILE) {
                try {
                    if (fileSize != 0) {
                        while (buf.readableBytes() > 0) {
                            out.write(buf.readByte());
                            receivedFileSize++;
                            if (fileSize == receivedFileSize) {
                                log.info(String.format("[ip: %s]: File transaction end.", ctx.channel().remoteAddress()));
                                currentState = State.FILE_LIST;
                                out.close();
                                break;
                            }
                        }
                    } else {
                        currentState = State.FILE_LIST;
                        out.close();
                    }
                } catch (Exception e) {
                    log.error(String.format("[ip: %s]: File transaction ERROR: [%s]: %s", ctx.channel().remoteAddress(), e.getClass().getSimpleName(), e.getMessage()));
                    currentState = State.IDLE;
                    out.close();
                }
            }
            /*
             **      Передача списка файлов
             */

            if (currentState == State.FILE_LIST) {
                log.info(String.format("[ip: %s]: Build and send file list to client", ctx.channel().remoteAddress()));
                List<FileInfo> serverFiles = Files.list(currentPath)
                        .map(FileInfo::new)
                        .collect(Collectors.toList());
                stringBuilder = new StringBuilder();
                for (FileInfo fileInfo : serverFiles) {
                    stringBuilder.append(String.format("%s,%d,%s,%s\n", fileInfo.getFileName(), fileInfo.getSize(), fileInfo.getFileType(), fileInfo.getLastModified()));
                }
                fileService.sendCommand(ctx.channel(), "/FileList\n" + stringBuilder.toString());
                currentState = State.IDLE;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info(String.format("[ip: %s]: Channel is connected", ctx.channel().remoteAddress()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info(String.format("[ip: %s]: Channel is disconnected", ctx.channel().remoteAddress()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(String.format("[ip: %s]: Channel disconnected with error: [%s]: %s", ctx.channel().remoteAddress(), cause.getClass().getSimpleName(), cause.getMessage()));

    }
}



