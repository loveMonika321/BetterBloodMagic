/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$BooleanValue
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 */
package com.mbmg.makebloodmagicgreatagain;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MBMGConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.BooleanValue enableBotaniaWill;
    public static final ForgeConfigSpec.BooleanValue enableInfiniteUpgradePoints;
    public static final ForgeConfigSpec.BooleanValue enableRitualDivinerGUI;

    private MBMGConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("botania");
        enableBotaniaWill = builder.comment("\u8ba9\u675f\u7075\u5934\u76d4\u5b9e\u73b0\u690d\u7269\u9b54\u6cd5 AncientWillContainer\uff0c\u53ef\u50cf\u6cf0\u62c9\u5934\u76d4\u4e00\u6837\u88c5\u4e0a\u516d\u79cd\u610f\u5fd7\u3002(\u9ed8\u8ba4: true)").define("enableAncientWill", true);
        builder.pop();
        builder.push("avaritia");
        enableInfiniteUpgradePoints = builder.comment("\u8ba9\u675f\u7075\u80f8\u7532\u53ef\u901a\u8fc7\u5408\u6210\u65e0\u5c3d\u50ac\u5316\u5242(infinity_catalyst)\u5c06\u5347\u7ea7\u70b9\u6570\u4e0a\u9650\u63d0\u5347\u81f3Integer.MAX_VALUE\u3002\u5408\u6210: living_plate + infinity_catalyst\u3002\u4ec5\u5f53\u68c0\u6d4b\u5230 avaritia \u65f6\u751f\u6548\u3002(\u9ed8\u8ba4: true)").define("enableInfiniteUpgradePoints", true);
        builder.pop();
        builder.push("ritual_diviner");
        enableRitualDivinerGUI = builder.comment("\u4e3a\u8840\u9b54\u6cd5\u4eea\u5f0f\u63a8\u6d4b\u6756(ritualdiviner / ritualdivinerdusk)\u5f00\u542f\u4eea\u5f0f\u9009\u62e9GUI\uff1a\u624b\u6301\u63a8\u6d4b\u6756\u6309 Z \u952e\u76f4\u63a5\u4ece\u5217\u8868\u9009\u62e9\u4eea\u5f0f\uff0c\u66ff\u4ee3\u539f\u672c Shift+\u5de6\u53f3\u952e\u5faa\u73af\u5207\u6362\u3002(\u9ed8\u8ba4: true)").define("enableRitualGUI", true);
        builder.pop();
        COMMON_SPEC = builder.build();
    }
}

