package org.zouhmi.momentum.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zouhmi.momentum.config.MomentumConfig;
import org.zouhmi.momentum.features.ProfileManager;
import org.zouhmi.momentum.features.WindowFeatures;

/**
 * Adds Momentum buttons inside the vanilla Video and Graphics screen.
 * Buttons call runtime features directly, they persist through config.
 */
@Mixin(VideoOptionsScreen.class)
public abstract class VideoOptionsScreenMixin extends Screen {

    private static MomentumConfig momentum$lastBaseline = null;

    protected VideoOptionsScreenMixin(Screen parent, MinecraftClient client, GameOptions gameOptions) {
        super(null);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void momentum$addMomentumButtons(CallbackInfo ci) {
        boolean borderlessOn = WindowFeatures.isBorderlessActive();
        boolean batteryOn = WindowFeatures.isBatterySaverActive();

        ButtonWidget borderlessBtn = ButtonWidget.builder(
                Text.literal("Momentum Borderless: " + (borderlessOn ? "ON" : "OFF")),
                btn -> {
                    WindowFeatures.toggleBorderless();
                    btn.setMessage(Text.literal("Momentum Borderless: "
                            + (WindowFeatures.isBorderlessActive() ? "ON" : "OFF")));
                })
                .dimensions(this.width / 2 - 200, this.height - 52, 195, 20)
                .tooltip(Tooltip.of(Text.literal("Decoration-less fullscreen on the primary monitor")))
                .build();

        ButtonWidget batteryBtn = ButtonWidget.builder(
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
                .tooltip(Tooltip.of(Text.literal("Caps FPS and restores your previous cap when off")))
                .build();

        ButtonWidget lowEndBtn = ButtonWidget.builder(
                Text.literal("Momentum Profile: Low-End"),
                btn -> {
                    // Two-press flow: first press previews, second press commits.
                    if (momentum$lastBaseline == null) {
                        momentum$lastBaseline = momentum$deepCopy(
                                org.zouhmi.momentum.MomentumClient.getConfig());
                        ProfileManager.apply(ProfileManager.Profile.LOW_END,
                                org.zouhmi.momentum.MomentumClient.getConfig());
                        btn.setMessage(Text.literal("Low-End applied, press again to confirm"));
                    } else {
                        momentum$lastBaseline = null;
                        btn.setMessage(Text.literal("Momentum Profile: Low-End"));
                    }
                })
                .dimensions(this.width / 2 - 200, this.height - 27, 195, 20)
                .tooltip(Tooltip.of(Text.literal("Conservative settings for weak laptops. Press to preview and apply.")))
                .build();

        ButtonWidget revertBtn = ButtonWidget.builder(
                Text.literal("Momentum: Revert Last Profile"),
                btn -> {
                    var cfg = org.zouhmi.momentum.MomentumClient.getConfig();
                    if (momentum$lastBaseline != null) {
                        ProfileManager.revert(momentum$lastBaseline, cfg);
                        momentum$lastBaseline = null;
                        btn.setMessage(Text.literal("Momentum: reverted"));
                    } else {
                        btn.setMessage(Text.literal("Momentum: nothing to revert"));
                    }
                })
                .dimensions(this.width / 2 - 200, this.height - 76, 195, 20)
                .tooltip(Tooltip.of(Text.literal("Restores the settings captured before the last profile apply")))
                .build();

        ButtonWidget lowLatBtn = ButtonWidget.builder(
                Text.literal("Momentum Profile: Low-Latency"),
                btn -> {
                    ProfileManager.apply(ProfileManager.Profile.LOW_LATENCY,
                            org.zouhmi.momentum.MomentumClient.getConfig());
                    btn.setMessage(Text.literal("Mom. Low-Latency: applied"));
                })
                .dimensions(this.width / 2 + 5, this.height - 27, 195, 20)
                .tooltip(Tooltip.of(Text.literal("Reduces overhead without changing your FPS cap")))
                .build();

        this.addDrawableChild(borderlessBtn);
        this.addDrawableChild(batteryBtn);
        this.addDrawableChild(lowEndBtn);
        this.addDrawableChild(lowLatBtn);
        this.addDrawableChild(revertBtn);
    }

    private MomentumConfig momentum$deepCopy(MomentumConfig src) {
        var copy = new MomentumConfig();
        copy.entities.entityCulling = src.entities.entityCulling;
        copy.chunks.coalesceDuplicateRebuilds = src.chunks.coalesceDuplicateRebuilds;
        copy.memory.trimCachesOnUnload = src.memory.trimCachesOnUnload;
        copy.battery.enabled = src.battery.enabled;
        copy.battery.maxFps = src.battery.maxFps;
        return copy;
    }
}
