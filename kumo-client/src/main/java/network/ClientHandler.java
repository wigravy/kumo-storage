package network;

import Utils.Messages.AbstractMessage;

import Utils.Messages.AuthtorizationMessage;
import controllers.AuthorizationController;
import controllers.Controller;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;
import main.ClientApp;

import java.io.IOException;


@Log4j2
public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private AuthorizationController authorizationController = AuthorizationController.getInstance();
    private Controller controller;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        System.out.print(abstractMessage.getClass().getSimpleName() + " has arrived from server");
        if (abstractMessage instanceof AuthtorizationMessage) {
            authorization(abstractMessage);
        }
    }

    private void authorization(AbstractMessage abstractMessage) throws IOException {

        AuthtorizationMessage authmsg = (AuthtorizationMessage) abstractMessage;
        System.out.println(authmsg.isAuth());
        if (authmsg.isAuth()) {
            ClientApp.getInstance().setLogin(true);
            ClientApp.getInstance().changeScene();
        } else {
            authorizationController.LoginError();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}



