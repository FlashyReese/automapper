package me.flashyreese.automapper.mixin;

import me.flashyreese.automapper.Automapper;
import me.flashyreese.automapper.mappings.MappingsParser;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public class MixinCrashReport {
    @Inject(method = "asString", at = @At(value = "TAIL", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"), cancellable = true)
    public void replace$asString(CallbackInfoReturnable<String> cir) {
        String original = cir.getReturnValue();
        if (original != null) {
            MappingsParser.Mappings.MappingResult mapped = Automapper.mappings.mapLog(original);
            cir.setReturnValue(mapped.mappedLog);
        }
    }

    @Inject(method = "getStackTrace", at = @At(value = "TAIL", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"), cancellable = true)
    public void replace$getStackTrace(CallbackInfoReturnable<String> cir) {
        String original = cir.getReturnValue();
        if (original != null) {
            MappingsParser.Mappings.MappingResult mapped = Automapper.mappings.mapLog(original);
            cir.setReturnValue(mapped.mappedLog);
        }
    }
}
