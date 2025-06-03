package pro.trevor.cobblemonrandoms.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import pro.trevor.cobblemonrandoms.CobblemonRandoms;

public class Callbacks {

    public static void registerAllCallbacks() {
        registerServerReferenceCallback();
    }

    public static void registerServerReferenceCallback() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> CobblemonRandoms.SERVER_INSTANCE = server);
    }

}
