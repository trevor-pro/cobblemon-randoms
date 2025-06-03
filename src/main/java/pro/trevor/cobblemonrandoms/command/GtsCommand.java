package pro.trevor.cobblemonrandoms.command;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import pro.trevor.cobblemonrandoms.CobblemonRandoms;
import pro.trevor.cobblemonrandoms.command.permission.Permissions;
import pro.trevor.cobblemonrandoms.gts.Gts;
import pro.trevor.cobblemonrandoms.gts.TradeQuery;
import pro.trevor.cobblemonrandoms.gui.GtsGui;
import pro.trevor.cobblemonrandoms.util.Log;

import java.util.UUID;

public class GtsCommand implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("gts")
                .requires(Permissions.GTS.hasPermission())
                .then(CommandManager.literal("check")
                        .requires(Permissions.GTS_CHECK.hasPermission())
                        .executes(GtsCommand::executeGtsCheckCommand))
                .then(CommandManager.literal("cancel")
                        .requires(Permissions.GTS_CANCEL.hasPermission())
                        .executes(GtsCommand::executeGtsCancelCommand))
                .then(CommandManager.literal("search")
                        .requires(Permissions.GTS_SEARCH.hasPermission())
                        .executes(GtsCommand::executeGtsSearchBasicCommand))
                .then(CommandManager.literal("trade")
                        .requires(Permissions.GTS_QUERY.hasPermission())
                        .then(CommandManager.argument("party_slot", PartySlotArgumentType.Companion.partySlot())
                                .then(CommandManager.argument("desired_species", SpeciesArgumentType.Companion.species())
                                        .executes(GtsCommand::executeGtsTradeCommand))))
                .then(CommandManager.literal("test")
                        .requires(Permissions.GTS_TEST.hasPermission())
                        .then(CommandManager.literal("receive")
                                .executes(GtsCommand::executeGtsTestReceiveCommand))
                        .then(CommandManager.literal("query").executes(GtsCommand::executeGtsTestQueryCommand))
                )
        );
    }

    public static int executeGtsCheckCommand(CommandContext<ServerCommandSource> context) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Failed to get player"), false);
            return 0;
        }

        TradeQuery query = gts.getQuery(player.getUuid());

        if (query == null) {
            context.getSource().sendFeedback(() -> Text.literal("You have no active trades"), false);
            return 1;
        }

        Pokemon tradePokemon = query.getTradePokemon();
        Species desiredSpecies = query.getDesiredSpecies();
        context.getSource().sendFeedback(() -> Text.literal(String.format("You have a level %d '%s' up for trade for any '%s'", tradePokemon.getLevel(), tradePokemon.getSpecies().getName(), desiredSpecies.getName())), false);
        return 1;
    }

    public static int executeGtsCancelCommand(CommandContext<ServerCommandSource> context) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Failed to get player"), false);
            return 0;
        }

        TradeQuery query = gts.getQuery(player.getUuid());

        if (query == null) {
            context.getSource().sendFeedback(() -> Text.literal("You have no active trades"), false);
            return 1;
        }

        if (!gts.removeQuery(player)) {
            context.getSource().sendFeedback(() -> Text.literal("Unable to cancel your existing query; your party and PC are probably full"), false);
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal(String.format("Cancelled your active query; returning your '%s'", query.getTradePokemon().getSpecies().getName())), false);
        return 1;
    }

    public static int executeGtsTradeCommand(CommandContext<ServerCommandSource> context) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Failed to get player"), false);
            return 0;
        }

        TradeQuery query = gts.getQuery(player.getUuid());

        if (query != null) {
            context.getSource().sendFeedback(() -> Text.literal("You already have an active trade"), false);
            return 1;
        }

        Pokemon tradePokemon = PartySlotArgumentType.Companion.getPokemonOf(context, "party_slot", player);
        Species desiredSpecies = SpeciesArgumentType.Companion.getPokemon(context, "desired_species");
        TradeQuery tradeQuery = new TradeQuery(player.getUuid(), tradePokemon, desiredSpecies);
        gts.putQuery(player, tradeQuery);

        context.getSource().sendFeedback(() -> Text.literal(String.format("Created a trade looking for '%s'; sending '%s' to the GTS", tradeQuery.getDesiredSpecies().getName(), tradeQuery.getTradePokemon().getSpecies().getName())), false);
        return 1;
    }

    public static int executeGtsTestReceiveCommand(CommandContext<ServerCommandSource> context) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Failed to get player"), false);
            return 0;
        }

        UUID uuid = player.getUuid();

        if (gts.hasCompletedTrade(uuid)) {
            context.getSource().sendFeedback(() -> Text.literal("You already have a pending completed trade pokemon; your party and PC are probably full"), false);
            return 0;
        }

        Species receivedSpecies = PokemonSpecies.INSTANCE.random();

        Pokemon recievedPokemon = receivedSpecies.create(10);

        gts.forceCompleteTrade(uuid, recievedPokemon);

        CobblemonRandoms.GTS_MANAGER.gtsStorage.flagForSaving();

        Log.LOGGER.info(gts.toSimpleString());

        context.getSource().sendFeedback(() -> Text.literal(String.format("Set pending completed trade of a '%s'", receivedSpecies.getName())), false);

        return 1;
    }

    public static int executeGtsTestQueryCommand(CommandContext<ServerCommandSource> context) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Failed to get player"), false);
            return 0;
        }

        UUID uuid = player.getUuid();

        if (gts.hasQuery(uuid)) {
            TradeQuery query = gts.getQuery(uuid);
            if (gts.removeQuery(player)) {
                context.getSource().sendFeedback(() -> Text.literal(String.format("Returned your existing trade pokemon '%s'", query.getTradePokemon().getSpecies().getName())), false);
            } else {
                context.getSource().sendFeedback(() -> Text.literal(String.format("Failed to return your existing trade pokemon '%s'; your party and PC are probably full", query.getTradePokemon().getSpecies().getName())), false);
                return 0;
            }
        }

        Species tradeSpecies = PokemonSpecies.INSTANCE.random();
        Species requestSpecies = PokemonSpecies.INSTANCE.random();

        Pokemon tradePokemon = tradeSpecies.create(10);
        TradeQuery query = new TradeQuery(player.getUuid(), tradePokemon, requestSpecies);

        gts.forcePutQuery(uuid, query);

        CobblemonRandoms.GTS_MANAGER.gtsStorage.flagForSaving();

        Log.LOGGER.info(gts.toSimpleString());

        context.getSource().sendFeedback(() -> Text.literal(String.format("Set query trade of a '%s' for a '%s'", tradeSpecies.getName(), requestSpecies.getName())), false);

        return 1;
    }


    public static int executeGtsSearchBasicCommand(CommandContext<ServerCommandSource> context) {
        GtsGui.openGtsBasePage(context.getSource().getPlayer(), false);
        return 1;
    }
}
