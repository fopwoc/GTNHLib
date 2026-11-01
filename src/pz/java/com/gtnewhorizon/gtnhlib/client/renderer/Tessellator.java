package com.gtnewhorizon.gtnhlib.client.renderer;

/**
 * The part of Minecraft's tessellator API that GTNHLib's direct tessellators inherit and its vertex writers read. Only
 * state lives here; every drawing operation belongs to the concrete tessellator.
 */
public abstract class Tessellator {

    /** Minecraft's global tessellator. There is none here; callers already treat {@code null} as idle. */
    public static Tessellator instance;

    public int[] rawBuffer;
    public int rawBufferIndex;
    public int rawBufferSize;
    public int vertexCount;
    public int drawMode;
    public boolean isDrawing;
    public boolean isColorDisabled;
    public boolean hasColor;
    public boolean hasTexture;
    public boolean hasBrightness;
    public boolean hasNormals;
    public double textureU;
    public double textureV;
    public int brightness;
    public int color;
    public int normal;
    public double xOffset;
    public double yOffset;
    public double zOffset;

    public abstract int draw();

    public abstract void reset();

    public abstract void startDrawing(int drawMode);

    public abstract void addVertex(double x, double y, double z);

    public abstract void setTextureUV(double u, double v);

    public abstract void setNormal(float nx, float ny, float nz);

    public abstract void setColorRGBA(int red, int green, int blue, int alpha);

    public abstract void setBrightness(int brightness);

    public void setColorRGBA_I(int color, int alpha) {
        setColorRGBA(color >> 16 & 255, color >> 8 & 255, color & 255, alpha);
    }
}
