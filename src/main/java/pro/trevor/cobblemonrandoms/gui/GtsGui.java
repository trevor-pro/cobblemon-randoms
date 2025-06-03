package pro.trevor.cobblemonrandoms.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import pro.trevor.cobblemonrandoms.CobblemonRandoms;
import pro.trevor.cobblemonrandoms.gts.Gts;
import pro.trevor.cobblemonrandoms.gts.TradeQuery;
import pro.trevor.cobblemonrandoms.util.PlayerHead;

import java.util.ArrayList;
import java.util.List;

public class GtsGui {

    public static void openGtsBasePage(ServerPlayerEntity player, boolean filter) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();

        List<Button> buttons = new ArrayList<>();

        for (TradeQuery query : gts.allQueries()) {
            Pokemon tradePokemon = query.getTradePokemon();
            Species desiredSpecies = query.getDesiredSpecies();


            if (filter) {
                boolean shouldSkip = true;

                for (Pokemon playerPokemon : Cobblemon.INSTANCE.getStorage().getParty(player)) {
                    if (playerPokemon.getSpecies().equals(desiredSpecies)) {
                        shouldSkip = false;
                        break;
                    }
                }

                if (shouldSkip) {
                    continue;
                }
            }

            buttons.add(pokemonToButtonBuilder(tradePokemon, List.of(Text.literal("LF: " + query.getDesiredSpecies().getName())), List.of())
                    .onClick(() -> openGtsTradePage(player, query, filter))
                    .build());
        }

        GooeyButton filterButton = GooeyButton.builder()
                .display(new ItemStack(Items.DIAMOND))
                .with(DataComponentTypes.ITEM_NAME, Text.literal(filter ? "Don't Filter" : "Filter"))
                .onClick(() -> openGtsBasePage(player, !filter))
                .build();

        ChestTemplate template = ChestTemplate.builder(6)
                .rectangleFromList(0, 0, 5, 9, buttons)
                .set(5, 4, filterButton)
                .build();

        GooeyPage page = GooeyPage.builder()
                .template(template)
                .title("GTS")
                .build();

        UIManager.openUIForcefully(player, page);
    }

    public static void openGtsTradePage(ServerPlayerEntity player, TradeQuery query, boolean filter) {
        Pokemon tradePokemon = query.getTradePokemon();
        Species desiredSpecies = query.getDesiredSpecies();

        boolean hasPokemonToTrade = false;

        for (Pokemon playerPokemon : Cobblemon.INSTANCE.getStorage().getParty(player)) {
            if (playerPokemon.getSpecies().equals(desiredSpecies)) {
                hasPokemonToTrade = true;
                break;
            }
        }

        Pair<ItemStack, String> headResult = PlayerHead.getHead(query.getInitiator());
        ItemStack head = headResult.getLeft();
        String name = headResult.getRight();

        GooeyButton trainerButton = GooeyButton.builder()
                .display(head)
                .with(DataComponentTypes.ITEM_NAME, Text.literal("Owner: " + name)
                        .withColor(0xFFFFFF))
                .build();

        GooeyButton pokemonButton = pokemonToButtonBuilder(tradePokemon, List.of(), List.of()).build();

        GooeyButton tradeButton;
        if (hasPokemonToTrade) {
            tradeButton = GooeyButton.builder()
                    .display(new ItemStack(Items.DIAMOND))
                    .with(DataComponentTypes.ITEM_NAME, Text.literal("Select a Pokemon to trade"))
                    .onClick(() -> openGtsTradeSelectPage(player, query, filter))
                    .build();
        } else {
            tradeButton = GooeyButton.builder()
                    .display(new ItemStack(Items.ANVIL))
                    .with(DataComponentTypes.ITEM_NAME, Text.literal("You have no matching Pokemon to trade"))
                    .build();
        }


        GooeyButton backButton = GooeyButton.builder()
                .display(new ItemStack(Items.BARRIER))
                .with(DataComponentTypes.ITEM_NAME, Text.literal("Back to GTS")
                        .withColor(0xFFFFFF))
                .onClick(() -> openGtsBasePage(player, filter))
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .set(0, 3, trainerButton)
                .set(0, 5, pokemonButton)
                .set(2, 3, backButton)
                .set(2, 5, tradeButton)
                .build();

        GooeyPage page = GooeyPage.builder()
                .template(template)
                .title(tradePokemon.getSpecies().getName())
                .build();

        UIManager.openUIForcefully(player, page);
    }

    private static void openGtsTradeSelectPage(ServerPlayerEntity player, TradeQuery query, boolean filter) {
        Species desiredSpecies = query.getDesiredSpecies();

        List<Button> buttons = new ArrayList<>();
        for (Pokemon playerPokemon : Cobblemon.INSTANCE.getStorage().getParty(player)) {
            if (playerPokemon.getSpecies().equals(desiredSpecies)) {
                buttons.add(pokemonToButtonBuilder(playerPokemon, List.of(), List.of())
                        .onClick(() -> openGtsConfirmPage(player, query, filter, playerPokemon))
                        .build());
            }
        }

        GooeyButton backButton = GooeyButton.builder()
                .display(new ItemStack(Items.BARRIER))
                .with(DataComponentTypes.ITEM_NAME, Text.literal("Back to trade menu")
                        .withColor(0xFFFFFF))
                .onClick(() -> openGtsTradePage(player, query, filter))
                .build();

        ChestTemplate template = ChestTemplate.builder(1)
                .fillFromList(buttons)
                .set(0, 8, backButton)
                .build();

        GooeyPage page = GooeyPage.builder()
                .template(template)
                .title("Select a Pokemon to trade")
                .build();

        UIManager.openUIForcefully(player, page);
    }

    private static void openGtsConfirmPage(ServerPlayerEntity player, TradeQuery query, boolean filter, Pokemon playerPokemon) {
        Gts gts = CobblemonRandoms.GTS_MANAGER.gtsStorage.data();

        GooeyButton tradePokemon = pokemonToButtonBuilder(playerPokemon, List.of(Text.literal("To Trade")), List.of()).build();

        GooeyButton receivePokemon = pokemonToButtonBuilder(query.getTradePokemon(), List.of(Text.literal("To Receive")), List.of()).build();

        GooeyButton backButton = GooeyButton.builder()
                .display(new ItemStack(Items.BARRIER))
                .with(DataComponentTypes.ITEM_NAME, Text.literal("Back to trade menu")
                        .withColor(0xFFFFFF))
                .onClick(() -> openGtsTradeSelectPage(player, query, filter))
                .build();

        GooeyButton confirmButton = GooeyButton.builder()
                .display(new ItemStack(Items.DIAMOND))
                .with(DataComponentTypes.ITEM_NAME, Text.literal("Confirm"))
                .onClick(() -> {
                    UIManager.closeUI(player);
                    gts.doTrade(player, query.getInitiator(), playerPokemon);
                })
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .set(0, 3, tradePokemon)
                .set(0, 5, receivePokemon)
                .set(2, 3, backButton)
                .set(2, 5, confirmButton)
                .build();

        GooeyPage page = GooeyPage.builder()
                .template(template)
                .title("Confirm your trade")
                .build();

        UIManager.openUIForcefully(player, page);
    }

    private static GooeyButton.Builder pokemonToButtonBuilder(Pokemon pokemon, List<Text> prefixLore, List<Text> suffixLore) {
        List<Text> lore = new ArrayList<>(prefixLore);

        if (pokemon.getNickname() != null) {
            lore.add(Text.literal("Nick: " + pokemon.getNickname()));
        }

        lore.addAll(List.of(
                Text.literal("Lv: " + pokemon.getLevel()),
                Text.literal("Shiny: " + (pokemon.getShiny() ? "yes" : "no"))));

        lore.addAll(suffixLore);

        return GooeyButton.builder()
                .display(new ItemStack(pokemon.getCaughtBall().item))
                .with(DataComponentTypes.ITEM_NAME, Text.literal(pokemon.getSpecies().getName())
                        .withColor(0xFFFFFF))
                .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .with(DataComponentTypes.LORE, new LoreComponent(lore));
    }

}
