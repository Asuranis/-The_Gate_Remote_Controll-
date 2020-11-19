package cz.grc.commands;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import thegate.main.Globals;
import thegate.main.TheGateMain;

/**
 *
 * @author rkriebel
 */
public class GetGRC implements CommandExecutor, Listener {

    private TheGateMain mainGate;
    private FileConfiguration TextConfig;

    public GetGRC(TheGateMain mainGate) {
        this.mainGate = mainGate;
        this.TextConfig = mainGate.configManager.TextConfig;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("thegate.user.gatetools")) {
                GATETOOLSGUI(player);
            } else {
                player.sendMessage(this.TextConfig.getString("PlayerMessages.GlobalText.NoPermission").replace('&', '§'));
            }
        } else {
            System.out.println("Command only for players");
        }
        return true;
    }

    private void GATETOOLSGUI(Player player) {
        Inventory invent = Bukkit.createInventory(null, 9, "Get GRC");
        ItemStack GRCItem = new ItemStack(Globals.DefaultAbydosCartouche);
        ItemMeta GRCItemMeta = GRCItem.getItemMeta();
        GRCItemMeta.setDisplayName("Gate RC");
        ArrayList<String> Lore = new ArrayList<>();
        Lore.add("GRC");
        GRCItemMeta.setLore(Lore);
        GRCItem.setItemMeta(GRCItemMeta);
        invent.setItem(5, GRCItem);
        if (player.hasPermission("thegate.user.gatetools")) {
            player.openInventory(invent);
        } else {
            player.sendMessage(this.TextConfig.getString("PlayerMessages.GlobalText.NoPermission").replace('&', '§'));
        }
    }

}
