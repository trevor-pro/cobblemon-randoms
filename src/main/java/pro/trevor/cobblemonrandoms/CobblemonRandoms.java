package pro.trevor.cobblemonrandoms;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import pro.trevor.cobblemonrandoms.command.Registry;
import pro.trevor.cobblemonrandoms.gts.GtsManager;
import pro.trevor.cobblemonrandoms.util.Callbacks;

public class CobblemonRandoms implements ModInitializer {

    public static GtsManager GTS_MANAGER;
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        Registry.registerCommands();
        Callbacks.registerAllCallbacks();
        GTS_MANAGER = new GtsManager();
    }
}
