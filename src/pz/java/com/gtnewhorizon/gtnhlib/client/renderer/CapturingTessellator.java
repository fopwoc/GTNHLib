package com.gtnewhorizon.gtnhlib.client.renderer;

import java.util.Objects;

import org.lwjgl.opengl.GL11;

/**
 * Keeps only the vertex flags of GTNHLib's {@code CapturingTessellator}, which the quad and triangle models carry. The
 * upstream class captures Minecraft's global tessellator and is not compiled; {@link Flags} is copied unchanged.
 */
public final class CapturingTessellator {

    private CapturingTessellator() {}

    public static class Flags {

        public boolean hasTexture;
        public boolean hasBrightness;
        public boolean hasColor;
        public boolean hasNormals;
        @Deprecated
        public int drawMode = GL11.GL_QUADS;

        public Flags(boolean hasTexture, boolean hasBrightness, boolean hasColor, boolean hasNormals) {
            this.hasTexture = hasTexture;
            this.hasBrightness = hasBrightness;
            this.hasColor = hasColor;
            this.hasNormals = hasNormals;
        }

        /**
         * Copy constructor for creating immutable snapshots of flag state.
         */
        public Flags(Flags other) {
            this.hasTexture = other.hasTexture;
            this.hasBrightness = other.hasBrightness;
            this.hasColor = other.hasColor;
            this.hasNormals = other.hasNormals;
            this.drawMode = other.drawMode;
        }

        /**
         * Updates this Flags instance with new values. Used to avoid repeated field assignments.
         */
        public void copyFrom(boolean hasTexture, boolean hasBrightness, boolean hasColor, boolean hasNormals) {
            this.hasTexture = hasTexture;
            this.hasBrightness = hasBrightness;
            this.hasColor = hasColor;
            this.hasNormals = hasNormals;
        }

        /**
         * Updates this Flags instance with new values including draw mode.
         */
        public void copyFrom(boolean hasTexture, boolean hasBrightness, boolean hasColor, boolean hasNormals,
                int drawMode) {
            this.hasTexture = hasTexture;
            this.hasBrightness = hasBrightness;
            this.hasColor = hasColor;
            this.hasNormals = hasNormals;
            this.drawMode = drawMode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Flags flags)) return false;
            return hasTexture == flags.hasTexture && hasBrightness == flags.hasBrightness
                    && hasColor == flags.hasColor
                    && hasNormals == flags.hasNormals
                    && drawMode == flags.drawMode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hasTexture, hasBrightness, hasColor, hasNormals, drawMode);
        }

    }
}
