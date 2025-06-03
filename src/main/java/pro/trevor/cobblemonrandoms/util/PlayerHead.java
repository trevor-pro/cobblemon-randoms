package pro.trevor.cobblemonrandoms.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.UserCache;
import pro.trevor.cobblemonrandoms.CobblemonRandoms;

import java.util.Optional;
import java.util.UUID;

public class PlayerHead {

    public static Pair<ItemStack, String> getHead(UUID uuid) {
        GameProfile profile = getGameProfile(uuid);
        if (profile == null) {
            return new Pair<>(new ItemStack(Items.PLAYER_HEAD), "Unknown Player");
        }

        String name = profile.getName();
        ItemStack headItemStack = new ItemStack(Items.PLAYER_HEAD);
        headItemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));

        return new Pair<>(headItemStack, name);
    }

    private static GameProfile getGameProfile(UUID uuid) {
        if (CobblemonRandoms.SERVER_INSTANCE == null) {
            return null;
        }

        UserCache userCache = CobblemonRandoms.SERVER_INSTANCE.getUserCache();
        if (userCache == null) {
            return null;
        }

        Optional<GameProfile> profile = userCache.getByUuid(uuid);
        if (profile.isPresent()) {
            return profile.get();
        } else {
            ProfileResult result = CobblemonRandoms.SERVER_INSTANCE.getSessionService().fetchProfile(uuid, false);
            if (result == null) {
                return null;
            }
            return result.profile();
        }
    }

}
