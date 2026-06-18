package kami.gg.souppvp.profile;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.CoinFlipState;
import kami.gg.souppvp.events.impl.colourshuffle.ColourShuffle;
import kami.gg.souppvp.events.impl.fourcorners.FourCorners;
import kami.gg.souppvp.events.impl.sumo.Sumo;
import kami.gg.souppvp.kit.DifficultyType;
import kami.gg.souppvp.kit.Kit;
import kami.gg.souppvp.kit.KitCategory;
import kami.gg.souppvp.kit.cosmetic.CosmeticSkin;
import kami.gg.souppvp.tier.Tiers;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.text.DecimalFormat;
import java.util.*;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class Profile {

    private UUID uuid;
    private String username;
    private Boolean loaded;
    private Kit currentKit;
    private Kit previousKit;
    private List<String> unlockedKits;
    private Map<String, List<Integer>> kitLayouts;   // <-- add this line
    private int kills;
    private int deaths;
    private int credits;
    private int bounty;
    private int experiences;
    private Tiers tier;
    private int currentKillstreak;
    private int highestKillstreak;

    private List<String> activePerks;
    private List<String> unlockedPerks;

    private int totalWagerGames;
    private int wagersWon;
    private int wagersLost;

    private Boolean enableKillDeathMessages;
    private Boolean enableParticleEffects;
    private Boolean enableKillstreakMessages;
    private Boolean enableScoreboard;

    private ProfileState profileState;
    private CoinFlipState coinFlipState;

    private Sumo sumoEvent;
    private FourCorners fourCornersEvent;
    private ColourShuffle colourShuffleEvent;
    private int eventsWon;

    private boolean juggernaut;

    private String guildName;

    private KitCategory selectedCategory = KitCategory.ALL;

    private final Map<KitCategory, Integer> categoryPages = new HashMap<>();

    private final Map<String, String> selectedCosmetics = new HashMap<>();

    // Leaderboards per Kits
    private Map<String, Integer> kitKills;

    private final Map<DifficultyType, Integer> difficultyPages = new HashMap<>();



    public Profile(UUID uuid){
        this.uuid = uuid;
        this.username = Bukkit.getOfflinePlayer(uuid).getName();
        this.loaded = false;
        this.currentKit = SoupPvP.getInstance().getKitsHandler().getKitByName("Default");
        this.previousKit = null;
        this.unlockedKits = new ArrayList<>();
        this.kitLayouts = new HashMap<>();
        this.unlockedKits.add("Default");
        this.kills = 0;
        this.deaths = 0;
        this.credits = 0;
        this.bounty = 0;
        this.experiences = 0;
        this.tier = Tiers.ZERO;
        this.currentKillstreak = 0;
        this.highestKillstreak = 0;

        this.activePerks = new ArrayList<>();
        this.activePerks.add("None");
        this.activePerks.add("None");
        this.activePerks.add("None");
        this.unlockedPerks = new ArrayList<>();

        this.totalWagerGames = 0;
        this.wagersWon = 0;
        this.wagersLost = 0;

        this.enableKillDeathMessages = true;
        this.enableParticleEffects = true;
        this.enableKillstreakMessages = true;
        this.enableScoreboard = true;

        this.profileState = ProfileState.SPAWN;
        this.coinFlipState = CoinFlipState.NONE;


       // Events
        this.sumoEvent = null;
        this.fourCornersEvent = null;
        this.colourShuffleEvent = null;


        this.eventsWon = 0;
        this.juggernaut = false;

        this.guildName = null;

        this.kitKills = new HashMap<>();


        this.loadProfile();
    }

    public int getCategoryPage(KitCategory category) {
        return categoryPages.getOrDefault(category, 0);
    }

    public void setCategoryPage(KitCategory category, int page) {
        categoryPages.put(category, Math.max(0, page));
    }

    public int getDifficultyPage(DifficultyType difficulty) {
        return difficultyPages.getOrDefault(difficulty, 0);
    }

    public void setDifficultyPage(DifficultyType difficulty, int page) {
        difficultyPages.put(difficulty, page);
    }

    public Profile(String userName){
        this.uuid = Bukkit.getOfflinePlayer(userName).getUniqueId();
        this.username = userName;
        this.loaded = false;
        this.currentKit = SoupPvP.getInstance().getKitsHandler().getKitByName("Default");
        this.previousKit = null;
        this.unlockedKits = new ArrayList<>();
        this.kitLayouts = new HashMap<>();
        this.unlockedKits.add("Default");
        this.kills = 0;
        this.deaths = 0;
        this.credits = 0;
        this.bounty = 0;
        this.experiences = 0;
        this.tier = Tiers.ZERO;
        this.currentKillstreak = 0;
        this.highestKillstreak = 0;

        this.kitKills = new HashMap<>();


        this.activePerks = new ArrayList<>();
        this.activePerks.add("None");
        this.activePerks.add("None");
        this.activePerks.add("None");
        this.unlockedPerks = new ArrayList<>();

        this.totalWagerGames = 0;
        this.wagersWon = 0;
        this.wagersLost = 0;

        this.enableKillDeathMessages = true;
        this.enableParticleEffects = true;
        this.enableKillstreakMessages = true;
        this.enableScoreboard = true;

        this.profileState = ProfileState.SPAWN;
        this.coinFlipState = CoinFlipState.NONE;

        this.sumoEvent = null;
        this.eventsWon = 0;

        this.juggernaut = false;

        this.guildName = null;


        this.loadProfile();
    }

    public void loadProfile(){
        Document document = SoupPvP.getInstance().getProfilesHandler().getMongoCollection().find(Filters.eq("uuid", getUuid().toString())).first();
        if (document != null){
            currentKit = SoupPvP.getInstance().getKitsHandler().getKitByName(document.getString("currentKit"));
            unlockedKits = SoupPvP.getGSON().fromJson(document.getString("unlockedKits"), SoupPvP.getLIST_STRING_TYPE());
            kills = document.getInteger("kills");
            deaths = document.getInteger("deaths");
            credits = document.getInteger("credits");
            bounty = document.getInteger("bounty");
            currentKillstreak = document.getInteger("currentKillstreak");
            highestKillstreak = document.getInteger("highestKillstreak");
            experiences = document.getInteger("experiences");
            tier = Tiers.getTierByNumber(document.getInteger("tier"));

            String kitLayoutsJson = document.getString("kitLayouts");
            if (kitLayoutsJson != null) {
                kitLayouts = SoupPvP.getGSON().fromJson(kitLayoutsJson,
                        new com.google.gson.reflect.TypeToken<Map<String, List<Integer>>>(){}.getType());
            }
            if (kitLayouts == null) kitLayouts = new HashMap<>();

            Object kitKillsObj = document.get("kitKills");
            if (kitKillsObj instanceof Document) {
                Document kkDoc = (Document) kitKillsObj;
                for (String key : kkDoc.keySet()) {
                    Number n = kkDoc.get(key, Number.class);
                    kitKills.put(key, n == null ? 0 : n.intValue());
                }
            }

            activePerks = SoupPvP.getGSON().fromJson(document.getString("activePerks"), SoupPvP.getLIST_STRING_TYPE());
            unlockedPerks = SoupPvP.getGSON().fromJson(document.getString("unlockedPerks"), SoupPvP.getLIST_STRING_TYPE());

            Document wagersDocument = (Document) document.get("wagers");
            totalWagerGames = wagersDocument.getInteger("totalWagersGames");
            wagersWon = wagersDocument.getInteger("wagersWon");
            wagersLost = wagersDocument.getInteger("wagersLost");

            Document optionsDocument = (Document) document.get("options");
            enableKillDeathMessages = optionsDocument.getBoolean("enableKillDeathMessages");
            enableParticleEffects = optionsDocument.getBoolean("enableParticleEffects");
            enableKillstreakMessages = optionsDocument.getBoolean("enableKillstreakMessages");
            enableScoreboard = optionsDocument.getBoolean("enableScoreboard");

            Document eventsStatisticsDocument = (Document) document.get("eventsStatistics");
            eventsWon = eventsStatisticsDocument.getInteger("eventsWon");

            if (document.containsKey("guildName")) {
                guildName = document.getString("guildName");
            }

        }
        loaded = true;
    }

    // Cosmetics
    public String getSelectedCosmetic(String kitName) {
        return selectedCosmetics.getOrDefault(
                kitName.toLowerCase(),
                "default"
        );
    }

    public void setSelectedCosmetic(String kitName, String cosmeticId) {
        selectedCosmetics.put(
                kitName.toLowerCase(),
                cosmeticId
        );
    }

    // Category Pages
    public int getPage(KitCategory category) {
        return categoryPages.getOrDefault(category, 0);
    }

    public void setPage(KitCategory category, int page) {
        categoryPages.put(category, page);
    }


    public void saveProfile(){
        Document document = new Document();
        document.append("uuid", uuid.toString());
        document.append("username", username);
        document.append("currentKit", currentKit.getName());
        document.append("unlockedKits", unlockedKits.toString());
        document.append("kills", kills);
        document.append("deaths", deaths);
        document.append("credits", credits);
        document.append("bounty", bounty);
        document.append("experiences", experiences);
        document.append("tier", tier.getTierLevel());
        document.append("currentKillstreak", currentKillstreak);
        document.append("highestKillstreak", highestKillstreak);

        document.append("activePerks", activePerks.toString());
        document.append("unlockedPerks", unlockedPerks.toString());

        document.append("kitLayouts", SoupPvP.getGSON().toJson(kitLayouts));

        Document wagersDocument = new Document();
        wagersDocument.append("totalWagersGames", totalWagerGames);
        wagersDocument.append("wagersWon", wagersWon);
        wagersDocument.append("wagersLost", wagersLost);
        document.append("wagers", wagersDocument);

        Document optionsDocument = new Document();
        optionsDocument.append("enableKillDeathMessages", enableKillDeathMessages);
        optionsDocument.append("enableParticleEffects", enableParticleEffects);
        optionsDocument.append("enableKillstreakMessages", enableKillstreakMessages);
        optionsDocument.append("enableScoreboard", enableScoreboard);
        document.append("options", optionsDocument);

        Document eventsStatistics = new Document();
        eventsStatistics.append("eventsWon", eventsWon);
        document.append("eventsStatistics", eventsStatistics);

        document.append("guildName", guildName);

        Document kitKillsDocument = new Document();
        for (Map.Entry<String, Integer> entry : kitKills.entrySet()) {
            kitKillsDocument.append(entry.getKey(), entry.getValue());
        }
        document.append("kitKills", kitKillsDocument);

        SoupPvP.getInstance().getProfilesHandler().getMongoCollection().replaceOne(Filters.eq("uuid", uuid.toString()), document, new ReplaceOptions().upsert(true));
    }

    public Boolean isInEvent() {
        return this.getSumoEvent() != null
                || this.getFourCornersEvent() != null
                || this.colourShuffleEvent != null
                || (SoupPvP.getInstance().getRedRoverHandler() != null
                && SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover() != null
                && SoupPvP.getInstance().getRedRoverHandler().getActiveRedRover()
                .getEventPlayers().containsKey(this.getUuid()));
    }

    public void addSpawnTeleportation(){
        SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().put(uuid, System.currentTimeMillis() + (6*1000));
    }

    public void removeSpawnTeleportation(){
        SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().remove(uuid);
    }

    public Boolean isTeleportingToSpawn(){
        return SoupPvP.getInstance().getSpawnTeleportationHandler().getSpawnTeleporataion().containsKey(uuid);
    }

    public int getKitKills(String kitName) {
        return kitKills.getOrDefault(kitName.toLowerCase(), 0);
    }

    public void incrementKitKills(String kitName) {
        kitKills.merge(kitName.toLowerCase(), 1, Integer::sum);
    }

    public void addCombatTag(){
        SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().put(uuid, System.currentTimeMillis() + (15 * 1000));
    }

    public Boolean isCombatTagged(){
        return SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().containsKey(uuid) && SoupPvP.getInstance().getCombatTagsHandler().getCombatTags().get(uuid) - System.currentTimeMillis() > 0;
    }

    public double getWinPercent() {
        if (this.wagersWon + this.wagersLost == 0 || this.wagersWon == 0) {
            return 0;
        }
        return Double.parseDouble(new DecimalFormat("##").format(this.wagersWon * 100L / (this.wagersWon + this.wagersLost)));
    }

    public KitCategory getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(KitCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

}
