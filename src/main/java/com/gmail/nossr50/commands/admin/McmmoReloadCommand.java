package com.gmail.nossr50.commands.admin;

import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class McmmoReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label,
            String[] args) {
        if (args.length == 0) {
            if (!Permissions.reload(sender)) {
                sender.sendMessage(command.getPermissionMessage());
                return true;
            }

            if (mcMMO.p.reloadConfigs()) {
                sender.sendMessage(LocaleLoader.getString("Commands.Reload.Success"));
            } else {
                sender.sendMessage(LocaleLoader.getString("Commands.Reload.Failure"));
            }

            return true;
        }
        return false;
    }
}
