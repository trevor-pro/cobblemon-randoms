package pro.trevor.cobblemonuncraftables.gts;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
import net.minecraft.registry.DynamicRegistryManager;

public class TradeQuery {

    private final Pokemon tradePokemon;
    private final Species desiredSpecies;

    public TradeQuery(Pokemon tradePokemon, Species desiredSpecies) {
        this.tradePokemon = tradePokemon;
        this.desiredSpecies = desiredSpecies;
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
        output.add("pokemon", pokemonJson);
        output.addProperty("ds_dexnum", tradeQuery.desiredSpecies.getNationalPokedexNumber());
        output.addProperty("ds_namespace", tradeQuery.desiredSpecies.resourceIdentifier.getNamespace());
        return output;
    }

    public static TradeQuery fromJson(JsonObject json, DynamicRegistryManager registryManager) {
        JsonObject pokemonJson = json.get("pokemon").getAsJsonObject();
        int desiredSpeciesDexNumber = json.get("ds_dexnum").getAsInt();
        String desiredSpeciesNamespace = json.get("ds_namespace").getAsString();
        Pokemon tradePokemon = Pokemon.Companion.loadFromJSON(registryManager, pokemonJson);
        Species desiredSpecies = PokemonSpecies.INSTANCE.getByPokedexNumber(desiredSpeciesDexNumber, desiredSpeciesNamespace);
        return new TradeQuery(tradePokemon, desiredSpecies);
    }
}
