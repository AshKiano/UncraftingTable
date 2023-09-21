package com.ashkiano.uncraftingtable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UncraftingTable extends JavaPlugin implements Listener, CommandExecutor {

    private final String UCT_NAME = "Uncrafting Table";
    private Map<Material, ItemStack[]> uncraftingRecipes;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        Inventory uctInventory = Bukkit.createInventory(null, 18, UCT_NAME);

        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.setDisplayName("Insert Item");
        redPane.setItemMeta(redMeta);
        for (int i = 0; i < 16; i++) {
            uctInventory.setItem(i, redPane);
        }

        ItemStack greenPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta greenMeta = greenPane.getItemMeta();
        greenMeta.setDisplayName("Uncraft Item");
        greenPane.setItemMeta(greenMeta);
        uctInventory.setItem(16, greenPane);

        player.openInventory(uctInventory);

        return true;
    }

    @Override
    public void onEnable() {
        this.getCommand("uncrafting").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        Metrics metrics = new Metrics(this, 19550);

        uncraftingRecipes = new HashMap<>();

        for (Material material : Material.values()) {
            List<Recipe> recipes = Bukkit.getRecipesFor(new ItemStack(material));

            for (Recipe recipe : recipes) {
                ItemStack[] ingredientsArray = new ItemStack[9];
                if (recipe instanceof ShapedRecipe) {
                    ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                    Map<Character, ItemStack> ingredientMap = shapedRecipe.getIngredientMap();

                    String[] shape = shapedRecipe.getShape();
                    int arrayIndex = 0;

                    for (String row : shape) {
                        for (char c : row.toCharArray()) {
                            ingredientsArray[arrayIndex] = ingredientMap.get(c);
                            arrayIndex++;
                        }
                    }

                } else if (recipe instanceof ShapelessRecipe) {
                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    List<ItemStack> ingredients = shapelessRecipe.getIngredientList();
                    for (int i = 0; i < ingredients.size() && i < 9; i++) {
                        ingredientsArray[i] = ingredients.get(i);
                    }
                }
                uncraftingRecipes.put(material, ingredientsArray);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(UCT_NAME)) return;

        Inventory clickedInv = event.getClickedInventory();

        if (clickedInv == null) return;

        if (clickedInv.equals(event.getWhoClicked().getInventory())) return;

        ItemStack clickedItem = clickedInv.getItem(event.getSlot());
        if (event.getSlot() == 16 || (clickedItem != null && clickedItem.getType() == Material.RED_STAINED_GLASS_PANE)) {
            event.setCancelled(true);
        }

        if (event.getSlot() == 16) {
            ItemStack targetItem = clickedInv.getItem(17);

            if (targetItem != null && uncraftingRecipes.containsKey(targetItem.getType())) {
                int amount = targetItem.getAmount();
                ItemStack[] ingredients = uncraftingRecipes.get(targetItem.getType());
                for (int i = 0; i < ingredients.length; i++) {
                    if (ingredients[i] != null) {
                        ItemStack ingredientClone = ingredients[i].clone();
                        ingredientClone.setAmount(ingredientClone.getAmount() * amount);
                        clickedInv.setItem(i, ingredientClone);
                    }
                }
                clickedInv.setItem(17, null);
            }
        }
    }
}
