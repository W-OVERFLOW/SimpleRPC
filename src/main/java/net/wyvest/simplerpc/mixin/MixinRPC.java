package net.wyvest.simplerpc.mixin;

import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityAssets;
import net.wyvest.simplerpc.SimpleRPC;
import net.wyvest.simplerpc.config.RPCConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "io.github.koxx12dev.scc.rpc.RPC")
public class MixinRPC {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lde/jcm/discordgamesdk/activity/Activity;setState(Ljava/lang/String;)V"))
    private static void modifyState(Activity activity, String state) {
        if (RPCConfig.INSTANCE.getKeep() && RPCConfig.INSTANCE.getSccRetain() && RPCConfig.INSTANCE.getRetainState()) {
            if (SimpleRPC.INSTANCE.getIpc().getPresence() != null && SimpleRPC.INSTANCE.getIpc().getPresence().getState() != null) {
                activity.setState(SimpleRPC.INSTANCE.getIpc().getPresence().getState());
            }
        } else {
            activity.setState(state);
        }
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lde/jcm/discordgamesdk/activity/Activity;setDetails(Ljava/lang/String;)V"))
    private static void modifyDetails(Activity activity, String state) {
        if (RPCConfig.INSTANCE.getKeep() && RPCConfig.INSTANCE.getSccRetain() && RPCConfig.INSTANCE.getRetainDetails()) {
            if (SimpleRPC.INSTANCE.getIpc().getPresence() != null && SimpleRPC.INSTANCE.getIpc().getPresence().getDetails() != null) {
                activity.setDetails(SimpleRPC.INSTANCE.getIpc().getPresence().getDetails());
            }
        } else {
            activity.setDetails(state);
        }
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lde/jcm/discordgamesdk/activity/ActivityAssets;setLargeText(Ljava/lang/String;)V"))
    private static void modifyLargeText(ActivityAssets assets, String string) {
        if (RPCConfig.INSTANCE.getKeep() && RPCConfig.INSTANCE.getSccRetain()) {
            assets.setLargeText("Powered by SimpleRPC by W-OVERFLOW");
        } else {
            assets.setLargeText(string);
        }
    }
}
