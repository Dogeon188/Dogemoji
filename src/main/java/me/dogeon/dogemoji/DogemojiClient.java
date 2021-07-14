package me.dogeon.dogemoji;

import me.dogeon.dogemoji.emoji.EmojiStorage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class DogemojiClient implements ClientModInitializer {
    public static EmojiStorage storage;

    @Override
    public void onInitializeClient() {
        storage = new EmojiStorage();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {

                @Override
                public void reload(ResourceManager manager) {
                    Log.LOGGER.info("Loading emoji resources...");
                    storage.beginUpdate();
                    for (Identifier id : manager.findResources("emojis", p -> p.endsWith(".json"))) {
                        storage.load(manager, id);
                    }
                    storage.endUpdate();
                    Log.LOGGER.info("Emoji resources loaded.");
                }

                @Override
                public Identifier getFabricId() {
                    return new Identifier(Reference.MOD_ID, "emojis");
                }
            });
    }
}
