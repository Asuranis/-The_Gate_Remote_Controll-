package cz.grc.guis;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import thegate.gate.GateManager;
import thegate.gate.GateObject;
import thegate.gui.easygui.GUIPages;
import thegate.main.Globals;
import thegate.main.TheGateMain;

/**
 *
 * @author rkriebel
 */
public class GateRCGui extends GUIPages {

    private TheGateMain mainGate;

    private FileConfiguration TextConfig;

    GateObject gate;

    ArrayList<GateObject> userGates = new ArrayList<>();

    public GateRCGui(Player p, String name, String gateadress, TheGateMain mainGate) {
        super(p, 54, name, "GateRCGui");
        this.mainGate = mainGate;
        this.gate = GateManager.getGateWithAddress(gateadress);
        this.TextConfig = mainGate.configManager.TextConfig;
        setup();
        setupFunctions();
        CondPerms();
    }

    public void CondPerms() {
        addUIAccessPermission(new String[]{"thegate.user.quickdial"});
        setDefaultErrorMessage(this.TextConfig.getString("PlayerMessages.GlobalText.NoPermission").replace('&', '§'));
    }

    public void setup() {

        for (GateObject obj : GateManager.getGatesAsSet()) {
            if (obj.getOwnerUUID().equals(getPlayer().getUniqueId()) && (this.gate.getNetwork().equals(obj.getNetwork())
                    || this.gate.getNetwork().equals(obj.getSecondaryNetwork())
                    || this.gate.getSecondaryNetwork().equals(obj.getNetwork()))) {
                this.userGates.add(obj);
            }
        }
        for (GateObject obj : GateManager.getGatesAsSet()) {
            if (obj.isOpen() && !obj.getOwnerUUID().equals(getPlayer().getUniqueId()) && (this.gate.getNetwork().equals(obj.getNetwork())
                    || this.gate.getNetwork().equals(obj.getSecondaryNetwork())
                    || this.gate.getSecondaryNetwork().equals(obj.getNetwork()))) {
                this.userGates.add(obj);
            }
        }
        this.userGates.remove(this.gate);

        if (Globals.UseBungee) {
            for (GateObject obj : GateManager.getGatesOnOtherServer()) {
                if (obj.getOwnerUUID().equals(getPlayer().getUniqueId()) && (this.gate.getNetwork().equals(obj.getNetwork())
                        || this.gate.getNetwork().equals(obj.getSecondaryNetwork())
                        || this.gate.getSecondaryNetwork().equals(obj.getNetwork()))) {
                    this.userGates.add(obj);
                }
            }
            for (GateObject obj : GateManager.getGatesOnOtherServer()) {
                if (obj.isOpen() && !obj.getOwnerUUID().equals(getPlayer().getUniqueId()) && (this.gate.getNetwork().equals(obj.getNetwork())
                        || this.gate.getNetwork().equals(obj.getSecondaryNetwork())
                        || this.gate.getSecondaryNetwork().equals(obj.getNetwork()))) {
                    this.userGates.add(obj);
                }
            }
            this.userGates.remove(this.gate);
        }
        ArrayList<ItemStack> items = new ArrayList<>();

        if (gate.isActive() || gate.isDialinginProssed()) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Disable dialing or gate connection");
            items.add(createItem("Deactivate", lore, Material.REDSTONE_BLOCK));
        }

        for (int i = 0; i < this.userGates.size(); i++) {
            GateObject GATE = this.userGates.get(i);
            ArrayList<String> lore = new ArrayList<>();
            String GATENAME = GATE.getGateName();
            String WORLD = GATE.getWorldName();
            String LocX = (new StringBuilder(String.valueOf(GATE.getGate().getX()))).toString();
            String LocY = (new StringBuilder(String.valueOf(GATE.getGate().getY()))).toString();
            String LocZ = (new StringBuilder(String.valueOf(GATE.getGate().getZ()))).toString();
            String NET = GATE.getNetwork();
            String OWNER = GATE.getOwnerName();
            String DESCRIPTION = GATE.getDescription();
            for (String s : this.TextConfig.getStringList("GUIS.QuickDialGUI.Items.GateObjects.Lore")) {
                lore.add(s.replace("&", "§").replace("{GATENAME}", GATENAME).replace("{WORLD}", WORLD).replace("{X}", LocX).replace("{Y}", LocY).replace("{Z}", LocZ).replace("{NETWORK}", NET).replace("{OWNER}", OWNER).replace("{DESCRIPTION}", DESCRIPTION));
            }
            items.add(createItem(GATE.getAddress(), lore, (Globals.UseBungee && !GATE.getServer().equals(Globals.ServerName)) ? Material.ENDER_PEARL : Material.HEART_OF_THE_SEA));
        }
        setSorceList(items);
        setNextPage(createItem(this.TextConfig.getString("GUIS.QuickDialGUI.Items.NextPage").replace("&", "§"), null, Material.PAPER));
        setPrevPage(createItem(this.TextConfig.getString("GUIS.QuickDialGUI.Items.PreviousPage").replace("&", "§"), null, Material.PAPER));

    }

    public void setupFunctions() {

        setGeneralFunction(x -> {
            if (x.item == null || x.event.getRawSlot() >= 45 || x.item.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
                return;
            }

            String Address = x.item.getItemMeta().getDisplayName();
            if ((gate.isDialinginProssed()) || gate.isOpen()) {
                if (x.item.getType().equals(Material.REDSTONE_BLOCK) && (Address != null && Address.contains("Deactivate"))) {
                    String target = gate.getDiled();
                    if (target != null && target.length() > 1) {
                        GateObject go = GateManager.getGateWithAddress(target);
                        go.Deactivate();
                    }
                    gate.Deactivate();
                    getPlayer().closeInventory();
                    return;
                }
            }

            GateObject go = GateManager.getGateWithAddress(Address);
            if (!Globals.UseBungee && go == null) {
                getPlayer().sendMessage(this.TextConfig.getString("PlayerMessages.FromGUI.QuickDialGUI.Message1").replace("&", "§").replace("{ADDRESS}", Address));
                getPlayer().closeInventory();
                return;
            }
            if (!Globals.UseBungee && !go.isOpen() && !go.getOwnerUUID().equals(getPlayer().getUniqueId())) {
                getPlayer().sendMessage(this.TextConfig.getString("PlayerMessages.FromGUI.QuickDialGUI.Message2").replace("&", "§"));
                getPlayer().closeInventory();
                return;
            }
            if (Globals.UseBungee && go == null && GateManager.hasGateOnOtherServerWithAddress(Address) && !this.gate.isActive() && !this.gate.isDialinginProssed()) {
                go = GateManager.getGateOnOtherServerWithAddress(Address);
                if (!this.gate.getNetwork().equals(go.getNetwork()) && !this.gate.getNetwork().equals(go.getSecondaryNetwork()) && !this.gate.getSecondaryNetwork().equals(go.getNetwork())) {
                    getPlayer().sendMessage(this.TextConfig.getString("PlayerMessages.FromGUI.DHD_GUI.Message12").replace('&', '§'));
                    getPlayer().closeInventory();
                    return;
                }
                if (go.isActive() || this.gate.isActive() || go.isLocked() || this.gate.isLocked() || go == null || go.isDialinginProssed() || this.gate.isDialinginProssed()) {
                    getPlayer().sendMessage(this.TextConfig.getString("PlayerMessages.FromGUI.DHD_GUI.Message11").replace('&', '§'));
                    getPlayer().closeInventory();
                    return;
                }
                if (this.mainGate.SaveLoadInterface.hasGateWithAddressInTableGates(Address)) {
                    this.gate.StartDialingOutSequenceSingleGate((Plugin) this.mainGate, this.mainGate, Address, getPlayer());
                    this.gate.StartDialingOutSequenceSingleGate((Plugin) this.mainGate, this.mainGate, Address, getPlayer());
                    this.gate.setDialinginProssed(true);
                    getPlayer().closeInventory();
                    return;
                }
                getPlayer().sendMessage(this.TextConfig.getString("PlayerMessages.FromGUI.DHD_GUI.Message11").replace('&', '§'));
                getPlayer().closeInventory();
                return;
            }
            if (this.gate.getNetwork().equals(go.getNetwork()) || this.gate.getNetwork().equals(go.getSecondaryNetwork()) || this.gate.getSecondaryNetwork().equals(go.getNetwork())) {
                if (GateManager.hasGateWithAddress(Address)) {
                    if (!go.isActive() && !this.gate.isActive() && !go.isLocked() && !this.gate.isLocked() && !go.isDialinginProssed() && !this.gate.isDialinginProssed()) {
                        if (Globals.DoAnimation) {
                            this.gate.StartDialingOutSequenceSingleGate((Plugin) this.mainGate, this.mainGate, Address, getPlayer());
                            this.gate.setDiled(go.getAddress());
                            go.setDiled(this.gate.getAddress());
                            this.gate.setDialinginProssed(true);
                            go.setDialinginProssed(true);
                        } else {
                            this.gate.Activate(Address, true);
                            go.Activate(Address, false);
                            this.gate.setDialinginProssed(true);
                            go.setDialinginProssed(true);
                        }
                        getPlayer().closeInventory();
                        return;
                    }
                }
            } else {
                getPlayer().sendMessage(this.TextConfig.getString("PlayerMessages.FromGUI.QuickDialGUI.Message4").replace("&", "§"));
                getPlayer().closeInventory();
            }
        });
    }
}
