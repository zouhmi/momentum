package org.zouhmi.momentum.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zouhmi.momentum.features.WindowFeatures;

/**
 * Adds Momentum buttons inside the vanilla Video and Graphics screen.
 * Buttons call runtime features directly, they persist through config.
 */
@Mixin(VideoOptionsScreen.class)
public abstract class VideoOptionsScreenMixin extends Screen {

    protected VideoOptionsScreenMixin(Screen parent, MinecraftClient client, GameOptions gameOptions) {
        super(null);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void momentum$addMomentumButtons(CallbackInfo ci) {
        boolean borderlessOn = WindowFeatures.isBorderlessActive();
        boolean batteryOn = WindowFeatures.isBatterySaverActive();

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Momentum Borderless: " + (borderlessOn ? "ON" : "OFF")),
                btn -> {
                    WindowFeatures.toggleBorderless();
                    btn.setMessage(Text.literal("Momentum Borderless: "
                            + (WindowFeatures.isBorderlessActive() ? "ON" : "OFF")));
                })
                .dimensions(this.width / 2 - 200, this.height - 52, 195, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Momentum Battery Saver: " + (batteryOn ? "ON" : "OFF")),
                btn -> {
                    var cfg = org.zouhmi.momentum.MomentumClient.getConfig();
                    if (WindowFeatures.isBatterySaverActive()) {
                        WindowFeatures.clearBatterySaver();
                        cfg.battery.enabled = false;
                        cfg.save();
                    } else {
                        cfg.battery.enabled = true;
                        cfg.save();
                        WindowFeatures.applyBatterySaverOnce();
                    }
                    btn.setMessage(Text.literal("Momentum Battery Saver: "
                            + (WindowFeatures.isBatterySaverActive() ? "ON" : "OFF")));
                })
                .dimensions(this.width / 2 + 5, this.height - 52, 195, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Momentum Profile: Low-End"),
                btn -> {
                    var cfg = org.zouhmi.momentum.MomentumClient.getConfig();
                    var baseline = deepCopy(cfg);
                    org.zouhmi.momentum.features.ProfileManager.apply(
                            org.zouhmi.momentum.features.ProfileManager.Profile.LOW_END, cfg);
                    btn.setMessage(Text.literal("Mom. Low-End: applied"));
                })
                .dimensions(this.width / 2 - 200, this.height - 27, 195, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Momentum Profile: Low-Latency"),
                btn -> {
                    var cfg = org.zouhmi.momentum.MomentumClient.getConfig();
                    org.zouhmi.momentum.features.ProfileManager.apply(
                            org.zouhmi.momentum.features.ProfileManager.Profile.LOW_LATENCY, cfg);
                    btn.setMessage(Text.literal("Mom. Low-Latency: applied"));
                })
                .dimensions(this.width / 2 + 5, this.height - 27, 195, 20)
                .build());
    }

    private org.zouhmi.momentum.config.MomentumConfig deepCopy(org.zouhmi.momentum.config.MomentumConfig src) {
        var copy = new org.zouhmi.momentum.config.MomentumConfig();
        copy.entities.entityCulling = src.entities.entityCulling;
        copy.chunks.coalesceDuplicateRebuilds = src.chunks.coalesceDuplicateRebuilds;
        copy.memory.trimCachesOnUnload = src.memory.trimCachesOnUnload;
        copy.battery.enabled = src.battery.enabled;
        copy.battery.maxFps = src.battery.maxFps;
        return copy;
    }
}
