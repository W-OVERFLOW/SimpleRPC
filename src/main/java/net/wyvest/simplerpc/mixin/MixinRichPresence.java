package net.wyvest.simplerpc.mixin;

import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityTimestamps;
import net.wyvest.simplerpc.SimpleRPC;
import net.wyvest.simplerpc.config.RPCConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.time.Instant;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "io.github.dediamondpro.hycord.features.discord.RichPresence")
public class MixinRichPresence {
    @Redirect(method = "updateRPC", at = @At(value = "INVOKE", target = "Lde/jcm/discordgamesdk/activity/Activity;setState(Ljava/lang/String;)V"))
    private void modifyState(Activity activity, String state) {
        if (RPCConfig.INSTANCE.getKeep()) {
            if (SimpleRPC.INSTANCE.getIpc().getPresence() != null && SimpleRPC.INSTANCE.getIpc().getPresence().getState() != null) {
                activity.setState(SimpleRPC.INSTANCE.getIpc().getPresence().getState());
            }
        } else {
            activity.setState(state);
        }
    }

    @Redirect(method = "updateRPC", at = @At(value = "INVOKE", target = "Lde/jcm/discordgamesdk/activity/Activity;setDetails(Ljava/lang/String;)V"))
    private void modifyDetails(Activity activity, String state) {
        if (RPCConfig.INSTANCE.getKeep()) {
            if (SimpleRPC.INSTANCE.getIpc().getPresence() != null && SimpleRPC.INSTANCE.getIpc().getPresence().getDetails() != null) {
                activity.setDetails(SimpleRPC.INSTANCE.getIpc().getPresence().getDetails());
            }
        } else {
            activity.setDetails(state);
        }
    }

    @Redirect(method = "updateRPC", at = @At(value = "INVOKE", target = "Lde/jcm/discordgamesdk/activity/ActivityTimestamps;setStart(Ljava/time/Instant;)V"))
    private void modifyTime(ActivityTimestamps timestamps, Instant instant) {
        if (RPCConfig.INSTANCE.getKeep()) {
            if (SimpleRPC.INSTANCE.getIpc().getPresence() != null && SimpleRPC.INSTANCE.getIpc().getPresence().getStartTimestamp() != null) {
                timestamps.setStart(Instant.ofEpochSecond(SimpleRPC.INSTANCE.getIpc().getPresence().getStartTimestamp()));
            }
        } else {
            timestamps.setStart(instant);
        }
    }
}
