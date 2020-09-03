package network;

public class ServiceMessage extends AbstractMessage{
    private String message;


    private ServiceMessage (ServiceMessageBuilder builder) {
        message = builder.messageBuilder;
    }

    public String getMessage() {
        return message;
    }

    public static ServiceMessageBuilder builder() {
        return new ServiceMessageBuilder();
    }

    public static class ServiceMessageBuilder {
        private String messageBuilder;

        public ServiceMessageBuilder message(String message) {
            messageBuilder = message;
            return this;
        }

        public ServiceMessage build() {
            return new ServiceMessage(this);
        }
    }
}
