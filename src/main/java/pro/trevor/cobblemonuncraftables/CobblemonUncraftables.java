package pro.trevor.cobblemonuncraftables;

import net.fabricmc.api.ModInitializer;
import pro.trevor.cobblemonuncraftables.gts.GtsManager;

public class CobblemonUncraftables implements ModInitializer {

    public static GtsManager GTS_MANAGER;

    @Override
    public void onInitialize() {
        GTS_MANAGER = new GtsManager();
    }
}
