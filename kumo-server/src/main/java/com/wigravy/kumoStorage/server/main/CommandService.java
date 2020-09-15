package com.wigravy.kumoStorage.server.main;


import com.wigravy.kumoStorage.common.utils.FileService;
import io.netty.channel.Channel;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class CommandService {
    public static Path authorization(String login, String password, Channel channel) {
        boolean isAuthorized = false;
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            isAuthorized = Authorization.getUserByLoginPassword(login, Arrays.toString(hash));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        if (isAuthorized) {
            FileService.sendCommand(channel,"/authorization OK");
            return Authorization.getUserPath(login);
        } else {
            FileService.sendCommand(channel,"/authorization BAD");
            return null;
        }
    }

    public static void downloadFile(Channel channel, Path path) {
        FileService.uploadFile(channel, path, null);
    }

    public static boolean enterToDirectory(Path currentPath, String directory) {
        return false;
    }
}