package pro.trevor.cobblemonuncraftables.gts;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import pro.trevor.cobblemonuncraftables.util.Log;

import java.util.UUID;

public class GtsManager {

    private static final long TICKS_PER_SECOND = 20;

    public GtsStorage gtsStorage;

    public GtsManager() {
        gtsStorage = null;
        ServerWorldEvents.LOAD.register(this::onServerStart);
        ServerPlayerEvents.JOIN.register(this::onPlayerJoin);
        ServerTickEvents.START_WORLD_TICK.register(this::onWorldTick);
    }

    private void onServerStart(MinecraftServer server, ServerWorld world) {
        if (gtsStorage == null) {
            Log.LOGGER.info("Loading GTS storage");
            gtsStorage = GtsStorage.get(world);
        }
    }

    private void onPlayerJoin(ServerPlayerEntity player) {
        if (gtsStorage == null) {
            Log.LOGGER.warn("Gts is not instantiated on player join");
            gtsStorage = GtsStorage.get(player.getServerWorld());
        }

        boolean distributed = gtsStorage.data().attemptToDistributeToPlayer(player);
        if (!distributed) {
            Log.LOGGER.warn("Failed to distribute pokemon from GTS to player");
        }

        test(player);
    }

    private void onWorldTick(ServerWorld world) {
        long ticks = world.getTime();
        if (ticks % TICKS_PER_SECOND == 0) {
            onSecondTick(world);
        }
    }

    // not to be registered directly; to be used by onWorldTick to tick each second (20 ticks)
    private void onSecondTick(ServerWorld world) {
        world.getPlayers().forEach((player) -> gtsStorage.data().attemptToDistributeToPlayer(player));
        gtsStorage.flagForSaving();
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(CommandManager.literal("gts")
                    .then(CommandManager.argument("subcommand", StringArgumentType.string()))
                    .executes((context) -> {
                        return 1;
            }));
        }));
    }

    private void test(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        Species tradeSpecies = PokemonSpecies.INSTANCE.random();
        Species requestSpecies = PokemonSpecies.INSTANCE.random();
        Pokemon tradePokemon = tradeSpecies.create(10);
        TradeQuery query = new TradeQuery(tradePokemon, requestSpecies);
        gtsStorage.data().putForcedQuery(uuid, query);
        gtsStorage.data().forceCompleteTrade(uuid, tradeSpecies.create(10));
        gtsStorage.flagForSaving();

        Log.LOGGER.info(gtsStorage.data().toSimpleString());
        Log.LOGGER.info(gtsStorage.data().allQueries()
                .getFirst()
                .getTradePokemon()
                .saveToJSON(DynamicRegistryManager.EMPTY, new JsonObject())
                .toString()
        );
    }

}
