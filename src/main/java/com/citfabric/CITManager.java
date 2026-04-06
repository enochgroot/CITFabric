package com.citfabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import java.io.InputStream;
import java.util.*;

public class CITManager {
    private static final CITManager INSTANCE = new CITManager();
    public static CITManager getInstance() { return INSTANCE; }
    private final List<CITRule> rules = new ArrayList<>();
    private final List<AnimatedCITTexture> animatedTextures = new ArrayList<>();
    private CITManager() {}

    public void reload(ResourceManager manager) {
        Minecraft mc = Minecraft.getInstance();
        for (CITRule rule : rules) mc.getTextureManager().release(rule.textureKey);
        rules.clear();
        animatedTextures.clear();
        Map<ResourceLocation, Resource> propsFiles =
            manager.listResources("optifine/cit", loc -> loc.getPath().endsWith(".properties"));
        for (Map.Entry<ResourceLocation, Resource> entry : propsFiles.entrySet()) {
            try { parseRule(manager, entry.getKey(), entry.getValue()); }
            catch (Exception e) { System.err.println("[CITFabric] Failed: " + entry.getKey() + " - " + e.getMessage()); }
        }
        System.out.println("[CITFabric] Loaded " + rules.size() + " CIT rule(s)");
    }

    private void parseRule(ResourceManager manager, ResourceLocation propsLoc, Resource propsResource) throws Exception {
        Properties props = new Properties();
        try (InputStream is = propsResource.open()) { props.load(is); }
        if (!"item".equals(props.getProperty("type", "item"))) return;
        String items = props.getProperty("items", "").trim();
        String enchProp = props.getProperty("enchantments", "").trim();
        String texName = props.getProperty("texture", "").trim();
        if (items.isEmpty() || enchProp.isEmpty() || texName.isEmpty()) return;
        String itemId = items.contains(":") ? items : "minecraft:" + items;
        String enchId = enchProp.split("\\s+")[0];
        if (!enchId.contains(":")) enchId = "minecraft:" + enchId;
        String propsPath = propsLoc.getPath();
        String dir = propsPath.substring(0, propsPath.lastIndexOf('/') + 1);
        String texPath = dir + texName + ".png";
        ResourceLocation texLoc = ResourceLocation.fromNamespaceAndPath(propsLoc.getNamespace(), texPath);
        ResourceLocation mcmetaLoc = ResourceLocation.fromNamespaceAndPath(propsLoc.getNamespace(), texPath + ".mcmeta");
        boolean hasAnim = manager.getResource(mcmetaLoc).isPresent();
        ResourceLocation textureKey = ResourceLocation.fromNamespaceAndPath(CITFabric.MOD_ID,
            "cit/" + itemId.replace(":", "/") + "/" + enchId.replace(":", "/"));
        Optional<Resource> texRes = manager.getResource(texLoc);
        if (texRes.isEmpty()) { System.err.println("[CITFabric] Texture not found: " + texLoc); return; }
        if (hasAnim) {
            AnimatedCITTexture animated = new AnimatedCITTexture(manager, texLoc, mcmetaLoc);
            Minecraft.getInstance().getTextureManager().register(textureKey, animated);
            animatedTextures.add(animated);
        } else {
            NativeImage img;
            try (InputStream is = texRes.get().open()) { img = NativeImage.read(is); }
            Minecraft.getInstance().getTextureManager().register(textureKey, new DynamicTexture(img));
        }
        rules.add(new CITRule(itemId, enchId, textureKey, hasAnim));
        System.out.println("[CITFabric] Rule: " + itemId + " + " + enchId);
    }

    public CITRule findRule(ItemStack stack) {
        if (stack.isEmpty() || rules.isEmpty()) return null;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) return null;
        Set<String> ids = new HashSet<>();
        enchants.keySet().forEach(h -> ids.add(h.unwrapKey().map(k -> k.location().toString()).orElse("")));
        for (CITRule rule : rules)
            if (rule.itemId.equals(itemId) && ids.contains(rule.enchantment)) return rule;
        return null;
    }

    public void tick() { for (AnimatedCITTexture t : animatedTextures) t.tick(); }
}
