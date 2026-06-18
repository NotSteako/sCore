package kami.gg.souppvp;

import kami.gg.souppvp.command.admin.*;
import kami.gg.souppvp.command.guild.GuildStatsCommand;
import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import kami.gg.souppvp.coinflip.listener.CoinFlipListener;
import kami.gg.souppvp.coinflip.listener.WagerCustomEventListeners;
import kami.gg.souppvp.command.*;
import kami.gg.souppvp.command.admin.statistics.SetStatisticsBountyCommand;
import kami.gg.souppvp.command.admin.statistics.SetStatisticsDeathsCommand;
import kami.gg.souppvp.command.admin.statistics.SetStatisticsKillsCommand;
import kami.gg.souppvp.command.admin.statistics.SetStatisticsKillstreakCommand;
import kami.gg.souppvp.command.bounty.BountyCommand;
import kami.gg.souppvp.command.bounty.BountyListCommand;
import kami.gg.souppvp.command.credit.CreditsAddCommand;
import kami.gg.souppvp.command.credit.CreditsPayCommand;
import kami.gg.souppvp.command.credit.CreditsSetCommand;
import kami.gg.souppvp.command.guild.*;
import kami.gg.souppvp.command.leave.LeaveCommand;
import kami.gg.souppvp.command.leave.OPLeaveCommand;
import kami.gg.souppvp.command.shop.CreditsCommand;
import kami.gg.souppvp.command.shop.RepairCommand;
import kami.gg.souppvp.cosmetics.PreviewListener;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleHandler;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffleListener;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersHandler;
import kami.gg.souppvp.events.impl.fourcorners.FourCornersListener;
import kami.gg.souppvp.events.impl.sumo.SumoHandler;
import kami.gg.souppvp.events.impl.sumo.SumoListener;
import kami.gg.souppvp.ffa.listener.FFAPvPListener;
import kami.gg.souppvp.ffa.listener.FFAWandListener;
import kami.gg.souppvp.gui.WarpGUI;
import kami.gg.souppvp.guild.listener.GuildHexChatHandler;

import kami.gg.souppvp.handlers.*;
import kami.gg.souppvp.juggernaut.JuggernautListener;
import kami.gg.souppvp.killstreak.KillstreaksHandler;
import kami.gg.souppvp.kit.KitsHandler;
import kami.gg.souppvp.kit.editor.KitEditorListener;
import kami.gg.souppvp.kit.inherit.*;
import kami.gg.souppvp.listener.*;
import kami.gg.souppvp.mooshroom.MooshroomListener;
import kami.gg.souppvp.mooshroom.command.MooshroomCommand;
import kami.gg.souppvp.mooshroom.command.MooshroomCreateCommand;
import kami.gg.souppvp.mooshroom.command.MooshroomListCommand;
import kami.gg.souppvp.mooshroom.command.MooshroomRemoveCommand;
import kami.gg.souppvp.mooshroom.handler.MooshroomHandler;
import kami.gg.souppvp.perk.PerksHandler;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.profile.ProfileTypeAdapter;
import kami.gg.souppvp.scoreboard.ScoreboardAdapter;
import kami.gg.souppvp.tablist.RankedTabAdapter;
import kami.gg.souppvp.tasks.CanaPerkAndFiremanKitTask;
import kami.gg.souppvp.tasks.ClearDropsTask;
import kami.gg.souppvp.tasks.ClearTimerCacheTask;
import kami.gg.souppvp.tasks.SaveProfilesTask;
import kami.gg.souppvp.tasks.SoupPvPPlaceholderExpansion;
import kami.gg.souppvp.teleportation.SpawnTeleporatationListener;
import kami.gg.souppvp.tier.TiersListener;
import kami.gg.souppvp.timer.TimersHandler;
import kami.gg.souppvp.timer.TimersListener;
import kami.gg.souppvp.util.assemble.Assemble;
import kami.gg.souppvp.util.menu.MenuListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.refinedev.api.tablist.TablistHandler;
import xyz.refinedev.phoenix.Phoenix;

import xyz.refinedev.api.skin.SkinAPI;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

// Events
import kami.gg.souppvp.events.impl.redrover.RedRoverHandler;
import kami.gg.souppvp.events.impl.redrover.RedRoverListener;

@Getter @Setter
public class SoupPvP extends JavaPlugin {

    @Getter public static final Gson GSON = new Gson();
    @Getter public static final Type LIST_STRING_TYPE = new TypeToken<List<String>>() {}.getType();
    @Getter @Setter public static Boolean isFreeKitsMode;
    @Getter public static SoupPvP instance;


    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private KitsHandler kitsHandler;
    private ProfilesHandler profilesHandler;
    private CombatTagsHandler combatTagsHandler;
    private SpawnTeleportationHandler spawnTeleportationHandler;
    private CoinFlipsHandler coinFlipsHandler;
    private SpawnHandler spawnHandler;
    private ClearDropsTask clearDropsTask;
    private SaveProfilesTask saveProfilesTask;
    private ClearTimerCacheTask clearTimerCacheTask;
    private CanaPerkAndFiremanKitTask canaPerkAndFiremanKitTask;


    // Events
    private SumoHandler sumoHandler;
    private RedRoverHandler redRoverHandler;
    private ColourShuffleHandler colourShuffleHandler;
    private FourCornersHandler fourCornersHandler;


    private LeaderboardHandler leaderboardHandler;


    private NoFallDamageHandler noFallDamageHandler;
    private PerksHandler perksHandler;
    private KillstreaksHandler killstreaksHandler;
    private TimersHandler timersHandler;
    private CommandService drink;

    // Tablist
    private Phoenix phoenixAPI;
    private TablistHandler tablistHandler;
    private SkinAPI skinAPI;
    private RankedTabAdapter tabAdapter;

    // Mooshroom AI
    private MooshroomHandler mooshroomHandler;

    // Guild
    private GuildsHandler guildsHandler;
    private GuildTeamHandler teamsHandler;

    private PacketBorderHandler packetHandler;


    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        isFreeKitsMode = getConfig().getBoolean("FREE-KITS");

        for (World world : Bukkit.getWorlds()) {
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doWeatherCycle", "false");
            world.setTime(6000L);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SoupPvPPlaceholderExpansion(this).register();
        }

// Database first
        setupDatabase();

// Core handlers first
        kitsHandler = new KitsHandler();
        profilesHandler = new ProfilesHandler();

// Leaderboards AFTER profilesHandler exists
        leaderboardHandler = new LeaderboardHandler();

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            leaderboardHandler.refreshAll();
        }, 20L);

        combatTagsHandler = new CombatTagsHandler();
        spawnTeleportationHandler = new SpawnTeleportationHandler();
        coinFlipsHandler = new CoinFlipsHandler();
        spawnHandler = new SpawnHandler();
        noFallDamageHandler = new NoFallDamageHandler();
        perksHandler = new PerksHandler();
        killstreaksHandler = new KillstreaksHandler();
        timersHandler = new TimersHandler();
        clearDropsTask = new ClearDropsTask();
        saveProfilesTask = new SaveProfilesTask();
        clearTimerCacheTask = new ClearTimerCacheTask();
        canaPerkAndFiremanKitTask = new CanaPerkAndFiremanKitTask();

        colourShuffleHandler = new ColourShuffleHandler();
        sumoHandler = new SumoHandler();
        redRoverHandler = new RedRoverHandler();
        fourCornersHandler = new FourCornersHandler();

        skinAPI = new SkinAPI();

        tablistHandler = new TablistHandler(this);
        tablistHandler.init(PacketEvents.getAPI());
        tablistHandler.setupSkinCache(skinAPI);

        mooshroomHandler = new MooshroomHandler();

        guildsHandler = new GuildsHandler();
        teamsHandler = new GuildTeamHandler();

        long updateInterval = getConfig().getLong("update-interval", 40L);
        tabAdapter = new RankedTabAdapter(this);
        tablistHandler.registerAdapter(tabAdapter, updateInterval);

        packetHandler = new PacketBorderHandler();
        packetHandler.start();

        drink = Drink.get(this);

        registerScoreboard();
        registerCommands();
        registerListeners();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            new SoupHoloCommand().restoreRefreshTasks();
        }, 60L);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            new HoloKitCommand().restoreRefreshTasks();
        }, 60L);

    }


    @Override
    public void onDisable() {
        if (profilesHandler != null) profilesHandler.saveProfiles();
        if (mongoClient != null) mongoClient.close();
    }

    private void setupDatabase() {
        if (getConfig().getBoolean("MONGO.URI.ENABLED")) {
            MongoClientURI mongoClientURI = new MongoClientURI(getConfig().getString("MONGO.URI.CONNECTION"));
            mongoClient = new MongoClient(mongoClientURI);
            mongoDatabase = mongoClient.getDatabase(getConfig().getString("MONGO.URI.DATABASE"));
        } else if (getConfig().getBoolean("MONGO.AUTHENTICATION.ENABLED")) {
            mongoClient = new MongoClient(
                    new ServerAddress(getConfig().getString("MONGO.HOST"), getConfig().getInt("MONGO.PORT")),
                    Collections.singletonList(MongoCredential.createCredential(
                            getConfig().getString("MONGO.AUTHENTICATION.USERNAME"),
                            getConfig().getString("MONGO.AUTHENTICATION.AUTHENTICATION-DATABASE"),
                            getConfig().getString("MONGO.AUTHENTICATION.PASSWORD").toCharArray()))
            );
            mongoDatabase = mongoClient.getDatabase(getConfig().getString("MONGO.AUTHENTICATION.AUTHENTICATION-DATABASE"));
        } else {
            mongoClient = new MongoClient(new ServerAddress(getConfig().getString("MONGO.HOST"), getConfig().getInt("MONGO.PORT")));
            mongoDatabase = mongoClient.getDatabase(getConfig().getString("MONGO.DATABASE"));
        }
    }

    private void registerScoreboard() {
        Assemble assemble = new Assemble(this, new ScoreboardAdapter());
        assemble.setTicks(2);
    }

    public void registerCommands() {
        drink.bind(Profile.class).toProvider(new ProfileTypeAdapter());
        drink.register(new SetStatisticsBountyCommand(), "setstatistics", "")
                .registerSub(new SetStatisticsKillstreakCommand())
                .registerSub(new SetStatisticsDeathsCommand())
                .registerSub(new SetStatisticsKillsCommand())
                .registerSub(new SetStatisticsKillstreakCommand());
        drink.register(new CuboidSetCommand(), "cuboid", "");
        drink.register(new BountyCommand(), "bounty", "")
                .registerSub(new BountyListCommand());
        drink.register(new CreditsAddCommand(), "credits", "credit")
                .registerSub(new CreditsSetCommand());
        drink.register(new CreditsPayCommand(), "pay", "");
        drink.register(new CreditsCommand(), "help", "");
        drink.register(new SoupHoloCommand(), "soupholo", "");
        drink.register(new HoloKitCommand(), "holokit", "");
        drink.register(new LeaveCommand(), "leave", "spawn");
        drink.register(new WipeCommand(), "wipe");
        drink.register(new WipeCommand(), "wipebyuuid");
        drink.register(new OPLeaveCommand(), "opleave", "opspawn");
        drink.register(new RepairCommand(), "repair", "fix");
        drink.register(new CoinflipCommand(), "coinflip", "wager", "cf");
        drink.register(new FreeKitsCommand(), "free", "");
        drink.register(new KillstreakCommand(), "killstreak", "killstreak", "ks");
        drink.register(new OptionsCommand(), "option", "options", "setting", "settings");
        drink.register(new PerksCommand(), "perks", "perk");
        drink.register(new ShopCommand(), "shop", "");
        drink.register(new StatisticsCommand(), "statistics", "statistic", "stats", "stat");
        drink.register(new TiersCommand(), "tiers", "tier");
        drink.register(new BuildCommand(), "build");

        // Mooshroom
        drink.register(new MooshroomCommand(), "mooshroom", "mooshrooms")
                .registerSub(new MooshroomCreateCommand())
                .registerSub(new MooshroomRemoveCommand())
                .registerSub(new MooshroomListCommand());

        // SetLobby
        drink.register(new SetLobbyCommand(), "setlobby");

        // Warp Menu
        drink.register(new WarpMenuCommand(), "warpmenu");

        // Warp
        drink.register(new WarpCommand(this), "warp", "warps");

        // Kit
        drink.register(new KitCommand(), "kit", "kits");

        drink.register(new CooldownCommand(), "cooldown");

        drink.register(new SpawnWandCommand(), "spawnwand");

        // Guild commands
        drink.register(new GuildCommand(), "guild", "g")
                .registerSub(new GuildCreateCommand())
                .registerSub(new GuildDisbandCommand())
                .registerSub(new GuildLeaveCommand())
                .registerSub(new GuildInviteCommand())
                .registerSub(new GuildAcceptCommand())
                .registerSub(new GuildDenyCommand())
                .registerSub(new GuildKickCommand())
                .registerSub(new GuildListCommand())
                .registerSub(new GuildInfoCommand())
                .registerSub(new GuildStatsCommand())
                .registerSub(new GuildTagCommand());

        drink.register(new KitEditorCommand(), "kiteditor", "editkit", "ke");


        drink.registerCommands();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new GeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new SoupListeners(), this);
        getServer().getPluginManager().registerEvents(new SpawnEventItemsListener(), this);
        getServer().getPluginManager().registerEvents(new PvPListeners(), this);
        getServer().getPluginManager().registerEvents(new SpawnListeners(), this);
        getServer().getPluginManager().registerEvents(new SpawnTeleporatationListener(), this);
        getServer().getPluginManager().registerEvents(new ShopItemsListener(), this);
        getServer().getPluginManager().registerEvents(new TiersListener(), this);
        getServer().getPluginManager().registerEvents(new KillStreakAnnouncerListener(), this);
        getServer().getPluginManager().registerEvents(new BountyListener(), this);
        getServer().getPluginManager().registerEvents(new LunarClientListener(), this);
        getServer().getPluginManager().registerEvents(new NoFallDamageListener(), this);
        getServer().getPluginManager().registerEvents(new CoinFlipListener(), this);
        getServer().getPluginManager().registerEvents(new WagerCustomEventListeners(), this);
        getServer().getPluginManager().registerEvents(new JuggernautListener(), this);
        getServer().getPluginManager().registerEvents(new StrengthAndInstantHarmNerfListener(), this);
        getServer().getPluginManager().registerEvents(new TimersListener(), this);

        // FFA
        getServer().getPluginManager().registerEvents(new FFAListener(), this);
        getServer().getPluginManager().registerEvents(new FFAPvPListener(), this);
        getServer().getPluginManager().registerEvents(new FFAWandListener(), this);

        // Mooshroom
        getServer().getPluginManager().registerEvents(new MooshroomListener(), this);

        // No Collide
        getServer().getPluginManager().registerEvents(new NoCollideListener(), this);

        // Events
        getServer().getPluginManager().registerEvents(new RedRoverListener(), this);
        getServer().getPluginManager().registerEvents(new SumoListener(), this);
        getServer().getPluginManager().registerEvents(new ColourShuffleListener(), this);
        getServer().getPluginManager().registerEvents(new FourCornersListener(), this);

        getServer().getPluginManager().registerEvents(new WarpGUI(), this);

        getServer().getPluginManager().registerEvents(new GuildHexChatHandler(), this);

        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(), this);

        getServer().getPluginManager().registerEvents(new PreviewListener(), this);

        getServer().getPluginManager().registerEvents(new KitEditorListener(), this);


    }

    public TablistHandler  getTablistHandler()    { return tablistHandler;  }
    public SkinAPI         getSkinAPI()           { return skinAPI;         }


}
