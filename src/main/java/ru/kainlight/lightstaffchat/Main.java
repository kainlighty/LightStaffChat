package ru.kainlight.lightstaffchat;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Plugin(
        id = "lightstaffchat",
        name = "LightStaffChat",
        version = "1.0.0",
        description = "VelocityStaffChat Fork by xii69",
        authors = {"xii69", "kainlight"},
        dependencies = {
                @Dependency(id = "luckperms")
        }
)

@Getter
public class Main implements SimpleCommand {

    @Getter
    private static Main instance;

    private final Logger logger;
    private final ProxyServer server;
    private final Path path;

    @Inject
    public Main(Logger logger, ProxyServer server, @DataDirectory Path path) {
        instance = this;

        this.logger = logger;
        this.server = server;
        this.path = path;
    }

    private Toml toml;
    private String chatChar;
    private List<String> groups;
    private String messageFormat;
    private Set<UUID> toggledPlayers;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.toml = loadConfig();

        if (toml == null) {
            logger.warn("Failed to load config.toml, Shutting down.");
            return;
        }

        this.chatChar = toml.getString("Configuration.Char");
        this.groups = toml.getList("Configuration.Groups");
        this.messageFormat = toml.getString("Messages.Message-Format");
        this.toggledPlayers = new HashSet<>();

        registerCommand("staffchat", new ArrayList<>(List.of("staffchat", "sc")), this, server);
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Эта команда доступа только для игроков").color(NamedTextColor.RED));
            return;
        }

        Staff staff = new Staff(player);
        if (!staff.isStaff()) return;

        if (args.length != 0) {
            this.sendStaffMessage(staff, String.join(" ", args));
            return;
        }

        if (toggledPlayers.contains(staff.getUniqueId())) {
            toggledPlayers.remove(staff.getUniqueId());
            this.sendToggleMessage(staff, false);
        } else {
            toggledPlayers.add(staff.getUniqueId());
            this.sendToggleMessage(staff, true);
        }

    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Staff staff = new Staff(event.getPlayer());

        if (toggledPlayers.contains(staff.getUniqueId())) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            sendStaffMessage(staff, event.getMessage());
        } else if (String.valueOf(event.getMessage().charAt(0)).equalsIgnoreCase(chatChar) && (staff.isStaff())) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            sendStaffMessage(staff, event.getMessage().substring(1));
        }
    }

    private void sendToggleMessage(Staff player, boolean state) {
        final String states = state ? "включен" : "выключен";
        player.sendMessage("Чат администратии теперь в статусе " + states);
    }

    private void sendStaffMessage(Staff player, String message) {
        server.getAllPlayers().stream().filter(target -> Staff.get(target).isStaff()).forEach(target -> {
            Component component = colorize(messageFormat
                    .replace("{username}", player.getUsername())
                    .replace("{prefix}", player.getPrefix())
                    .replace("{server}", server != null ? player.getCurrentServer().getServerInfo().getName() : "N/A")
                    .replace("{message}", message));

            target.sendMessage(component);
        });
    }

    private Toml loadConfig() {
        File file = new File(path.toFile(), "config.toml");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return new Toml().read(file);
    }



    public void registerCommand(String name, Collection<String> aliases, Command command, ProxyServer server) {
        CommandMeta meta = server.getCommandManager().metaBuilder(name).aliases(aliases.toArray(new String[0])).build();
        server.getCommandManager().register(meta, command);
    }

    public static Component colorize(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
