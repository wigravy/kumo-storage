package Utils.Messages;


public class AuthtorizationMessage extends AbstractMessage{
    private boolean auth;

    public AuthtorizationMessage(boolean auth) {
        this.auth = auth;
    }

    public boolean isAuth() {
        return auth;
    }


}
