package dev.flomik.stardew.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class StardewConfig {

    public static class Client {
        public final ForgeConfigSpec.DoubleValue energyBarScale;
        public final ForgeConfigSpec.DoubleValue clockScale;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Visual settings").push("client");

            energyBarScale = builder
                    .comment("Scale of the Energy Bar overlay")
                    .defineInRange("energyBarScale", 1.0, 0.1, 5.0);

            clockScale = builder
                    .comment("Scale of the Clock overlay")
                    .defineInRange("clockScale", 1.0, 0.1, 5.0);

            builder.pop();
        }
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue allowSprinting;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Gameplay settings").push("gameplay");
            builder.comment("WARNING: Changing these settings can significantly affect your gameplay experience!");

            allowSprinting = builder
                    .comment(
                            "Allow players to sprint.",
                            "WARNING: This option SIGNIFICANTLY affects your gameplay experience, making it unplanned easier in some places!",
                            "Default: false (sprinting disabled)"
                    )
                    .translation("stardew.config.allowSprinting")
                    .define("allowSprinting", false);

            builder.pop();
        }
    }

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
