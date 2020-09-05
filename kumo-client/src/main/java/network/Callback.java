package network;

import Utils.Messages.AbstractMessage;

public interface Callback {
    void call(AbstractMessage message);
}
