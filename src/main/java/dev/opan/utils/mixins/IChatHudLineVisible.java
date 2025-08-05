package dev.opan.utils.mixins;

public interface IChatHudLineVisible {
    boolean opan$isClientMessage();

    void opan$setClientMessage(boolean clientMessage);

    String opan$getClientIdentifier();

    void opan$setClientIdentifier(String clientIdentifier);
}
