package pro.trevor.cobblemonrandoms.gts;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
import net.minecraft.registry.DynamicRegistryManager;

import java.util.UUID;

public class TradeQuery {

    private final UUID initiator;
    private final Pokemon tradePokemon;
    private final Species desiredSpecies;

    public TradeQuery(UUID initiator, Pokemon tradePokemon, Species desiredSpecies) {
        this.initiator = initiator;
        this.tradePokemon = tradePokemon;
        this.desiredSpecies = desiredSpecies;
    }

    public UUID getInitiator() {
        return initiator;
    }

    public Pokemon getTradePokemon() {
        return tradePokemon;
    }

    public Species getDesiredSpecies() {
        return desiredSpecies;
    }

    public static JsonObject toJson(TradeQuery tradeQuery, DynamicRegistryManager registryManager) {
        JsonObject output = new JsonObject();
        JsonObject pokemonJson = tradeQuery.tradePokemon.saveToJSON(registryManager, new JsonObject());
        output.addProperty("initiator", tradeQuery.initiator.toString());
        output.add("pokemon", pokemonJson);
        output.addProperty("ds_dexnum", tradeQuery.desiredSpecies.getNationalPokedexNumber());
        output.addProperty("ds_namespace", tradeQuery.desiredSpecies.resourceIdentifier.getNamespace());
        return output;
    }

    public static TradeQuery fromJson(JsonObject json, DynamicRegistryManager registryManager) {
        UUID initiator = UUID.fromString(json.get("initiator").getAsString());
        JsonObject pokemonJson = json.get("pokemon").getAsJsonObject();
        int desiredSpeciesDexNumber = json.get("ds_dexnum").getAsInt();
        String desiredSpeciesNamespace = json.get("ds_namespace").getAsString();
        Pokemon tradePokemon = Pokemon.Companion.loadFromJSON(registryManager, pokemonJson);
        Species desiredSpecies = PokemonSpecies.INSTANCE.getByPokedexNumber(desiredSpeciesDexNumber, desiredSpeciesNamespace);
        return new TradeQuery(initiator, tradePokemon, desiredSpecies);
    }
}
