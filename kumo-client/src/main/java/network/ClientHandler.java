package network;

import Utils.Messages.AbstractMessage;

import Utils.Messages.AuthtorizationMessage;
import controllers.AuthorizationController;
import controllers.Controller;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import main.Main;

import java.io.IOException;


@Log4j2
public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    @Setter
    private AuthorizationController authorizationController;
    @Setter
    private Controller controller;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        if (abstractMessage instanceof AuthtorizationMessage) {
            authorization(abstractMessage);
        }
    }

    private void authorization(AbstractMessage abstractMessage) throws IOException {
        AuthtorizationMessage authmsg = (AuthtorizationMessage) abstractMessage;
        if (authmsg.isAuth()) {
            Main.setRootSimple();
        } else {
            authorizationController.LoginError();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}



