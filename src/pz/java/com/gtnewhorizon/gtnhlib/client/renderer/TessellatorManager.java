package com.gtnewhorizon.gtnhlib.client.renderer;

import static com.gtnewhorizon.gtnhlib.bytebuf.MemoryUtilities.memAlloc;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.gtnhlib.client.renderer.tessellator.TessellatorCallback;
import com.gtnewhorizon.gtnhlib.client.renderer.vao.IVertexArrayObject;
import com.gtnewhorizon.gtnhlib.client.renderer.vao.VertexBufferType;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.VertexFormat;

/**
 * Replaces GTNHLib's {@code TessellatorManager} with only its direct-capture stack, which the renderer uses. The
 * upstream class also captures Minecraft's global tessellator, which has no counterpart here, so it is not compiled.
 * The capture stack below follows upstream's {@code startCapturingDirect}/{@code stopCapturingDirect} unchanged.
 */
public final class TessellatorManager {

    public static final Logger LOGGER = LogManager.getLogger("TessellatorManager");

    public static final int DEFAULT_BUFFER_SIZE = 0x8000; // 32KB, enough to store 1024 full-sized vertices
    public static final int DIRECT_TESSELLATOR_STACK_DEPTH = 16;

    // Stands in for Minecraft's shared tessellator buffer, which upstream reuses for the bottom of the stack.
    private static final ByteBuffer MAIN_BUFFER = memAlloc(0x200000 * 4);
    private static final int BUFFER_CAPACITY = MAIN_BUFFER.capacity();

    private static final DirectTessellator mainInstance = new DirectTessellator(MAIN_BUFFER);
    private static final CallbackTessellator mainCallbackInstance = new CallbackTessellator(MAIN_BUFFER);

    private static boolean mainInstanceInStack = false;

    private static final DirectTessellator[] directTessellators = new DirectTessellator[DIRECT_TESSELLATOR_STACK_DEPTH];
    private static int directTessellatorIndex = -1; // Points to the DirectTessellator in the stack (-1 = stack empty)

    private TessellatorManager() {}

    /** There is no global tessellator to merge from. */
    public static Tessellator getVanillaTessellator() {
        return null;
    }

    public static void discardTessellator(Tessellator tessellator) {
        tessellator.reset();
        tessellator.isDrawing = false;
        tessellator.hasNormals = false;
        tessellator.hasColor = false;
        tessellator.hasTexture = false;
        tessellator.hasBrightness = false;
        tessellator.isColorDisabled = false;
    }

    private static DirectTessellator getDirectTessellator() {
        return directTessellators[directTessellatorIndex];
    }

    private static boolean hasDirectTessellator() {
        return directTessellatorIndex != -1;
    }

    public static int getDirectCaptureDepth() {
        return directTessellatorIndex + 1;
    }

    private static void setDirectTessellator(DirectTessellator tessellator) {
        if (++directTessellatorIndex >= DIRECT_TESSELLATOR_STACK_DEPTH) {
            directTessellatorIndex--;
            throw new IllegalStateException("DirectTessellator stack overflow");
        }
        mainInstanceInStack = mainInstanceInStack || tessellator.baseBuffer == MAIN_BUFFER;
        directTessellators[directTessellatorIndex] = tessellator;
    }

    public static DirectTessellator startCapturingDirect() {
        if (!mainInstanceInStack) {
            setDirectTessellator(mainInstance);
            return mainInstance;
        }
        final DirectTessellator tessellator = new DirectTessellator(DEFAULT_BUFFER_SIZE);
        setDirectTessellator(tessellator);
        return tessellator;
    }

    public static DirectTessellator startCapturingDirect(int capacity) {
        if (!mainInstanceInStack && BUFFER_CAPACITY >= capacity) {
            setDirectTessellator(mainInstance);
            return mainInstance;
        }
        final DirectTessellator tessellator = new DirectTessellator(capacity);
        setDirectTessellator(tessellator);
        return tessellator;
    }

    public static DirectTessellator startCapturingDirect(VertexFormat format) {
        final DirectTessellator tessellator = startCapturingDirect();
        try {
            tessellator.setVertexFormat(format);
        } catch (Throwable t) {
            stopCapturingDirect();
            throw t;
        }
        return tessellator;
    }

    @Deprecated
    public static CallbackTessellator startCapturingDirect(DirectDrawCallback callback) {
        return startCapturingDirect(new TessellatorCallback() {

            @Override
            public boolean onDraw(CallbackTessellator tessellator) {
                return callback.onDraw(tessellator);
            }
        });
    }

    public static CallbackTessellator startCapturingDirect(TessellatorCallback callback) {
        final CallbackTessellator tessellator = mainInstanceInStack ? new CallbackTessellator(DEFAULT_BUFFER_SIZE)
                : mainCallbackInstance;
        VertexCallbackManager.pushCallback(tessellator, callback);
        setDirectTessellator(tessellator);
        return tessellator;
    }

    public static void startCapturingDirect(DirectTessellator tessellator) {
        setDirectTessellator(tessellator);
    }

    public static void stopCapturingDirect() {
        if (!hasDirectTessellator()) throw new IllegalStateException("Tried to stop capturing when not capturing!");
        final DirectTessellator tessellator = getDirectTessellator();
        directTessellators[directTessellatorIndex--] = null;
        mainInstanceInStack = mainInstanceInStack && tessellator.baseBuffer != MAIN_BUFFER;
        tessellator.onRemovedFromStack();
    }

    public static IVertexArrayObject stopCapturingDirectToVBO(VertexBufferType bufferType) {
        if (!hasDirectTessellator()) throw new IllegalStateException("Tried to stop capturing when not capturing!");
        final DirectTessellator tessellator = getDirectTessellator();
        final IVertexArrayObject vbo = tessellator.uploadToVBO(bufferType);
        stopCapturingDirect();
        return vbo;
    }
}
