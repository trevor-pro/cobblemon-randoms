package pro.trevor.cobblemonrandoms;

import net.fabricmc.api.ModInitializer;
import pro.trevor.cobblemonrandoms.gts.GtsManager;

public class CobblemonRandoms implements ModInitializer {

    public static GtsManager GTS_MANAGER;

    @Override
    public void onInitialize() {
        GTS_MANAGER = new GtsManager();
    }
}
