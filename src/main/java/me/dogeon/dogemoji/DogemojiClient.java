package me.dogeon.dogemoji;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class DogemojiClient implements ClientModInitializer {
    EmojiStorage storage;

    @Override
    public void onInitializeClient() {
        storage = new EmojiStorage();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {

                @Override
                public void reload(ResourceManager manager) {
                    storage.beginUpdate();
                    for (Identifier id : manager.findResources("emojis", p -> p.endsWith(".json")))
                        storage.load(manager, id);
                    storage.endUpdate();
                    Dogemoji.LOGGER.info(storage.query("s"));
                }

                @Override
                public Identifier getFabricId() { return new Identifier(Dogemoji.MOD_ID, "emojis"); }
            });
    }
}
