package cz.grc;

import cz.grc.commands.GetGRC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import thegate.main.TheGateMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import thegate.main.*;
import cz.grc.guis.GateRCGui;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import thegate.gate.GateObject;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import thegate.gate.GateManager;
import thegate.gui.easygui.InventoryManager;
import thegate.math.GateMath;

/**
 *
 * @author rkriebel
 */
public class Main_GRC extends JavaPlugin implements Listener {

    TheGateMain thegatem;
    private FileConfiguration TextConfig;

    @Override
    public void onEnable() {

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("The_Gate")) {
            thegatem = (TheGateMain) Bukkit.getServer().getPluginManager().getPlugin("The_Gate");
            System.out.println("[GRC] Stargate system 'The_Gate' found. Start of breaching.");
            getServer().getPluginManager().registerEvents(this, (Plugin) this);
            registerCommands();
            this.TextConfig = ConfigManager.getTextConfigFile();
            System.out.println("[GRC] Stargate system 'The_Gate' breached. Remote controll online");
            DynmapMarkers.hook(thegatem);
        } else {
            System.out.println("[GRC] Cannot access to 'The_Gate' plugin");
            this.setEnabled(false);
        }

    }

    private void registerCommands() {
        GetGRC obtaingrc = new GetGRC(thegatem);
        getCommand("GetGRC").setExecutor(obtaingrc);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && e.getMaterial().equals(Globals.DefaultAbydosCartouche)) {
            if (e.getItem().hasItemMeta()) {
                ItemStack item = e.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta.getDisplayName().contains("Gate RC") &&  meta.getLore().contains("GRC")) {
                    if (GateManager.GateInRadius(player.getLocation())) {
                        GateObject gate = GateManager.getClosestGateTo(player.getLocation());
                        GateRCGui gcg = new GateRCGui(player, "GRC: " + gate.getAddress(), gate.getAddress(), this.thegatem);
                        if (gcg.OpenGUI()) {
                            InventoryManager.addGUI((GateRCGui) gcg);
                        }
                    } else {
                        player.sendMessage(this.TextConfig.getString("PlayerMessages.GlobalText.NoGatesNearBy").replace("&", "§"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleMove(org.bukkit.event.vehicle.VehicleMoveEvent e) {
        Vehicle vehicle = e.getVehicle();
        if (GateManager.GateInRadius(vehicle.getLocation())) {
            GateObject go = GateManager.getClosestGateTo(vehicle.getLocation());
            if (go == null) {
                return;
            }
            double vehiclex = vehicle.getLocation().getX();
            double vehicley = vehicle.getLocation().getY();
            double vehiclez = vehicle.getLocation().getZ();

            if (go.isActive()) {
                GateObject otherGate = GateManager.getGateWithAddress(go.getDiled());
                if (otherGate != null) {
                    if (otherGate.isActive() && go.getGate().getWorld().equals(vehicle.getLocation().getWorld())) {
                        double x = go.getGate().getX() + 0.5D;
                        double y = go.getGate().getY() + 1.5D;
                        double z = go.getGate().getZ() + 0.5D;
                        double otherGatex = otherGate.getGate().getX() + 0.5D;
                        double otherGatey = otherGate.getGate().getY() + 1.5D;
                        double otherGatez = otherGate.getGate().getZ() + 0.5D;
                        if (GateMath.getDistance(vehiclex, vehicley, vehiclez, x, y, z) < 3.0D
                                && go.isAllowTeleport()) {
                            Vector vn = go.getFacingVector();
                            double facing = go.getFacing();
                            double facingothergate = otherGate.getFacing();
                            if (GateMath.DistancePointPlane(new Vector(vehiclex, vehicley, vehiclez), new Vector(x, y, z),
                                    vn) < 0.2D) {
                                if (GateMath.DistancePointPlane(new Vector(vehiclex, vehicley, vehiclez),
                                        new Vector(x, y, z), vn) > -0.3D) {
                                    vehiclex -= x;
                                    vehicley -= y;
                                    vehiclez -= z;
                                    Vector vp = GateMath.RotateVectorY(new Vector(vehiclex, vehicley, vehiclez),
                                            Math.toRadians((-facing + 2.0D) * 90.0D));
                                    Vector vp2 = GateMath.RotateVectorY(vp, Math.toRadians(facingothergate * 90.0D));
                                    vehiclex = vp2.getX();
                                    vehicley = vp2.getY();
                                    vehiclez = vp2.getZ();
                                    vehiclex += otherGatex;
                                    vehicley += otherGatey;
                                    vehiclez += otherGatez;
                                    Location loc = new Location(getServer().getWorld(otherGate.getWorldName()), vehiclex, vehicley + 0.25D, vehiclez);
                                    float vehicleaw = (vehicle.getLocation().getYaw() + 360.0F) % 360.0F;
                                    loc.setYaw((float) ((vehicleaw + 180.0F) + (facingothergate - facing) * 90.0D));
                                    loc.setPitch(vehicle.getLocation().getPitch());
                                    getServer().getWorld(go.getWorldName()).playSound(go.getGate(), Globals.DefaultGateEnterSound,
                                            Globals.DefaultGateEnterVolume, Globals.DefaultGateEnterPitch);
                                    List<Entity> entities = vehicle.getPassengers();

                                    for (Entity pas : entities) {
                                        vehicle.removePassenger(pas);
                                        pas.teleport(loc);
                                        pas.setVelocity(GateMath.RotateVectorY(new Vector(0.0D, 0.0D, 0.3D),
                                                Math.toRadians(facingothergate * 90.0D)));
                                    }
                                    vehicle.teleport(loc);

                                    vehicle.setVelocity(GateMath.RotateVectorY(new Vector(0.0D, 0.0D, 0.3D),
                                            Math.toRadians(facingothergate * 90.0D)).multiply(1.2f));

                                    getServer().getWorld(otherGate.getWorldName()).playSound(otherGate.getGate(),
                                            Globals.DefaultGateExitSound, Globals.DefaultGateExitVolume,
                                            Globals.DefaultGateExitPitch);
                                    this.thegatem.OnCooldown.add(vehicle.getName());
                                    (new BukkitRunnable() {
                                        public void run() {
                                            thegatem.OnCooldown.remove(vehicle.getName());
                                            for (Entity pas : entities) {
                                                vehicle.addPassenger(pas);

                                            }
                                        }
                                    }).runTaskLater((Plugin) this, 50);
                                    return;

                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
