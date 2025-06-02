package pro.trevor.cobblemonrandoms.gts;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import pro.trevor.cobblemonrandoms.util.JsonCodec;
import pro.trevor.cobblemonrandoms.util.Log;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Gts {

    // Completed trades, used to distribute pokemon to trade initiator if they are offline when the trade occurs
    private final Map<UUID, Pokemon> completedTrades;

    // Trade queries by the UUID of the requesting player
    private final Map<UUID, TradeQuery> queries;

    public Gts() {
        this.completedTrades = new HashMap<>();
        this.queries = new HashMap<>();
    }

    public boolean hasQuery(UUID uuid) {
        return queries.containsKey(uuid);
    }

    public boolean hasCompletedTrade(UUID uuid) {
        return completedTrades.containsKey(uuid);
    }

    public Pokemon getCompletedTrade(UUID uuid) {
        return completedTrades.get(uuid);
    }

    // Forcibly emplace a completed trade without any trade occurring
    public void forceCompleteTrade(UUID uuid, Pokemon pokemon) {
        completedTrades.put(uuid, pokemon);
    }

    // Forcibly remove a completed trade without distributing the pokemon
    public void forceRemoveCompletedTrade(UUID uuid) {
        completedTrades.remove(uuid);
    }

    public TradeQuery getQuery(UUID uuid) {
        return queries.get(uuid);
    }

    public List<TradeQuery> allQueries() {
        return new ArrayList<>(queries.values());
    }

    public List<TradeQuery> filterQueries(List<Predicate<Pokemon>> predicates) {
        Stream<TradeQuery> stream = queries.values().stream();
        for (Predicate<Pokemon> predicate : predicates) {
            stream = stream.filter((q) -> predicate.test(q.getTradePokemon()));
        }

        return stream.toList();
    }

    public boolean putQuery(ServerPlayerEntity initiator, TradeQuery query) {
        if (queries.containsKey(initiator.getUuid())) {
            return false;
        }

        if (removePokemonFrom(initiator, query.getTradePokemon())) {
            queries.put(initiator.getUuid(), query);
            return true;
        }
        return false;
    }


    // Emplace a query without needing access to a ServerPlayerEntity or removing a pokemon
    public boolean forcePutQuery(UUID initiator, TradeQuery query) {
        if (queries.containsKey(initiator)) {
            return false;
        }

        queries.put(initiator, query);
        return true;
    }

    public boolean removeQuery(ServerPlayerEntity initiator) {
        UUID uuid = initiator.getUuid();
        if (!queries.containsKey(uuid)) {
            return true;
        }

        TradeQuery query = queries.get(uuid);
        if (givePokemonTo(initiator, query.getTradePokemon())) {
            queries.remove(uuid);
            return true;
        }

        return false;
    }

    public boolean forceRemoveQuery(UUID initiator) {
        if (!queries.containsKey(initiator)) {
            return true;
        }

        queries.remove(initiator);

        return true;
    }



    public boolean doTrade(ServerPlayerEntity responder, UUID initiatorUuid, Pokemon pokemonToTrade) {
        TradeQuery query = queries.get(initiatorUuid);
        Pokemon initiatorPokemon = query.getTradePokemon();

        boolean removalSuccess = removePokemonFrom(responder, pokemonToTrade);
        if (!removalSuccess) {
            return false;
        }

        boolean tradeSuccess = givePokemonTo(responder, initiatorPokemon);
        if (!tradeSuccess) {
            return false;
        }

        completedTrades.put(initiatorUuid, pokemonToTrade);
        queries.remove(initiatorUuid);
        return true;
    }

    public boolean attemptToDistributeToPlayer(ServerPlayerEntity player) {
        if (!completedTrades.containsKey(player.getUuid())) {
            return true;
        }

        Pokemon toReceive = completedTrades.remove(player.getUuid());
        boolean givePokemonSuccess = givePokemonTo(player, toReceive);
        if (!givePokemonSuccess) {
            return false;
        }

        Log.LOGGER.info("Successfully distributed {} to {}", toReceive.getSpecies().getName(), player.getName());
        player.sendMessage(Text.literal(String.format("Your GTS trade succeeded! Say hello to your new %s!", toReceive.getSpecies().getName())));

        return true;
    }

    private boolean removePokemonFrom(ServerPlayerEntity player, Pokemon pokemon) {
        PartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        Pokemon fromParty = partyStore.get(pokemon.getUuid());

        if (fromParty != null) {
            return Cobblemon.INSTANCE.getStorage().getParty(player).remove(pokemon);
        }

        PCStore pcStore = Cobblemon.INSTANCE.getStorage().getPC(player);
        Pokemon fromPC = pcStore.get(pokemon.getUuid());

        if (fromPC != null) {
            return Cobblemon.INSTANCE.getStorage().getPC(player).remove(pokemon);
        }

        return false;
    }

    private boolean givePokemonTo(ServerPlayerEntity player, Pokemon pokemon) {
        PartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        PartyPosition partyPosition = partyStore.getFirstAvailablePosition();
        if (partyPosition != null) {
            partyStore.set(partyPosition, pokemon);
            return true;
        }

        PCStore pcStore = Cobblemon.INSTANCE.getStorage().getPC(player);
        PCPosition position = pcStore.getFirstAvailablePosition();
        if (position != null) {
            pcStore.set(position, pokemon);
            return true;
        }

        return false;
    }

    public String toSimpleString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Queries [");

        // Start the count at 1 to skip the last entry when adding commas
        int count = 1;
        for (Map.Entry<UUID, TradeQuery> query : queries.entrySet()) {
            sb.append('{');
            sb.append(query.getKey().toString());
            sb.append(' ');
            sb.append(query.getValue().getTradePokemon().getSpecies().getName());
            sb.append('}');

            if (++count < queries.size()) {
                sb.append(',');
            }
        }

        sb.append(']');

        sb.append('\n');

        sb.append("Completed [");

        // Start the count at 1 to skip the last entry when adding commas
        count = 1;
        for (Map.Entry<UUID, Pokemon> query : completedTrades.entrySet()) {
            sb.append('{');
            sb.append(query.getKey().toString());
            sb.append(": ");
            sb.append(query.getValue().getSpecies().getName());
            sb.append('}');

            if (++count < queries.size()) {
                sb.append(',');
            }
        }

        sb.append(']');

        return sb.toString();
    }

    public static class GtsCodec extends JsonCodec<Gts> {
        public static JsonObject toJson(Gts gts) {
            DynamicRegistryManager registryManager = DynamicRegistryManager.EMPTY;

            JsonObject output = new JsonObject();

            JsonArray queriesArray = new JsonArray();
            for (Map.Entry<UUID, TradeQuery> query : gts.queries.entrySet()) {
                JsonObject queryObject = new JsonObject();
                queryObject.addProperty("uuid", query.getKey().toString());
                queryObject.add("query", TradeQuery.toJson(query.getValue(), registryManager));
                queriesArray.add(queryObject);
            }

            JsonArray completedArray = new JsonArray();
            for (Map.Entry<UUID, Pokemon> completed : gts.completedTrades.entrySet()) {
                JsonObject completedObject = new JsonObject();
                completedObject.addProperty("uuid", completed.getKey().toString());
                completedObject.add("completed", completed.getValue().saveToJSON(registryManager, new JsonObject()));
                completedArray.add(completedObject);
            }

            output.add("queries", queriesArray);
            output.add("completed", completedArray);

            return output;
        }

        public static Gts fromJson(JsonObject json) {

            DynamicRegistryManager registryManager = DynamicRegistryManager.EMPTY;

            Gts result = new Gts();

            JsonArray queriesArray = json.getAsJsonArray("queries");
            JsonArray completedArray = json.getAsJsonArray("completed");

            for (JsonObject queryObject : queriesArray.asList().stream().map(JsonElement::getAsJsonObject).toList()) {
                String uuidString = queryObject.get("uuid").getAsString();
                UUID uuid = UUID.fromString(uuidString);

                JsonObject queryJson = queryObject.getAsJsonObject("query");
                TradeQuery query =  TradeQuery.fromJson(queryJson, registryManager);

                result.queries.put(uuid, query);
            }

            for (JsonObject completedObject : completedArray.asList().stream().map(JsonElement::getAsJsonObject).toList()) {
                String uuidString = completedObject.get("uuid").getAsString();
                UUID uuid = UUID.fromString(uuidString);

                JsonObject completedJson = completedObject.getAsJsonObject("completed");
                Pokemon completedTradePokemon = Pokemon.Companion.loadFromJSON(registryManager, completedJson);

                result.completedTrades.put(uuid, completedTradePokemon);
            }

            return result;
        }

        public GtsCodec() {
            super(Gts.class, GtsCodec::toJson, GtsCodec::fromJson);
        }
    }

}
