package com.github.flandre923.berrypouch.client.config;

import com.github.flandre923.berrypouch.client.DevEnvironment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PokeBallGunTransformSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final float DEFAULT_BASE_ROTATION_Y = 90F;

    private static final Transform DEFAULT_FIRST_PERSON = new Transform(
            0.2F, 0.35F, -0.25F,
            0F, 0F, 0F,
            0.4F, 0.4F, 0.4F
    );

    private static final Transform DEFAULT_GROUND = new Transform(
            0.2f,0.45f,-0.25f,
            0f,0f,0f,
            0.4f,0.4f,0.4f
    );

    private static final Transform DEFAULT_GUI = new Transform(
            -0.6f,0.0f,0.0f,
            0f,90f,0f,
            0.8f,0.8f,0.8f
    );

    private static final Transform DEFAULT_OTHER = new Transform(
            0F, 0F, 0F,
            0F, 45F, 0F,
            1F, 1F, 1F
    );

    private static Transform firstPerson = DEFAULT_FIRST_PERSON.copy();
    private static Transform other = DEFAULT_OTHER.copy();
    private static Transform gui =  DEFAULT_GUI.copy();
    private static Transform ground = DEFAULT_GROUND.copy();
    private static float baseRotationY = DEFAULT_BASE_ROTATION_Y;

    private PokeBallGunTransformSettings() {
    }

    public static Transform getFirstPerson() {
        return firstPerson;
    }

    public static Transform getOther() {
        return other;
    }

    public static Transform getGUI(){
        return gui;
    }

    public static Transform getGroud(){
        return ground;
    }

    public static float getBaseRotationY() {
        return baseRotationY;
    }

    public static void setBaseRotationY(float value) {
        baseRotationY = value;
    }

    public static void load() {
        if (!DevEnvironment.IS_DEV) {
            resetToDefaults();
            return;
        }
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data == null) {
                resetToDefaults();
                save();
                return;
            }
            applyData(data);
        } catch (IOException | JsonSyntaxException ignored) {
            resetToDefaults();
            save();
        }
    }

    public static void save() {
        if (!DevEnvironment.IS_DEV) {
            return;
        }
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException ignored) {
            return;
        }

        Data data = new Data();
        data.firstPerson = firstPerson;
        data.other = other;
        data.baseRotationY = baseRotationY;

        try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (IOException ignored) {
        }
    }

    public static void resetToDefaults() {
        firstPerson = DEFAULT_FIRST_PERSON.copy();
        other = DEFAULT_OTHER.copy();
        baseRotationY = DEFAULT_BASE_ROTATION_Y;
    }

    private static void applyData(Data data) {
        firstPerson = data.firstPerson == null ? DEFAULT_FIRST_PERSON.copy() : data.firstPerson;
        other = data.other == null ? DEFAULT_OTHER.copy() : data.other;
        baseRotationY = data.baseRotationY;
    }

    private static Path getConfigPath() {
        return Path.of(Minecraft.getInstance().gameDirectory.getAbsolutePath(),
                "config",
                "berrypouch",
                "pokeball_transform.json");
    }

    private static final class Data {
        private Transform firstPerson = DEFAULT_FIRST_PERSON.copy();
        private Transform other = DEFAULT_OTHER.copy();
        private float baseRotationY = DEFAULT_BASE_ROTATION_Y;
    }

    public static final class Transform {
        public float translateX;
        public float translateY;
        public float translateZ;
        public float rotateX;
        public float rotateY;
        public float rotateZ;
        public float scaleX;
        public float scaleY;
        public float scaleZ;

        public Transform() {
        }

        public Transform(
                float translateX,
                float translateY,
                float translateZ,
                float rotateX,
                float rotateY,
                float rotateZ,
                float scaleX,
                float scaleY,
                float scaleZ
        ) {
            this.translateX = translateX;
            this.translateY = translateY;
            this.translateZ = translateZ;
            this.rotateX = rotateX;
            this.rotateY = rotateY;
            this.rotateZ = rotateZ;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
        }

        public Transform copy() {
            return new Transform(
                    translateX,
                    translateY,
                    translateZ,
                    rotateX,
                    rotateY,
                    rotateZ,
                    scaleX,
                    scaleY,
                    scaleZ
            );
        }
    }
}
