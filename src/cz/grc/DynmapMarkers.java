/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.grc;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import thegate.gate.GateManager;
import thegate.gate.GateObject;
import thegate.main.TheGateMain;
/**
 *
 * @author rkriebel
 */
public class DynmapMarkers {

    private static DynmapAPI dapi;
    private static MarkerSet markerset;
    private static TheGateMain thegate;

    private static Timer timer = null;

    static void hook(TheGateMain thegatem) {
        try {
            thegate = thegatem;
            dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
            if (dapi != null) {
                System.out.println("[GRC] Dynmap found. Attempt to display gates");
                if (timer == null) {
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new DMarkerRefresher(), 100, 60000);
                }
            }else{
                 if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static class DMarkerRefresher extends TimerTask {

        public void run() {
            markerset = dapi.getMarkerAPI().createMarkerSet("id", "Gates", dapi.getMarkerAPI().getMarkerIcons(), false);
            
            MarkerIcon planetIcon = dapi.getMarkerAPI().getMarkerIcon("door");

            Set<GateObject> gates = GateManager.getGatesAsSet();

            for (GateObject gate : gates) {
                Marker planetMarker = markerset.findMarker(gate.getAddress());
                if (planetMarker == null) {
                    markerset.createMarker(gate.getAddress(), gate.getAddress(), gate.getWorldName(), gate.getGate().getX(), gate.getGate().getY(), gate.getGate().getZ(), planetIcon, true);
                } else {
                    planetMarker.setLocation(gate.getWorldName(), gate.getGate().getX(), gate.getGate().getY(), gate.getGate().getZ());
                }
            }
        }
    }
}
