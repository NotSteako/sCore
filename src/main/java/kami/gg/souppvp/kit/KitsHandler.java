package kami.gg.souppvp.kit;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.kit.inherit.DefaultKit;
import kami.gg.souppvp.kit.inherit.ProKit;
import kami.gg.souppvp.kit.marvel.*;
import kami.gg.souppvp.kit.overwatch.*;
import kami.gg.souppvp.kit.valorant.PhoenixKit;
import kami.gg.souppvp.kit.valorant.ReynaKit;
import kami.gg.souppvp.kit.valorant.YoruKit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class KitsHandler {

    @Getter public List<Kit> kits;

    public KitsHandler(){
        kits = new ArrayList<>();  //A B C D E F G H I J K L M N O P Q R S T U V W X Y Z

        // Normal
        addKit(new ProKit());
        addKit(new DefaultKit());

        // Overwatch
        addKit(new ReaperKit());
        addKit(new EchoKit());
        addKit(new TracerKit());

        // Marvel
        addKit(new SpidermanKit());
        addKit(new StarlordKit());
        addKit(new WinterSoldierKit());

        // Valorant
        addKit(new YoruKit());
        addKit(new PhoenixKit());
        addKit(new ReynaKit());

        // Media


        registerKitListeners();
    }

    public void registerKitListeners(){
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new DefaultKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new ProKit(), SoupPvP.getInstance());

        // Overwatch
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new ReaperKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new EchoKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new TracerKit(), SoupPvP.getInstance());

        // Marvel
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new SpidermanKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new StarlordKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new WinterSoldierKit(), SoupPvP.getInstance());

        // Valorant
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new YoruKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new PhoenixKit(), SoupPvP.getInstance());
        SoupPvP.getInstance().getServer().getPluginManager().registerEvents(new ReynaKit(), SoupPvP.getInstance());

        // Media

    }

    public Kit getKitByName(String name){
        for (Kit kit : getKits()){
            if (kit.getName().equalsIgnoreCase(name)){
                return kit;
            }
        }
        return null;
    }

    public void addKit(Kit kit){
        getKits().add(kit);
    }

    public void removeKit(Kit kit){
        getKits().remove(kit);
    }

}
