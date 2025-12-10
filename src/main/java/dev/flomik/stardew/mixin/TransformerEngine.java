package dev.flomik.stardew.mixin;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import dev.flomik.stardew.common.stacksize.StackConfig;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import portb.slw.MyLoggerFactory;
import portb.transformerlib.TransformerLib;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mixin plugin that uses BiggerStacksTransformerLib to apply XML transformers
 * for increasing stack sizes in Container, Slot, and IItemHandler interfaces.
 */
public class TransformerEngine implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerEngine.class);

    static {
        try {
            // Get phases for the transformer
            EnumSet<ILaunchPluginService.Phase> noPhases = EnumSet.noneOf(ILaunchPluginService.Phase.class);
            EnumSet<ILaunchPluginService.Phase> beforePhase = EnumSet.of(ILaunchPluginService.Phase.BEFORE);

            // Get the launch plugins map via reflection
            LaunchPluginHandler handler = getPrivateField(Launcher.INSTANCE, "launchPlugins");
            Map<String, ILaunchPluginService> plugins = getPrivateField(handler, "plugins");

            // Set up TransformerLib logger
            TransformerLib.LOGGER = MyLoggerFactory.createMyLogger(
                    LoggerFactory.getLogger(TransformerLib.class)
            );

            // Set the global stack limit supplier
            TransformerLib.setGlobalStackLimitSupplier(StackConfig::getMaxStackSize);

            // Load XML transformers from resources/transformers/
            TransformerLib.loadTransformers(TransformerEngine.class);

            // Register our transformer as a launch plugin
            plugins.put("stardew_transformer", new ILaunchPluginService() {
                @Override
                public String name() {
                    return "stardew_transformer";
                }

                @Override
                public EnumSet<Phase> handlesClass(Type type, boolean isEmpty) {
                    return beforePhase;
                }

                @Override
                public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
                    return TransformerLib.handleTransformation(classNode);
                }
            });

            LOGGER.debug("[Stardew] TransformerEngine initialized");
        } catch (Exception e) {
            LOGGER.error("[Stardew] Failed to initialize TransformerEngine", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        // No action needed
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // No action needed
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No action needed
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No action needed
    }
}

