package com.citfabric.cit;

import net.minecraft.resources.Identifier;
import java.io.*;
import java.util.*;

public class CITProperties {

    public static CITRule parse(String content, Identifier location) {
        Properties props = new Properties();
        try { props.load(new StringReader(content)); }
        catch (IOException e) {
            System.err.println("[CITFabric] Parse error " + location + ": " + e.getMessage());
            return null;
        }

        String type = props.getProperty("type","item").trim();
        if (!type.equals("item")) {
            System.out.println("[CITFabric] Skipping non-item type=" + type + " in " + location);
            return null;
        }

        Set<Identifier> items   = parseIds(props.getProperty("items",
            props.getProperty("matchItems", props.getProperty("item",""))));
        Set<Identifier> enchants = parseIds(props.getProperty("enchantments",
            props.getProperty("enchantmentIDs", props.getProperty("enchantmentIds",""))));

        // Strip level ranges from enchant IDs e.g. "minecraft:breach:1-127"
        Set<Identifier> normEnchants = new LinkedHashSet<>();
        for (Identifier e : enchants) {
            String path = e.getPath();
            int ci = path.lastIndexOf(':');
            if (ci >= 0) {
                try { Integer.parseInt(path.substring(ci+1)); path = path.substring(0,ci); }
                catch (NumberFormatException ignored) {}
            }
            normEnchants.add(Identifier.fromNamespaceAndPath(e.getNamespace(), path));
        }

        String name   = props.getProperty("name","").trim();
        String texStr = props.getProperty("texture","").trim();
        Identifier tex = texStr.isEmpty() ? null : resolve(location, texStr);

        if (tex == null) {
            System.err.println("[CITFabric] No valid texture in " + location + ", skipping");
            return null;
        }

        System.out.println("[CITFabric] Parsed rule: items=" + items
            + " name=" + (name.isEmpty()?"<any>":name) + " tex=" + tex);
        return new CITRule(items, normEnchants, name.isEmpty()?null:name, tex, location.getPath());
    }

    private static Set<Identifier> parseIds(String raw) {
        Set<Identifier> r = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) return r;
        for (String p : raw.trim().split("[\\s,]+")) {
            if (p.isBlank()) continue;
            if (!p.contains(":")) p = "minecraft:" + p;
            try { r.add(Identifier.parse(p)); }
            catch (Exception e) { System.err.println("[CITFabric] Bad ID: " + p); }
        }
        return r;
    }

    // Resolve texture path relative to properties file location
    // e.g. location=minecraft:optifine/cit/thor/mjolnir.properties, ref="mjolnir"
    // -> minecraft:optifine/cit/thor/mjolnir
    private static Identifier resolve(Identifier base, String ref) {
        if (ref.contains(":")) {
            try { return Identifier.parse(ref); } catch (Exception ignored) {}
        }
        if (ref.endsWith(".png")) ref = ref.substring(0, ref.length()-4);
        String path = base.getPath();
        int sl = path.lastIndexOf('/');
        String dir = sl >= 0 ? path.substring(0, sl+1) : "";
        return Identifier.fromNamespaceAndPath(base.getNamespace(), dir + ref);
    }
}
