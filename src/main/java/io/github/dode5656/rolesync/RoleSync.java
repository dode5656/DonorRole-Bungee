package io.github.dode5656.rolesync;

import io.github.dode5656.rolesync.commands.ReloadCommand;
import io.github.dode5656.rolesync.commands.SyncCommand;
import io.github.dode5656.rolesync.events.JoinEvent;
import io.github.dode5656.rolesync.storage.FileStorage;
import io.github.dode5656.rolesync.utilities.MessageManager;
import io.github.dode5656.rolesync.utilities.PluginStatus;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.logging.Level;

public class RoleSync extends Plugin {

    private FileStorage playerCache;
    private FileStorage messages;
    private FileStorage config;
    private MessageManager messageManager;
    private JDA jda;
    PluginStatus pluginStatus;

    @Override
    public void onEnable() {
        pluginStatus = PluginStatus.ENABLED;

        config = new FileStorage("config.yml", new File(getDataFolder().getPath()));
        config.saveDefaults(this);

        playerCache = new FileStorage("playerCache.yml", new File(getDataFolder().getPath(), "cache"));

        messages = new FileStorage("messages.yml", new File(getDataFolder().getPath()));
        messages.saveDefaults(this);
        messageManager = new MessageManager(this);

        if (getConfig().getString("bot-token").equals("REPLACEBOTTOKEN")) {

            getLogger().severe(messageManager.defaultError("Bot Token"));
            disablePlugin();

        } else if (getConfig().getString("server-id").equals("REPLACESERVERID")) {

            getLogger().severe(messageManager.defaultError("Server ID"));
            disablePlugin();

        } else if (getConfig().getSection("roles").contains("REPLACEROLEID")) {
            getLogger().severe(messageManager.defaultError("a Role ID"));
            disablePlugin();
        }

        if (!startBot()) { return; }

        getProxy().getPluginManager().registerCommand(this, new SyncCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));
        getProxy().getPluginManager().registerListener(this, new JoinEvent(this));

    }

    @Override
    public void onDisable() {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) jda.shutdown();
    }

    public final FileStorage getMessages() {
        return messages;
    }

    public final FileStorage getPlayerCache() {
        return playerCache;
    }

    public final MessageManager getMessageManager() {
        return messageManager;
    }

    public Configuration getConfig() {
        return config.read();
    }

    public FileStorage getConfigStorage() {
        return config;
    }

    public final JDA getJDA() {
        return jda;
    }

    public PluginStatus getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(PluginStatus pluginStatus) {
        this.pluginStatus = pluginStatus;
    }

    private boolean startBot() {
        try {
            this.jda = new JDABuilder(AccountType.BOT).setToken(getConfig().getString("bot-token")).build();

            return true;
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, "Error when logging in!");
            disablePlugin();
        }

        return false;
    }

    private void disablePlugin() {
        pluginStatus = PluginStatus.DISABLED;
    }
}
