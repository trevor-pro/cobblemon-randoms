package pro.trevor.cobblemonrandoms.command.permission;

import net.minecraft.server.command.ServerCommandSource;
import pro.trevor.cobblemonrandoms.util.Res;

import java.util.function.Predicate;

public record Permission(String identifier, PermissionLevel level) {

    public String qualifiedIdentifier() {
        return Res.MOD_ID + "." + identifier;
    }


    public Predicate<ServerCommandSource> hasPermission() {
        return (s) -> s.hasPermissionLevel(level.value);
    }

}
