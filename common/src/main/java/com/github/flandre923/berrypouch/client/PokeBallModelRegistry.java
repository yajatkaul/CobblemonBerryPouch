package com.github.flandre923.berrypouch.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokeball.PokeBall;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PokeBallModelRegistry {
    
    private static final Map<String, Float> BALL_TO_INDEX = new HashMap<>();
    private static float nextIndex = 1.0f;
    
    static {
        // 预定义已知精灵球的索引（与现有模型文件对应）
        BALL_TO_INDEX.put("poke_ball", 1.0f);
        BALL_TO_INDEX.put("citrine_ball", 2.0f);
        BALL_TO_INDEX.put("verdant_ball", 3.0f);
        BALL_TO_INDEX.put("azure_ball", 4.0f);
        BALL_TO_INDEX.put("roseate_ball", 5.0f);
        BALL_TO_INDEX.put("slate_ball", 6.0f);
        BALL_TO_INDEX.put("premier_ball", 7.0f);
        BALL_TO_INDEX.put("great_ball", 8.0f);
        BALL_TO_INDEX.put("ultra_ball", 9.0f);
        BALL_TO_INDEX.put("safari_ball", 10.0f);
        BALL_TO_INDEX.put("fast_ball", 11.0f);
        BALL_TO_INDEX.put("level_ball", 12.0f);
        BALL_TO_INDEX.put("lure_ball", 13.0f);
        BALL_TO_INDEX.put("heavy_ball", 14.0f);
        BALL_TO_INDEX.put("love_ball", 15.0f);
        BALL_TO_INDEX.put("friend_ball", 16.0f);
        BALL_TO_INDEX.put("moon_ball", 17.0f);
        BALL_TO_INDEX.put("sport_ball", 18.0f);
        BALL_TO_INDEX.put("park_ball", 19.0f);
        BALL_TO_INDEX.put("net_ball", 20.0f);
        BALL_TO_INDEX.put("dive_ball", 21.0f);
        BALL_TO_INDEX.put("nest_ball", 22.0f);
        BALL_TO_INDEX.put("repeat_ball", 23.0f);
        BALL_TO_INDEX.put("timer_ball", 24.0f);
        BALL_TO_INDEX.put("luxury_ball", 25.0f);
        BALL_TO_INDEX.put("dusk_ball", 26.0f);
        BALL_TO_INDEX.put("heal_ball", 27.0f);
        BALL_TO_INDEX.put("quick_ball", 28.0f);
        BALL_TO_INDEX.put("dream_ball", 29.0f);
        BALL_TO_INDEX.put("beast_ball", 30.0f);
        BALL_TO_INDEX.put("master_ball", 31.0f);
        BALL_TO_INDEX.put("cherish_ball", 32.0f);
        BALL_TO_INDEX.put("ancient_poke_ball", 33.0f);
        BALL_TO_INDEX.put("ancient_citrine_ball", 34.0f);
        BALL_TO_INDEX.put("ancient_verdant_ball", 35.0f);
        BALL_TO_INDEX.put("ancient_azure_ball", 36.0f);
        BALL_TO_INDEX.put("ancient_roseate_ball", 37.0f);
        BALL_TO_INDEX.put("ancient_slate_ball", 38.0f);
        BALL_TO_INDEX.put("ancient_ivory_ball", 39.0f);
        BALL_TO_INDEX.put("ancient_great_ball", 40.0f);
        BALL_TO_INDEX.put("ancient_ultra_ball", 41.0f);
        BALL_TO_INDEX.put("ancient_feather_ball", 42.0f);
        BALL_TO_INDEX.put("ancient_wing_ball", 43.0f);
        BALL_TO_INDEX.put("ancient_jet_ball", 44.0f);
        BALL_TO_INDEX.put("ancient_heavy_ball", 45.0f);
        BALL_TO_INDEX.put("ancient_leaden_ball", 46.0f);
        BALL_TO_INDEX.put("ancient_gigaton_ball", 47.0f);
        BALL_TO_INDEX.put("ancient_origin_ball", 48.0f);
        
        nextIndex = 49.0f;
    }
    
    public static float getModelIndex(String ballPath) {
        return BALL_TO_INDEX.getOrDefault(ballPath, -1.0f);
    }
    
    public static float getOrAssignModelIndex(String ballPath) {
        float existing = getModelIndex(ballPath);
        if (existing > 0) {
            return existing;
        }
        
        // 为新精灵球分配索引
        float newIndex = nextIndex;
        nextIndex++;
        BALL_TO_INDEX.put(ballPath, newIndex);
        return newIndex;
    }
    
    public static String getTexturePath(String ballPath) {
        return "cobblemon:item/poke_balls/models/" + ballPath;
    }
    
    public static Set<String> getRegisteredBalls() {
        return BALL_TO_INDEX.keySet();
    }
}
