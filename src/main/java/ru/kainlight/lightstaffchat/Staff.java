package ru.kainlight.lightstaffchat;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.Getter;
import ru.kainlight.lightstaffchat.HOOK.LuckPerms;

import java.util.UUID;

@Getter
final class Staff {

    private final Player player;
    private final String username;
    private final UUID UniqueId;
    private final ServerConnection currentServer;

    private final String group;
    private final String prefix;
    private final String suffix;

    Staff(Player player) {
        this.player = player;
        this.UniqueId = player.getUniqueId();
        this.username = player.getUsername();
        this.currentServer = player.getCurrentServer().get();

        this.group = LuckPerms.getGroup(UniqueId);
        this.prefix = LuckPerms.getPrefix(UniqueId);
        this.suffix = LuckPerms.getSuffix(UniqueId);
    }

    public static Staff get(Player player) {
        return new Staff(player);
    }

    public boolean isStaff() {
        return Main.getInstance().getGroups().contains(group);
    }

    public void sendMessage(String message) {
        player.sendMessage(Main.colorize(message));
    }
}
