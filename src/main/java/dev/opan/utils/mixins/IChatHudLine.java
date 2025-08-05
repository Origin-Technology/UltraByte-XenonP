package dev.opan.utils.mixins;

public interface IChatHudLine {
    boolean opan$isClientMessage();

    void opan$setClientMessage(boolean clientMessage);

    String opan$getClientIdentifier();

    void opan$setClientIdentifier(String clientIdentifier);
}
