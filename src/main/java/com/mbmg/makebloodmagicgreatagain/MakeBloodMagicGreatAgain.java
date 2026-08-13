/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.AttachCapabilitiesEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  org.slf4j.Logger
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.AvaritiaInfinityUpgradeCompat;
import com.mbmg.makebloodmagicgreatagain.BotaniaWillCompat;
import com.mbmg.makebloodmagicgreatagain.MBMGConfig;
import com.mbmg.makebloodmagicgreatagain.MBMGMenus;
import com.mbmg.makebloodmagicgreatagain.MBMGNetwork;
import com.mbmg.makebloodmagicgreatagain.MBMGRecipes;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value="makebloodmagicgreatagain")
public class MakeBloodMagicGreatAgain {
    public static final String MOD_ID = "makebloodmagicgreatagain";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MakeBloodMagicGreatAgain INSTANCE;
    private final boolean botaniaLoaded;
    private final boolean avaritiaLoaded;

    public MakeBloodMagicGreatAgain() {
        INSTANCE = this;
        ModList modList = ModList.get();
        this.botaniaLoaded = modList.isLoaded("botania");
        this.avaritiaLoaded = modList.isLoaded("avaritia");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)MBMGConfig.COMMON_SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MBMGRecipes.register(modEventBus);
        MBMGMenus.register(modEventBus);
        MinecraftForge.EVENT_BUS.register((Object)this);
        LOGGER.info("[MBMG] Starting. Dependencies -> Botania: {}, Avaritia: {}", (Object)this.botaniaLoaded, (Object)this.avaritiaLoaded);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (this.botaniaLoaded && ((Boolean)MBMGConfig.enableBotaniaWill.get()).booleanValue()) {
                try {
                    BotaniaWillCompat.bootstrap();
                    LOGGER.info("[MBMG] Botania Ancient Will compat enabled for \u675f\u7075\u5934\u76d4.");
                }
                catch (Throwable t) {
                    LOGGER.error("[MBMG] Failed to bootstrap Botania Ancient Will compat", t);
                }
            }
            if (this.avaritiaLoaded && ((Boolean)MBMGConfig.enableInfiniteUpgradePoints.get()).booleanValue()) {
                try {
                    AvaritiaInfinityUpgradeCompat.bootstrap();
                    LOGGER.info("[MBMG] Re-Avaritia \u65e0\u5c3d\u5347\u7ea7\u5408\u6210\u5df2\u542f\u7528: living_plate + infinity_catalyst -> maxPoints = Integer.MAX_VALUE");
                }
                catch (Throwable t) {
                    LOGGER.error("[MBMG] Failed to bootstrap Re-Avaritia infinity upgrade compat", t);
                }
            }
            MBMGNetwork.register();
            LOGGER.info("[MBMG] Network channel registered.");
        });
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
    }

    public boolean isBotaniaWillEnabled() {
        return this.botaniaLoaded && (Boolean)MBMGConfig.enableBotaniaWill.get() != false;
    }

    public boolean isInfiniteUpgradePointsEnabled() {
        return this.avaritiaLoaded && (Boolean)MBMGConfig.enableInfiniteUpgradePoints.get() != false;
    }

    public boolean isRitualDivinerWorkbenchEnabled() {
        return (Boolean)MBMGConfig.enableRitualDivinerGUI.get();
    }

    static Field findFieldByNameType(Class<?> owner, String simpleTypeName) throws NoSuchFieldException {
        for (Field f : owner.getDeclaredFields()) {
            for (Class<?> t = f.getType(); t != null; t = t.getSuperclass()) {
                if (t.getSimpleName().equals(simpleTypeName)) {
                    f.setAccessible(true);
                    return f;
                }
                for (Class<?> iface : t.getInterfaces()) {
                    if (!iface.getSimpleName().equals(simpleTypeName)) continue;
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        throw new NoSuchFieldException("No field of type " + simpleTypeName + " on " + owner.getName());
    }

    static <T> T invokeStatic(Class<?> owner, String methodName, Class<?>[] params, Object ... args) throws ReflectiveOperationException {
        Method m = owner.getMethod(methodName, params);
        m.setAccessible(true);
        return (T)m.invoke(null, args);
    }

    static <T> T invoke(Object target, String methodName, Class<?>[] params, Object ... args) throws ReflectiveOperationException {
        Method m = target.getClass().getMethod(methodName, params);
        m.setAccessible(true);
        return (T)m.invoke(target, args);
    }

    static List asList(Object ... items) {
        return Arrays.asList(items);
    }

    static String lowerName(Enum<?> e) {
        return e.name().toLowerCase(Locale.ROOT);
    }
}

