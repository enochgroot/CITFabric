package com.citfabric;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnimatedCITTexture extends DynamicTexture {
    private final List<NativeImage> frames = new ArrayList<>();
    private final List<int[]> frameList = new ArrayList<>();
    private int framePos = 0;
    private int tickTimer = 0;

    public AnimatedCITTexture(ResourceManager manager, ResourceLocation texLoc, ResourceLocation mcmetaLoc) throws Exception {
        // Init with blank 16x16 image — will be updated on first tick
        super(new NativeImage(NativeImage.Format.RGBA, 16, 16, false));

        NativeImage strip;
        Optional<Resource> texRes = manager.getResource(texLoc);
        if (texRes.isEmpty()) throw new IllegalArgumentException("Texture not found: " + texLoc);
        try (InputStream is = texRes.get().open()) { strip = NativeImage.read(is); }
        int w = strip.getWidth();
        int frameH = w;
        int numFrames = strip.getHeight() / frameH;
        for (int i = 0; i < numFrames; i++) {
            NativeImage frame = new NativeImage(NativeImage.Format.RGBA, w, frameH, false);
            for (int y = 0; y < frameH; y++)
                for (int x = 0; x < w; x++)
                    frame.setPixelRGBA(x, y, strip.getPixelRGBA(x, i * frameH + y));
            frames.add(frame);
        }
        strip.close();

        Optional<Resource> metaRes = manager.getResource(mcmetaLoc);
        if (metaRes.isPresent()) {
            try (InputStream is = metaRes.get().open(); InputStreamReader rdr = new InputStreamReader(is)) {
                JsonObject root = JsonParser.parseReader(rdr).getAsJsonObject();
                if (root.has("animation")) {
                    JsonObject anim = root.getAsJsonObject("animation");
                    int dt = anim.has("frametime") ? anim.get("frametime").getAsInt() : 2;
                    if (anim.has("frames")) {
                        for (var elem : anim.getAsJsonArray("frames")) {
                            if (elem.isJsonObject()) {
                                JsonObject f = elem.getAsJsonObject();
                                frameList.add(new int[]{f.get("index").getAsInt(), f.get("time").getAsInt()});
                            } else { frameList.add(new int[]{elem.getAsInt(), dt}); }
                        }
                    }
                }
            }
        }
        if (frameList.isEmpty()) for (int i = 0; i < frames.size(); i++) frameList.add(new int[]{i, 2});
        uploadFrame(0);
    }

    private void uploadFrame(int frameIdx) {
        if (frameIdx >= frames.size()) return;
        NativeImage src = frames.get(frameIdx);
        NativeImage pixels = getPixels();
        if (pixels == null) return;
        for (int y = 0; y < Math.min(src.getHeight(), pixels.getHeight()); y++)
            for (int x = 0; x < Math.min(src.getWidth(), pixels.getWidth()); x++)
                pixels.setPixelRGBA(x, y, src.getPixelRGBA(x, y));
        upload();
    }

    public void tick() {
        if (frameList.isEmpty()) return;
        tickTimer++;
        if (tickTimer >= frameList.get(framePos)[1]) {
            tickTimer = 0;
            framePos = (framePos + 1) % frameList.size();
            uploadFrame(frameList.get(framePos)[0]);
        }
    }
}
