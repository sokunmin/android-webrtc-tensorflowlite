package org.appspot.apprtc.webrtc;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import org.appspot.apprtc.util.L;
import org.webrtc.EglBase;
import org.webrtc.GlRectDrawer;
import org.webrtc.RendererCommon.GlDrawer;
import org.webrtc.RendererCommon.RendererEvents;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.RendererCommon.VideoLayoutMeasure;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoRenderer.I420Frame;

import java.util.concurrent.CountDownLatch;

public class SurfaceViewRenderer extends SurfaceView implements Callback, Callbacks {
    private static final String TAG = "SurfaceViewRenderer";
    private final String resourceName = this.getResourceName();
    private final VideoLayoutMeasure videoLayoutMeasure = new VideoLayoutMeasure();
    private final EglRenderer eglRenderer;
    private RendererEvents rendererEvents;
    private final Object layoutLock = new Object();
    private boolean isFirstFrameRendered;
    private int rotatedFrameWidth;
    private int rotatedFrameHeight;
    private int frameRotation;
    private boolean enableFixedSize;
    private int surfaceWidth;
    private int surfaceHeight;

    //TODO: pinch-to-zoom
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Context context;
    private boolean isSingleTouch;
    private float zoomScale = 1f;
    private float minScale = 1f;
    private float maxScale = 2.5f;
    private float dX, dY;
    private static final boolean MOVE_VIEW = false;
    private ValueAnimator zoomAnimator;
    private boolean isZoomed = false;
    private boolean isMovable = false;

    public SurfaceViewRenderer(Context context) {
        super(context);
        this.context = context;
        this.eglRenderer = new EglRenderer(this.resourceName);
        this.getHolder().addCallback(this);
    }

    public SurfaceViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.eglRenderer = new EglRenderer(this.resourceName);
        this.getHolder().addCallback(this);
    }

    public void init(EglBase.Context sharedContext, RendererEvents rendererEvents) {
        this.init(sharedContext, rendererEvents, EglBase.CONFIG_PLAIN, new GlRectDrawer());
    }

    public void init(EglBase.Context sharedContext, RendererEvents rendererEvents, int[] configAttributes, GlDrawer drawer) {
        ThreadUtils.checkIsOnMainThread();

        Object var5 = this.layoutLock;
        synchronized(this.layoutLock) {
            this.rotatedFrameWidth = 0;
            this.rotatedFrameHeight = 0;
            this.frameRotation = 0;
        }

        this.eglRenderer.init(sharedContext, configAttributes, drawer);
        this.rendererEvents = rendererEvents;

        //TODO: pinch-to-zoom / double-tap to zoom
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

    }

    public void release() {
        this.eglRenderer.release();
    }

    public void addFrameListener(EglRenderer.FrameListener listener, float scale, GlDrawer drawer) {
        this.eglRenderer.addFrameListener(listener, scale, drawer);
    }

    public void addFrameListener(EglRenderer.FrameListener listener, float scale) {
        this.eglRenderer.addFrameListener(listener, scale);
    }

    public void removeFrameListener(EglRenderer.FrameListener listener) {
        this.eglRenderer.removeFrameListener(listener);
    }

    public void setEnableHardwareScaler(boolean enabled) {
        ThreadUtils.checkIsOnMainThread();
        this.enableFixedSize = enabled;
        this.updateSurfaceSize();
    }

    public void setMirror(boolean mirror) {
        this.eglRenderer.setMirror(mirror);
    }

    public void setScalingType(ScalingType scalingType) {
        ThreadUtils.checkIsOnMainThread();
        this.videoLayoutMeasure.setScalingType(scalingType);
    }

    public void setScalingType(ScalingType scalingTypeMatchOrientation, ScalingType scalingTypeMismatchOrientation) {
        ThreadUtils.checkIsOnMainThread();
        this.videoLayoutMeasure.setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation);
    }

    public void setFpsReduction(float fps) {
        this.eglRenderer.setFpsReduction(fps);
    }

    public void disableFpsReduction() {
        this.eglRenderer.disableFpsReduction();
    }

    public void pauseVideo() {
        this.eglRenderer.pauseVideo();
    }

    public void renderFrame(I420Frame frame) {
        this.updateFrameDimensionsAndReportEvents(frame);
        this.eglRenderer.renderFrame(frame);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        ThreadUtils.checkIsOnMainThread();
        Object var4 = this.layoutLock;
        Point size;
        synchronized(this.layoutLock) {
            size = this.videoLayoutMeasure.measure(widthSpec, heightSpec, this.rotatedFrameWidth, this.rotatedFrameHeight);
        }

        this.setMeasuredDimension(size.x, size.y);
        L.d(getClass(), "[%s] New size: %dx%d", this.resourceName, size.x, size.y);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ThreadUtils.checkIsOnMainThread();
//        L.w(getClass(), "PID: " + Thread.currentThread().getId());

//        if (width == 0 && height == 0) {
//            width = SurfaceViewRenderer.this.getWidth();
//            height = SurfaceViewRenderer.this.getHeight();
//            this.left = left;
//            this.right = right;
//            this.top = top;
//            this.bottom = bottom;
//        }
        this.eglRenderer.setLayoutAspectRatio((float)(right - left) / (float)(bottom - top));
        this.updateSurfaceSize();
    }

    private void updateSurfaceSize() {
        ThreadUtils.checkIsOnMainThread();
        L.w(getClass(), "[%s] PID: %d", resourceName, Thread.currentThread().getId());
        Object var1 = this.layoutLock;
        synchronized(this.layoutLock) {
            if (this.enableFixedSize && this.rotatedFrameWidth != 0 && this.rotatedFrameHeight != 0 && this.getWidth() != 0 && this.getHeight() != 0) {
                float layoutAspectRatio = (float)this.getWidth() / (float)this.getHeight();
                float frameAspectRatio = (float)this.rotatedFrameWidth / (float)this.rotatedFrameHeight;
                L.d(getClass(), "[%s] LayoutSize: getWidth(%d) / getHeight(%d) = %.2f", resourceName, getWidth(), getHeight(), layoutAspectRatio);
                L.d(getClass(), "[%s] FrameSize: getWidth(%d) / getHeight(%d) = %.2f", resourceName, rotatedFrameWidth, rotatedFrameHeight, frameAspectRatio);
                int drawnFrameWidth;
                int drawnFrameHeight;
                if (frameAspectRatio > layoutAspectRatio) { //frameAspectRatio: width > height
                    drawnFrameWidth = (int)((float)this.rotatedFrameHeight * layoutAspectRatio);
                    drawnFrameHeight = this.rotatedFrameHeight;
                } else {
                    drawnFrameWidth = this.rotatedFrameWidth; //frameAspectRatio: height > width
                    drawnFrameHeight = (int)((float)this.rotatedFrameWidth / layoutAspectRatio);
                }
                L.d(getClass(), "[%s] DrawnSize: getWidth(%d) / getHeight(%d) = %.2f", resourceName, drawnFrameWidth, drawnFrameHeight, ((float)drawnFrameWidth / drawnFrameHeight));
                int width = Math.min(this.getWidth(), drawnFrameWidth);
                int height = Math.min(this.getHeight(), drawnFrameHeight);
                L.d(getClass(), "[%s] NewSurfaceSize: getWidth(%d) / getHeight(%d) = %.2f", resourceName, width, height, ((float)width / height));
                if (width != this.surfaceWidth || height != this.surfaceHeight) {
                    this.surfaceWidth = width;
                    this.surfaceHeight = height;
                    this.getHolder().setFixedSize(width, height);
                }
            } else {
                this.surfaceWidth = this.surfaceHeight = 0;
                this.getHolder().setSizeFromLayout();
            }

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        ThreadUtils.checkIsOnMainThread();
        L.w(getClass(), "PID: " + Thread.currentThread().getId());

        this.eglRenderer.createEglSurface(holder.getSurface());
        this.surfaceWidth = this.surfaceHeight = 0;
        this.updateSurfaceSize();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        ThreadUtils.checkIsOnMainThread();
        final CountDownLatch completionLatch = new CountDownLatch(1);
        this.eglRenderer.releaseEglSurface(() -> completionLatch.countDown());
        ThreadUtils.awaitUninterruptibly(completionLatch);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ThreadUtils.checkIsOnMainThread();
        L.d("format: %d size: %dx%d", format, width, height);
    }

    private String getResourceName() {
        try {
            return this.getResources().getResourceEntryName(this.getId());
        } catch (NotFoundException var2) {
            return "";
        }
    }

    private void updateFrameDimensionsAndReportEvents(I420Frame frame) {
        Object var2 = this.layoutLock;
        synchronized(this.layoutLock) {
            if (!this.isFirstFrameRendered) {
                this.isFirstFrameRendered = true;
                L.d(getClass(), "[%s] Reporting first rendered frame.", this.resourceName);
                if (this.rendererEvents != null) {
                    this.rendererEvents.onFirstFrameRendered();
                }
            }

            if (this.rotatedFrameWidth != frame.rotatedWidth() ||
                this.rotatedFrameHeight != frame.rotatedHeight() ||
                this.frameRotation != frame.rotationDegree) {
                L.d(getClass(), "[%s] Reporting frame resolution changed to %dx%d with rotation %d", this.resourceName, frame.width, frame.height, frame.rotationDegree);
                if (this.rendererEvents != null) {
                    this.rendererEvents.onFrameResolutionChanged(frame.width, frame.height, frame.rotationDegree);
                }
                L.d(getClass(), "[%s] Frame rotated size: %dx%d", resourceName, frame.rotatedWidth(), frame.rotatedHeight());
                this.rotatedFrameWidth = frame.rotatedWidth();
                this.rotatedFrameHeight = frame.rotatedHeight();
                this.frameRotation = frame.rotationDegree;
                this.post(() -> {
                    this.updateSurfaceSize();
                    this.requestLayout();
                });
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        if (isMovable) {
            if (event.getPointerCount() > 1) {
                isSingleTouch = false;
            } else {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isSingleTouch = true;
                }
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = this.getX() - event.getRawX();
                    dY = this.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (isSingleTouch) {
                        this.animate().x(event.getRawX() + dX)
                                      .y(event.getRawY() + dY)
                                      .setDuration(0)
                                      .start();
    //                        checkDimension(this);
                    }
                    break;
                default:
                    return true;
            }
        }
        return true;
    }

    public void setViewMovable(boolean enabled) {
        this.isMovable = enabled;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (zoomAnimator != null && zoomAnimator.isRunning())
                zoomAnimator.cancel();
            zoomScale *= detector.getScaleFactor();
            zoomScale = Math.max(minScale, Math.min(zoomScale, maxScale));

            //FIXME: this line does not make pinch-to-zoom work well.
//            animate().scaleX(scale).scaleY(scale).setDuration(0).start();
            eglRenderer.setSurfaceViewScale(zoomScale);
            isZoomed = zoomScale > minScale;
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e){
            L.e("GestureListener");
            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            L.e("GestureListener");
            performLongClick();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            L.e("GestureListener");
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            L.e("GestureListener");
            if (zoomAnimator != null && zoomAnimator.isRunning())
                zoomAnimator.cancel();

            float start = (isZoomed) ? zoomScale : minScale;
            float end = (isZoomed) ? minScale : Math.max(zoomScale, maxScale);
            zoomAnimator = ValueAnimator.ofFloat(start, end);
            zoomAnimator.addUpdateListener(valueAnimator -> {
//                L.e("[GestureListener] animScale: %.2f", valueAnimator.getAnimatedValue());
                zoomScale = (float) valueAnimator.getAnimatedValue();
                eglRenderer.setSurfaceViewScale(zoomScale);
                isZoomed = zoomScale > minScale;
            });
            zoomAnimator.setDuration(300);
            zoomAnimator.start();

            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            L.e("GestureListener");
            return false;
        }
    }


    /*
    private void checkDimension(View vi) {
        if (vi.getX() > left) {
            vi.animate().x(left).y(vi.getY()).setDuration(0).start();
        }

        if ((vi.getWidth() + vi.getX()) < right) {
            vi.animate().x(right - vi.getWidth()).y(vi.getY()).setDuration(0).start();
        }

        if (vi.getY() > top) {
            vi.animate().x(vi.getX()).y(top).setDuration(0).start();
        }

        if ((vi.getHeight() + vi.getY()) < bottom) {
            vi.animate().x(vi.getX()).y(bottom - vi.getHeight()).setDuration(0).start();
        }
    }*/
}
