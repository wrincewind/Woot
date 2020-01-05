package ipsis.woot.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ipsis.woot.Woot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class DyeSqueezerRecipeSerializer<T extends DyeSqueezerRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    private final DyeSqueezerRecipeSerializer.IFactory<T> factory;

    public DyeSqueezerRecipeSerializer(DyeSqueezerRecipeSerializer.IFactory<T> factory) {
        this.factory = factory;
    }

    @Nullable
    @Override
    public T read(ResourceLocation recipeId, PacketBuffer buffer) {
        return null;
    }

    @Override
    public void write(PacketBuffer buffer, T recipe) {
    }

    @Override
    public T read(ResourceLocation recipeId, JsonObject json) {
        JsonElement jsonelement = (JsonElement)(JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient"));
        Ingredient ingredient = Ingredient.deserialize(jsonelement);

        int energy = JSONUtils.getInt(json, "energy", 100);
        int red = JSONUtils.getInt(json, "red", 0);
        int yellow = JSONUtils.getInt(json, "yellow", 0);
        int blue = JSONUtils.getInt(json, "blue", 0);
        int white = JSONUtils.getInt(json, "white", 0);

        // If we return null then the recipe wont be used but we can still use the load process
        return this.factory.create(recipeId, ingredient, energy, red, yellow, blue, white);
    }

    public interface IFactory<T extends DyeSqueezerRecipe> {
        T create(ResourceLocation resourceLocation, Ingredient ingredient, int energy, int red, int yellow, int blue, int white);
    }

    /*
    private ResourceLocation name;

    @Override
    public IRecipeSerializer<?> setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<IRecipeSerializer<?>> getRegistryType() {
        return DyeSqueezerRecipeSerializer.<IRecipeSerializer<?>>castClass(IRecipeSerializer.class);
    }

    @SuppressWarnings("unchecked") // Need this wrapper, because generics
    private static <G> Class<G> castClass(Class<?> cls) {
        return (Class<G>)cls;
    }

    @Override
    public T read(ResourceLocation recipeId, JsonObject json) {

        JsonElement jsonelement = (JsonElement)(JSONUtils.isJsonArray(json, "ingredient") ? JSONUtils.getJsonArray(json, "ingredient") : JSONUtils.getJsonObject(json, "ingredient"));
        Ingredient ingredient = Ingredient.deserialize(jsonelement);

        int energy = JSONUtils.getInt(json, "energy", 100);
        int red = JSONUtils.getInt(json, "red", 0);
        int yellow = JSONUtils.getInt(json, "yellow", 0);
        int blue = JSONUtils.getInt(json, "blue", 0);
        int white = JSONUtils.getInt(json, "white", 0);

//        DyeSqueezerRecipe.addRecipe(recipeId, ingredient, energy, red, yellow, blue, white);

        // If we return null then the recipe wont be used but we can still use the load process
        return (T) DyeSqueezerRecipe.dyeSqueezerRecipe(recipeId, ingredient, energy, red, yellow, blue, white);
    } */

}
