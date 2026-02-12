package com.github.flandre923.berrypouch.client;

import dev.architectury.platform.Platform;

public final class DevEnvironment {
    public static final boolean IS_DEV = Platform.isDevelopmentEnvironment();

    private DevEnvironment() {
    }
}
