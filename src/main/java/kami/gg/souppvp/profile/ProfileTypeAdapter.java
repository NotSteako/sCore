package kami.gg.souppvp.profile;

import com.jonahseguin.drink.argument.CommandArg;
import com.jonahseguin.drink.exception.CommandExitMessage;
import com.jonahseguin.drink.parametric.DrinkProvider;
import kami.gg.souppvp.SoupPvP;
import kami.gg.souppvp.util.CC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ProfileTypeAdapter extends DrinkProvider<Profile> {

    @Override
    public boolean doesConsumeArgument() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Nullable
    @Override
    public Profile provide(@Nonnull CommandArg arg, @Nonnull List<? extends Annotation> annotations) throws CommandExitMessage {
        Profile profile = SoupPvP.getInstance().getProfilesHandler().getProfileByName(arg.get());
        if (profile == null){
            throw new CommandExitMessage(CC.translate("&cCouldn't resolve that player's name."));
        }
        return profile;
    }

    @Override
    public String argumentDescription() {
        return "profile";
    }

    @Override
    public List<String> getSuggestions(@Nonnull String prefix) {
        List<String> complete = new ArrayList<>();
        for (Profile profile : SoupPvP.getInstance().getProfilesHandler().getProfiles()){
            if (profile.getUsername().toUpperCase().startsWith(prefix.toUpperCase())){
                complete.add(profile.getUsername());
            }
        }
        return complete;
    }

}
