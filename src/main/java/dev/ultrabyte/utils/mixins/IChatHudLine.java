package dev.ultrabyte.utils.mixins;

public interface IChatHudLine {
    boolean ultrabyte$isClientMessage();

    void ultrabyte$setClientMessage(boolean clientMessage);

    String ultrabyte$getClientIdentifier();

    void ultrabyte$setClientIdentifier(String clientIdentifier);
}
