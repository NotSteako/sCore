package kami.gg.souppvp.coinflip;

import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.coinflip.menu.wager.LoserMenu;
import kami.gg.souppvp.coinflip.menu.wager.WinnerMenu;
import kami.gg.souppvp.profile.Profile;
import kami.gg.souppvp.util.CC;
import kami.gg.souppvp.util.TaskUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
public class CoinFlip {

    private UUID creator;
    private Integer amount;
    private UUID opponent;
    private UUID winner;
    private UUID loser;

    public CoinFlip(UUID creator, Integer amount){
        this.creator = creator;
        this.amount = amount;
        this.opponent = null;
        this.winner = null;
        this.loser = null;
        SoupPvP.getInstance().getCoinFlipsHandler().getCoinFlips().add(this);
    }

    public void start(){

        Player creator = Bukkit.getPlayer(this.creator);
        Player opponent = Bukkit.getPlayer(this.opponent);

        List<UUID> creatorAndopponentUUIDs = new ArrayList<>();
        creatorAndopponentUUIDs.add(this.creator);
        creatorAndopponentUUIDs.add(this.opponent);

        Random random = new Random();
        int randomIndex = random.nextInt(creatorAndopponentUUIDs.size());

        Profile creatorProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(this.creator);
        Profile opponentProfile = SoupPvP.getInstance().getProfilesHandler().getProfileByUUID(this.opponent);

        if (creatorAndopponentUUIDs.get(randomIndex).equals(this.creator)){
            this.winner = this.creator;
            this.loser = this.opponent;

            creatorProfile.setCredits(creatorProfile.getCredits() + (amount * 2));
            creatorProfile.setTotalWagerGames(creatorProfile.getTotalWagerGames() + 1);
            creatorProfile.setWagersWon(creatorProfile.getWagersWon() + 1);

            opponentProfile.setCredits(opponentProfile.getCredits() - amount);
            opponentProfile.setTotalWagerGames(opponentProfile.getTotalWagerGames() + 1);
            opponentProfile.setWagersLost(opponentProfile.getWagersLost() + 1);

        } else {
            this.winner = this.opponent;
            this.loser = this.creator;

            opponentProfile.setCredits(opponentProfile.getCredits() + amount);
            opponentProfile.setTotalWagerGames(opponentProfile.getTotalWagerGames() + 1);
            opponentProfile.setWagersWon(opponentProfile.getWagersWon() + 1);

            creatorProfile.setTotalWagerGames(creatorProfile.getTotalWagerGames() + 1);
            creatorProfile.setWagersLost(creatorProfile.getWagersLost() + 1);
        }

        TaskUtil.runAsync(() -> {
            new WinnerMenu(this).openMenu(Bukkit.getPlayer(this.getWinner()));
            new LoserMenu(this).openMenu(Bukkit.getPlayer(this.getLoser()));
        });

        if (this.getAmount() > 1000) {
            Bukkit.getServer().broadcastMessage(CC.translate(""));
            Bukkit.getServer().broadcastMessage(CC.translate(Bukkit.getPlayer(this.getWinner()).getDisplayName() + " &ebeat &a" + Bukkit.getPlayer(this.getLoser()).getDisplayName() + " &ein a coinflip for &a" + this.getAmount() + " credits&e!"));
            Bukkit.getServer().broadcastMessage(CC.translate(""));
        }

        Bukkit.getPlayer(this.getWinner()).sendMessage(CC.translate("&aYou won the bet and were rewarded! &7(&a" + this.getAmount() + (this.getAmount() != 1 ? " credits&7)" : " credit&7)")));
        Bukkit.getPlayer(this.getLoser()).sendMessage(CC.translate("&cYou lost the bet and the wager. &7(&a" + this.getAmount() + (this.getAmount() != 1 ? " credits&7)" : " credit&7)")));

        TaskUtil.runLater(() -> {
            SoupPvP.getInstance().getCoinFlipsHandler().removeCoinFlip(this);
            SoupPvP.getInstance().getCoinFlipsHandler().getCoinFlips().remove(this);
        }, 10L);

    }

}
