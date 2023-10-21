package ru.kainlight.lightstaffchat.HOOK;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class LuckPerms {

    private static final net.luckperms.api.LuckPerms lp = LuckPermsProvider.get();

    public static String getPrefix(UUID uuid) {
        if(getUser(uuid) != null) {
            return getUser(uuid).getCachedData().getMetaData().getPrefix();
        } else return "";
    }

    public static String getSuffix(UUID uuid) {
        if(getUser(uuid) != null) {
            return getUser(uuid).getCachedData().getMetaData().getSuffix();
        } else return "";
    }

    public static String getGroup(UUID uuid) {
        if(getUser(uuid) != null) {
            return getUser(uuid).getPrimaryGroup();
        } else return "";
    }

    public static User getUser(UUID uuid) {
        return lp.getUserManager().getUser(uuid);
    }
}
