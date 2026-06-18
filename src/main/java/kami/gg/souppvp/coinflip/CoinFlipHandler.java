package kami.gg.souppvp.coinflip;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoinFlipHandler {

    @Getter
    public List<CoinFlip> coinFlips;

    public CoinFlipHandler(){
        this.coinFlips = new ArrayList<>();
    }

    public void addNewCoinFlip(CoinFlip coinFlip){
        coinFlips.add(coinFlip);
    }

    public void removeCoinFlip(CoinFlip coinFlip){
        coinFlips.remove(coinFlip);
    }

    public Boolean hasCoinFlipWager(UUID uuid){
        for (CoinFlip coinFlip : coinFlips){
            if (coinFlip.getCreator().equals(uuid)){
                return true;
            }
        }
        return false;
    }

    public CoinFlip getPlayerCoinFlip(UUID uuid){
        for (CoinFlip coinFlip : coinFlips){
            if (coinFlip.getCreator().equals(uuid)){
                return coinFlip;
            }
        }
        return null;
    }

}
