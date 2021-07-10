package me.dogeon.dogemoji.emoji;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.dogeon.dogemoji.Log;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EmojiStorage {

    private static JsonElement getFieldIfExists(JsonObject json, String field) throws EmojiParsingError {
        if (!json.has(field)) throw new EmojiParsingError("field \"" + field + "\" doesn't exist");
        return json.get(field);
    }

    static final class EmojiEntry {
        enum EmojiEntryType { IMAGE, TEXT }
        public EmojiEntryType type;
        public Identifier textureId;
        public String text;

        EmojiEntry(JsonObject json) throws EmojiParsingError {
            switch (getFieldIfExists(json, "type").getAsString()) {
                case "image" -> {
                    this.textureId = new Identifier(getFieldIfExists(json, "texture").getAsString());
                    this.type = EmojiEntryType.IMAGE;
                }
                case "text" -> {
                    this.text = getFieldIfExists(json, "text").getAsString();
                    this.type = EmojiEntryType.TEXT;
                }
                default -> throw new EmojiParsingError("unknown emoji type: " + json.get("type").getAsString());
            }
        }

        @Override
        public String toString() {
            return (type == EmojiEntryType.IMAGE) ? textureId.toString() : text;
        }
    }

    public PatriciaTrie<EmojiEntry> trie;
    public PatriciaTrie<EmojiEntry> tmpTrie;
    private static final JsonParser parser = new JsonParser();

    public EmojiStorage() {
        trie = new PatriciaTrie<>();
    }

    public void beginUpdate() {
        assert tmpTrie == null;
        tmpTrie = new PatriciaTrie<>();
    }

    public void load(ResourceManager manager, Identifier resourceId) {
        try (InputStream stream = manager.getResource(resourceId).getInputStream()) {
            JsonObject rawEmoji = (JsonObject) parser.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String name = getFieldIfExists(rawEmoji, "name").getAsString();
            EmojiEntry emojiEntry = new EmojiEntry((JsonObject) getFieldIfExists(rawEmoji, "emoji"));
            tmpTrie.put(name, emojiEntry);
        } catch (EmojiParsingError | InvalidIdentifierException | JsonSyntaxException e) {
            Log.LOGGER.error("Invalid emoji resource {}: {}", resourceId, e.getMessage());
        } catch (Exception e) {
            Log.LOGGER.error("Unknown error occurred while loading emoji resource " + resourceId, e);
        }
    }

    public void endUpdate() {
        assert tmpTrie != null;
        trie = tmpTrie;
        tmpTrie = null;
    }


    public Map<String, EmojiEntry> query(final String prefix) {
        return trie.prefixMap(prefix);
    }

    static class EmojiParsingError extends Exception {
        EmojiParsingError(String msg) {
            super(msg);
        }
    }
}
