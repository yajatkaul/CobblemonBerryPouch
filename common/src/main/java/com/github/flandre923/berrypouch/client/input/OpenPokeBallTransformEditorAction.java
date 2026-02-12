package com.github.flandre923.berrypouch.client.input;

import com.github.flandre923.berrypouch.client.DevEnvironment;
import com.github.flandre923.berrypouch.client.screen.PokeBallTransformScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class OpenPokeBallTransformEditorAction implements KeyAction {
    @Override
    public void onKeyPressed(Minecraft client) {
        if (!DevEnvironment.IS_DEV) {
            return;
        }
        client.setScreen(new PokeBallTransformScreen(client.screen));
    }

    @Override
    public boolean shouldTrigger(Player player) {
        return true;
    }
}
