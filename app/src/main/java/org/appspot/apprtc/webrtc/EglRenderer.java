package org.appspot.apprtc.webrtc;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;

import org.appspot.apprtc.util.L;
import org.webrtc.EglBase;
import org.webrtc.EglBase.Context;
import org.webrtc.EglBase10;
import org.webrtc.GlTextureFrameBuffer;
import org.webrtc.GlUtil;
import org.webrtc.Logging;
import org.webrtc.RendererCommon;
import org.webrtc.RendererCommon.GlDrawer;
import org.webrtc.RendererCommon.YuvUploader;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoRenderer.I420Frame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EglRenderer implements Callbacks {
    private static final String TAG = "EglRenderer";
    private static final long LOG_INTERVAL_SEC = 4L;
    private static final int MAX_SURFACE_CLEAR_COUNT = 3;
    private final String name;
    private final Object handlerLock = new Object();
    private Handler renderThreadHandler;
    private final ArrayList<FrameListenerAndParams> frameListeners = new ArrayList();
    private final Object fpsReductionLock = new Object();
    private long nextFrameTimeNs;
    private long minRenderPeriodNs;
    private EglBase eglBase;
    private final YuvUploader yuvUploader = new YuvUploader();
    private GlDrawer drawer;
    private int[] yuvTextures = null;
    private final Object frameLock = new Object();
    private I420Frame pendingFrame;
    private final Object layoutLock = new Object();
    private float layoutAspectRatio;
    private boolean mirror;
    private final Object statisticsLock = new Object();
    private int framesReceived;
    private int framesDropped;
    private int framesRendered;
    private long statisticsStartTimeNs;
    private long renderTimeNs;
    private long renderSwapBufferTimeNs;
    private GlTextureFrameBuffer bitmapTextureFramebuffer;
    private final Runnable renderFrameRunnable = () -> EglRenderer.this.renderFrameOnRenderThread();
    private final Runnable logStatisticsRunnable = () -> {
        EglRenderer.this.logStatistics();
        synchronized(EglRenderer.this.handlerLock) {
            if (EglRenderer.this.renderThreadHandler != null) {
                EglRenderer.this.renderThreadHandler.removeCallbacks(EglRenderer.this.logStatisticsRunnable);
                EglRenderer.this.renderThreadHandler.postDelayed(EglRenderer.this.logStatisticsRunnable, TimeUnit.SECONDS.toMillis(4L));
            }

        }
    };
    private float surfaceViewScale = 1.0f;
    private final EglRenderer.EglSurfaceCreation eglSurfaceCreationRunnable = new EglRenderer.EglSurfaceCreation();

    public EglRenderer(String name) {
        this.name = name;
    }

    public void init(final Context sharedContext, final int[] configAttributes, GlDrawer drawer) {
        L.i(this);
        Object var4 = this.handlerLock;
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler != null) {
                throw new IllegalStateException(this.name + "Already initialized");
            } else {
                L.d(getClass(),"Initializing EglRenderer");
                this.drawer = drawer;
                HandlerThread renderThread = new HandlerThread(this.name + "EglRenderer");
                renderThread.start();
                this.renderThreadHandler = new Handler(renderThread.getLooper());
                ThreadUtils.invokeAtFrontUninterruptibly(this.renderThreadHandler, () -> {
                    if (sharedContext == null) {
                        L.d(getClass(), "EglBase10.create context");
                        EglRenderer.this.eglBase = new EglBase10(null, configAttributes);
                    } else {
                        L.d(getClass(), "EglBase.create shared context");
                        EglRenderer.this.eglBase = EglBase.create(sharedContext, configAttributes);
                    }

                });
                this.renderThreadHandler.post(this.eglSurfaceCreationRunnable);
                long currentTimeNs = System.nanoTime();
                this.resetStatistics(currentTimeNs);
                this.renderThreadHandler.postDelayed(this.logStatisticsRunnable, TimeUnit.SECONDS.toMillis(4L));
            }
        }
    }

    public void createEglSurface(Surface surface) {
        L.i(this);
        this.createEglSurfaceInternal(surface);
    }

    public void createEglSurface(SurfaceTexture surfaceTexture) {
        L.i(this);
        this.createEglSurfaceInternal(surfaceTexture);
    }

    private void createEglSurfaceInternal(Object surface) {
//        L.i(this);
        this.eglSurfaceCreationRunnable.setSurface(surface);
        this.postToRenderThread(this.eglSurfaceCreationRunnable);
    }

    public void release() {
        L.e(this);
        L.d(getClass(), "Releasing.");
        final CountDownLatch eglCleanupBarrier = new CountDownLatch(1);
        Object var2 = this.handlerLock;
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler == null) {
                L.d(getClass(), "Already released");
                return;
            }

            this.renderThreadHandler.removeCallbacks(this.logStatisticsRunnable);
            this.renderThreadHandler.postAtFrontOfQueue(() -> {
                if (EglRenderer.this.drawer != null) {
                    EglRenderer.this.drawer.release();
                    EglRenderer.this.drawer = null;
                }

                if (EglRenderer.this.yuvTextures != null) {
                    GLES20.glDeleteTextures(3, EglRenderer.this.yuvTextures, 0);
                    EglRenderer.this.yuvTextures = null;
                }

                if (EglRenderer.this.bitmapTextureFramebuffer != null) {
                    EglRenderer.this.bitmapTextureFramebuffer.release();
                    EglRenderer.this.bitmapTextureFramebuffer = null;
                }

                if (EglRenderer.this.eglBase != null) {
                    L.d(getClass(), "eglBase detach and release.");
                    EglRenderer.this.eglBase.detachCurrent();
                    EglRenderer.this.eglBase.release();
                    EglRenderer.this.eglBase = null;
                }

                eglCleanupBarrier.countDown();
            });
            final Looper renderLooper = this.renderThreadHandler.getLooper();
            this.renderThreadHandler.post(() -> {
                L.d(getClass(), "Quitting render thread.");
                renderLooper.quit();
            });
            this.renderThreadHandler = null;
        }

        ThreadUtils.awaitUninterruptibly(eglCleanupBarrier);
        var2 = this.frameLock;
        synchronized(this.frameLock) {
            if (this.pendingFrame != null) {
                VideoRenderer.renderFrameDone(this.pendingFrame);
                this.pendingFrame = null;
            }
        }

        L.d(getClass(), "Releasing done.");
    }

    private void resetStatistics(long currentTimeNs) {
        L.i(this);
        Object var3 = this.statisticsLock;
        synchronized(this.statisticsLock) {
            this.statisticsStartTimeNs = currentTimeNs;
            this.framesReceived = 0;
            this.framesDropped = 0;
            this.framesRendered = 0;
            this.renderTimeNs = 0L;
            this.renderSwapBufferTimeNs = 0L;
        }
    }

    public void printStackTrace() {
        L.i(this);
        Object var1 = this.handlerLock;
        synchronized(this.handlerLock) {
            Thread renderThread = this.renderThreadHandler == null ? null : this.renderThreadHandler.getLooper().getThread();
            if (renderThread != null) {
                StackTraceElement[] renderStackTrace = renderThread.getStackTrace();
                if (renderStackTrace.length > 0) {
                    L.d(getClass(), "EglRenderer stack trace:");
                    StackTraceElement[] arr$ = renderStackTrace;
                    int len$ = renderStackTrace.length;

                    for(int i$ = 0; i$ < len$; ++i$) {
                        StackTraceElement traceElem = arr$[i$];
                        L.d(getClass(), traceElem.toString());
                    }
                }
            }

        }
    }

    public void setMirror(boolean mirror) {
        L.d(getClass(), ": " + mirror);
        Object var2 = this.layoutLock;
        synchronized(this.layoutLock) {
            this.mirror = mirror;
        }
    }

    public void setLayoutAspectRatio(float layoutAspectRatio) {
        L.d(getClass(), ": " + layoutAspectRatio);
        Object var2 = this.layoutLock;
        synchronized(this.layoutLock) {
            this.layoutAspectRatio = layoutAspectRatio;
        }
    }

    public void setFpsReduction(float fps) {
        L.d(getClass(), ": " + fps);
        Object var2 = this.fpsReductionLock;
        synchronized(this.fpsReductionLock) {
            long previousRenderPeriodNs = this.minRenderPeriodNs;
            if (fps <= 0.0F) {
                this.minRenderPeriodNs = 9223372036854775807L;
            } else {
                this.minRenderPeriodNs = (long)((float)TimeUnit.SECONDS.toNanos(1L) / fps);
            }

            if (this.minRenderPeriodNs != previousRenderPeriodNs) {
                this.nextFrameTimeNs = System.nanoTime();
            }

        }
    }

    public void disableFpsReduction() {
        L.i(this);
        this.setFpsReduction((float) (1.0F / 0.0));
    }

    public void pauseVideo() {
        L.i(this);
        this.setFpsReduction(0.0F);
    }

    public void addFrameListener(final EglRenderer.FrameListener listener, final float scale) {
        L.i(this);
        this.postToRenderThread(() -> EglRenderer.this.frameListeners.add(new FrameListenerAndParams(listener, scale, EglRenderer.this.drawer)));
    }

    public void addFrameListener(final EglRenderer.FrameListener listener, final float scale, final GlDrawer drawer) {
        L.i(this);
        this.postToRenderThread(() -> EglRenderer.this.frameListeners.add(new FrameListenerAndParams(listener, scale, drawer)));
    }

    public void removeFrameListener(final EglRenderer.FrameListener listener) {
        L.i(this);
        final CountDownLatch latch = new CountDownLatch(1);
        this.postToRenderThread(() -> {
            latch.countDown();
            Iterator iter = EglRenderer.this.frameListeners.iterator();

            while(iter.hasNext()) {
                if (((FrameListenerAndParams)iter.next()).listener == listener) {
                    iter.remove();
                }
            }

        });
        ThreadUtils.awaitUninterruptibly(latch);
    }

    public void renderFrame(I420Frame frame) {
        Object var2 = this.statisticsLock;
        synchronized(this.statisticsLock) {
            ++this.framesReceived;
        }

        Object var3 = this.handlerLock;
        boolean dropOldFrame;
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler == null) {
                L.d(getClass(), "Dropping frame - Not initialized or already released.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }

            Object var4 = this.fpsReductionLock;
            synchronized(this.fpsReductionLock) {
                if (this.minRenderPeriodNs > 0L) {
                    long currentTimeNs = System.nanoTime();
                    if (currentTimeNs < this.nextFrameTimeNs) {
                        L.d(getClass(), "Dropping frame - fps reduction is active.");
                        VideoRenderer.renderFrameDone(frame);
                        return;
                    } else {
                        this.nextFrameTimeNs += this.minRenderPeriodNs;
                        this.nextFrameTimeNs = Math.max(this.nextFrameTimeNs, currentTimeNs);
                    }
                }
            }

            var4 = this.frameLock;
            synchronized(this.frameLock) {
                dropOldFrame = this.pendingFrame != null;
                if (dropOldFrame) {
                    VideoRenderer.renderFrameDone(this.pendingFrame);
                }

                this.pendingFrame = frame;
                this.renderThreadHandler.post(this.renderFrameRunnable);
            }
        }

        if (dropOldFrame) {
            var3 = this.statisticsLock;
            synchronized(this.statisticsLock) {
                ++this.framesDropped;
            }
        }

    }

    public void releaseEglSurface(final Runnable completionCallback) {
        L.i(this);
        this.eglSurfaceCreationRunnable.setSurface(null);
        Object var2 = this.handlerLock;
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler != null) {
                this.renderThreadHandler.removeCallbacks(this.eglSurfaceCreationRunnable);
                this.renderThreadHandler.postAtFrontOfQueue(() -> {
                    if (EglRenderer.this.eglBase != null) {
                        EglRenderer.this.eglBase.detachCurrent();
                        EglRenderer.this.eglBase.releaseSurface();
                    }

                    completionCallback.run();
                });
                return;
            }
        }

        completionCallback.run();
    }

    private void postToRenderThread(Runnable runnable) {
        L.i(this);
        Object var2 = this.handlerLock;
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler != null) {
                this.renderThreadHandler.post(runnable);
            }

        }
    }

    private void clearSurfaceOnRenderThread() {
        L.i(this);
        if (this.eglBase != null && this.eglBase.hasSurface()) {
            L.d(getClass(), "clearSurface");
            GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GLES20.glClear(16384);
            this.eglBase.swapBuffers();
        }

    }

    public void clearImage() {
        L.i(this);
        Object var1 = this.handlerLock;
        synchronized(this.handlerLock) {
            if (this.renderThreadHandler != null) {
                this.renderThreadHandler.postAtFrontOfQueue(() -> EglRenderer.this.clearSurfaceOnRenderThread());
            }
        }
    }

    private void renderFrameOnRenderThread() {
//        L.w(this);
        Object var2 = this.frameLock;
        I420Frame frame;
        synchronized(this.frameLock) {
            if (this.pendingFrame == null) {
                return;
            }

            frame = this.pendingFrame;
            this.pendingFrame = null;
        }

        if (this.eglBase != null && this.eglBase.hasSurface()) {
            long startTimeNs = System.nanoTime();
            float[] texMatrix = RendererCommon.rotateTextureMatrix(frame.samplingMatrix, (float)frame.rotationDegree);
            Object var8 = this.layoutLock;
            float[] drawMatrix;
            int drawnFrameWidth;
            int drawnFrameHeight;
            synchronized(this.layoutLock) {
                float[] layoutMatrix;
                if (this.layoutAspectRatio > 0.0F) {
                    float frameAspectRatio = (float)frame.rotatedWidth() / (float)frame.rotatedHeight();
                    layoutMatrix = RendererCommon.getLayoutMatrix(this.mirror, frameAspectRatio, this.layoutAspectRatio);
                    if (frameAspectRatio > this.layoutAspectRatio) {
                        drawnFrameWidth = (int)((float)frame.rotatedHeight() * this.layoutAspectRatio);
                        drawnFrameHeight = frame.rotatedHeight();
                    } else {
                        drawnFrameWidth = frame.rotatedWidth();
                        drawnFrameHeight = (int)((float)frame.rotatedWidth() / this.layoutAspectRatio);
                    }
                } else {
                    layoutMatrix = this.mirror ? RendererCommon.horizontalFlipMatrix() : RendererCommon.identityMatrix();
                    drawnFrameWidth = frame.rotatedWidth();
                    drawnFrameHeight = frame.rotatedHeight();
                }
                drawMatrix = RendererCommon.multiplyMatrices(texMatrix, layoutMatrix);
            }

            GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GLES20.glClear(16384);
            if (frame.yuvFrame) {
                if (this.yuvTextures == null) {
                    this.yuvTextures = new int[3];

                    for(int i = 0; i < 3; ++i) {
                        this.yuvTextures[i] = GlUtil.generateTexture(3553);
                    }
                }

                this.yuvUploader.uploadYuvData(this.yuvTextures, frame.width, frame.height, frame.yuvStrides, frame.yuvPlanes);
                this.drawer.drawYuv(this.yuvTextures, drawMatrix, drawnFrameWidth, drawnFrameHeight, 0, 0, this.eglBase.surfaceWidth(), this.eglBase.surfaceHeight());
            } else {
                //TODO: compute size for pinch-to-zoom
                float newWidth = eglBase.surfaceWidth() * surfaceViewScale;
                float newHeight = eglBase.surfaceHeight() * surfaceViewScale;
                int newX = -(int) (Math.abs(newWidth - eglBase.surfaceWidth()) / 2.f);
                int newY = -(int) (Math.abs(newHeight - eglBase.surfaceHeight()) / 2.f);
//                L.d(getClass(), "@[%s] scale: %.2f, pos: [%d, %d] / [%d, %d]", name, surfaceViewScale, 0, 0, newX, newY);
//                L.d(getClass(), "@[%s] scale: %.2f, size: [%d, %d] / [%d, %d]", name, surfaceViewScale, eglBase.surfaceWidth(), eglBase.surfaceHeight(), newWidth, newHeight);
                this.drawer.drawOes(frame.textureId, drawMatrix, drawnFrameWidth, drawnFrameHeight, newX, newY, (int)newWidth, (int)newHeight);
            }

            long swapBuffersStartTimeNs = System.nanoTime();
            this.eglBase.swapBuffers();
            long currentTimeNs = System.nanoTime();
            Object var12 = this.statisticsLock;
            synchronized(this.statisticsLock) {
                ++this.framesRendered;
                this.renderTimeNs += currentTimeNs - startTimeNs;
                this.renderSwapBufferTimeNs += currentTimeNs - swapBuffersStartTimeNs;
            }

            this.notifyCallbacks(frame, texMatrix);
            VideoRenderer.renderFrameDone(frame);
        } else {
            L.d(getClass(), "Dropping frame - No surface");
            VideoRenderer.renderFrameDone(frame);
        }
    }

    private void notifyCallbacks(I420Frame frame, float[] texMatrix) {
//        L.i(getClass(), "threadId: "+Thread.currentThread().getId());
        if (!this.frameListeners.isEmpty()) {
            ArrayList<FrameListenerAndParams> tmpList = new ArrayList(this.frameListeners);
//            this.frameListeners.clear();
            float[] bitmapMatrix = RendererCommon.multiplyMatrices(RendererCommon.multiplyMatrices(texMatrix,
                    this.mirror ? RendererCommon.horizontalFlipMatrix() : RendererCommon.identityMatrix()), RendererCommon.verticalFlipMatrix());
            Iterator i$ = tmpList.iterator();

//            while(true) {
            while (i$.hasNext()) {

                EglRenderer.FrameListenerAndParams listenerAndParams = (EglRenderer.FrameListenerAndParams) i$.next();
                int scaledWidth = (int) (listenerAndParams.scale * (float) frame.rotatedWidth());
                int scaledHeight = (int) (listenerAndParams.scale * (float) frame.rotatedHeight());
                if (scaledWidth != 0 && scaledHeight != 0) {

                    if (this.bitmapTextureFramebuffer == null) {
                        this.bitmapTextureFramebuffer = new GlTextureFrameBuffer(6408);
                    }

                    this.bitmapTextureFramebuffer.setSize(scaledWidth, scaledHeight);
                    GLES20.glBindFramebuffer(36160, this.bitmapTextureFramebuffer.getFrameBufferId());
                    GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.bitmapTextureFramebuffer.getTextureId(), 0);
                    if (frame.yuvFrame) {
                        listenerAndParams.drawer.drawYuv(this.yuvTextures, bitmapMatrix, frame.rotatedWidth(), frame.rotatedHeight(),
                                0, 0, scaledWidth, scaledHeight);
                    } else {
                        listenerAndParams.drawer.drawOes(frame.textureId, bitmapMatrix, frame.rotatedWidth(), frame.rotatedHeight(),
                                0, 0, scaledWidth, scaledHeight);
                    }

                    ByteBuffer bitmapBuffer = ByteBuffer.allocateDirect(scaledWidth * scaledHeight * 4);
                    GLES20.glViewport(0, 0, scaledWidth, scaledHeight);
                    GLES20.glReadPixels(0, 0, scaledWidth, scaledHeight, 6408, 5121, bitmapBuffer);
                    GLES20.glBindFramebuffer(36160, 0);
                    GlUtil.checkNoGLES2Error("EglRenderer.notifyCallbacks");
                    Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(bitmapBuffer);
                    listenerAndParams.listener.onFrame(bitmap);
                } else {
                    listenerAndParams.listener.onFrame(null);
                }
            }
//                return;
//            }
        }
    }

    private String averageTimeAsString(long sumTimeNs, int count) {
//        L.i(this);
        return count <= 0 ? "NA" : TimeUnit.NANOSECONDS.toMicros(sumTimeNs / (long)count) + " μs";
    }

    private void logStatistics() {
//        L.w(this);
        long currentTimeNs = System.nanoTime();
        synchronized(this.statisticsLock) {
            long elapsedTimeNs = currentTimeNs - this.statisticsStartTimeNs;
            if (elapsedTimeNs > 0L) {
                float renderFps = (float)((long)this.framesRendered * TimeUnit.SECONDS.toNanos(1L)) / (float)elapsedTimeNs;
                L.d(getClass(), "[%s] Duration: %d ms / Frames received: %d / Dropped: %d / Rendered: %d / Render fps: %s /  Average render time: %s / Average swapBuffer time: %s",
                        name, TimeUnit.NANOSECONDS.toMillis(elapsedTimeNs), this.framesReceived, this.framesDropped,  this.framesRendered, String.format("%.1f", renderFps), this.averageTimeAsString(this.renderTimeNs, this.framesRendered), this.averageTimeAsString(this.renderSwapBufferTimeNs, this.framesRendered));
                this.resetStatistics(currentTimeNs);
            }
        }
    }

    //TODO: add setter/getter for pinch-to-zoom
    public void setSurfaceViewScale(final float scale) {
        this.surfaceViewScale = scale;
    }

    public float getSurfaceViewScale() {
        return surfaceViewScale;
    }

    private class EglSurfaceCreation implements Runnable {
        private Object surface;

        private EglSurfaceCreation() {
        }

        public synchronized void setSurface(Object surface) {
            this.surface = surface;
        }

        public synchronized void run() {
            L.w(this);
            if (this.surface != null && EglRenderer.this.eglBase != null && !EglRenderer.this.eglBase.hasSurface()) {
                if (this.surface instanceof Surface) {
                    EglRenderer.this.eglBase.createSurface((Surface)this.surface);
                } else {
                    if (!(this.surface instanceof SurfaceTexture)) {
                        throw new IllegalStateException("Invalid surface: " + this.surface);
                    }

                    EglRenderer.this.eglBase.createSurface((SurfaceTexture)this.surface);
                }

                EglRenderer.this.eglBase.makeCurrent();
                GLES20.glPixelStorei(3317, 1);
            }

        }
    }

    private static class FrameListenerAndParams {
        public final EglRenderer.FrameListener listener;
        public final float scale;
        public final GlDrawer drawer;

        public FrameListenerAndParams(EglRenderer.FrameListener listener, float scale, GlDrawer drawer) {
            this.listener = listener;
            this.scale = scale;
            this.drawer = drawer;
        }
    }

    public interface FrameListener {
        void onFrame(Bitmap var1);
    }
}
