package org.job.job.cooking.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.job.job.cooking.gui.*;

public class CookCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§c사용법: /cook <도구>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "mortar":
                new MortarGUI(player).open();
                break;
            case "pot":
                new PotGUI(player).open();
                break;
            case "oven":
                new OvenGUI(player).open();
                break;
            case "pan":
                new PanGUI(player).open();
                break;
            case "blender":
                new BlenderGUI(player).open();
                break;
            default:
                player.sendMessage("§c존재하지 않는 조리 도구입니다.");
        }

        return true;

    }
}
