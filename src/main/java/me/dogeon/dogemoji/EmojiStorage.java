package me.dogeon.dogemoji;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EmojiStorage {
    static final class EmojiEntry {
        enum EmojiEntryType { IMAGE, TEXT }
        public EmojiEntryType type;
        public Identifier textureId;
        public String text;

        EmojiEntry(JsonObject json) throws EmojiParsingError {
            if (!json.has("type")) throw new EmojiParsingError("field \"emoji.type\" not found");
            switch (json.get("type").getAsString()) {
                case "image" -> {
                    if (!json.has("texture"))
                        throw new EmojiParsingError("field \"emoji.texture\" not found");
                    this.type = EmojiEntryType.IMAGE;
                    this.textureId = new Identifier(json.get("texture").getAsString());
                }
                case "text" -> {
                    if (!json.has("text"))
                        throw new EmojiParsingError("field \"emoji.text\" not found");
                    this.type = EmojiEntryType.TEXT;
                    this.text = json.get("text").getAsString();
                }
                default -> throw new EmojiParsingError("Unknown emoji type " + json.get("type").getAsString());
            }
        }

        @Override
        public String toString() { return (type == EmojiEntryType.IMAGE) ? textureId.toString() : text; }
    }

    public PatriciaTrie<EmojiEntry> trie;
    public PatriciaTrie<EmojiEntry> tmpTrie;
    private static final JsonParser parser = new JsonParser();

    public EmojiStorage() { trie = new PatriciaTrie<>(); }

    public void beginUpdate() {
        assert tmpTrie == null;
        tmpTrie = new PatriciaTrie<>();
    }

    public void load(ResourceManager manager, Identifier resourceId) {
        try (InputStream stream = manager.getResource(resourceId).getInputStream()) {
            JsonObject rawEmoji = (JsonObject) parser.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (!rawEmoji.has("name")) throw new EmojiParsingError("field \"name\" not found");
            if (!rawEmoji.has("emoji")) throw new EmojiParsingError("field \"emoji\" not found");
            String name = rawEmoji.get("name").getAsString();
            EmojiEntry emojiEntry = new EmojiEntry((JsonObject) rawEmoji.get("emoji"));
            tmpTrie.put(name, emojiEntry);
        } catch (EmojiParsingError e) {
            Dogemoji.LOGGER.error("Invalid emoji resource {}: {}", resourceId, e.getMessage());
        } catch (JsonSyntaxException | InvalidIdentifierException e) {
            Dogemoji.LOGGER.error("Error while parsing emoji resource {}: {}", resourceId, e.getMessage());
        } catch (Exception e) {
            Dogemoji.LOGGER.error("Unknown error occurred while loading emoji resource " + resourceId, e);
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
        EmojiParsingError(String msg) { super(msg); }
    }
}
