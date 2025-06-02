package pro.trevor.cobblemonrandoms.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Registry {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(new GtsCommand());
    }

}
