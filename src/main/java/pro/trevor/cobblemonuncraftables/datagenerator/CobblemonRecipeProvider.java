package pro.trevor.cobblemonuncraftables.datagenerator;

import com.cobblemon.mod.common.CobblemonItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class CobblemonRecipeProvider extends FabricRecipeProvider {

    public CobblemonRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter recipeExporter) {
        ShapedRecipeJsonBuilder
                .create(RecipeCategory.MISC, CobblemonItems.ABILITY_PATCH)
                .input('D', Items.DIAMOND)
                .input('I', Items.IRON_INGOT)
                .input('G', Items.GOLD_BLOCK)
                .pattern("DDD")
                .pattern("DGD")
                .pattern("IDD")
                .group("cobblemon")
                .criterion(hasItem(Items.DIAMOND), conditionsFromItem(Items.DIAMOND))
                .offerTo(recipeExporter);
    }
}
