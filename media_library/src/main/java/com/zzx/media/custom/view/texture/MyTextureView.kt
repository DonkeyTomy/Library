package com.zzx.media.custom.view.texture

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.util.AttributeSet
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import androidx.annotation.Keep
import androidx.annotation.NonNull
import com.zzx.media.camera.ICameraManager
import com.zzx.media.custom.view.camera.ISurfaceView
import timber.log.Timber


/**@author Tomy
 * Created by Tomy on 2017/9/11.
 */
class MyTextureView: TextureView, TextureView.SurfaceTextureListener, ISurfaceView<SurfaceTexture, CameraDevice>, SurfaceTexture.OnFrameAvailableListener {

    private var mPreviewWidth = 1280

    private var mPreviewHeight = 720

    private var mLayoutParamsWidth  = 0

    private var mLayoutParamsHeight = 0

    private var mOnFrameAvailableListener: SurfaceTexture.OnFrameAvailableListener? = null

    private var mTextureListener: SurfaceTextureListener? = null

    private var mSurfaceStateCallback: ISurfaceView.StateCallback<SurfaceTexture>? = null

    private var mRotation = 0

    constructor(context: Context, attributes: AttributeSet?): super(context, attributes) {
        if (Timber.treeCount() == 0) {
            Timber.plant(Timber.DebugTree())
        }
        surfaceTextureListener = this
    }

    constructor(context: Context): this(context, null)

    override fun setStateCallback(stateCallback: ISurfaceView.StateCallback<SurfaceTexture>) {
        mSurfaceStateCallback = stateCallback
    }

    override fun setCameraManager(cameraManager: ICameraManager<SurfaceTexture, CameraDevice>) {
    }

    override fun setPreviewSize(width: Int, height: Int) {
        mPreviewWidth   = width
        mPreviewHeight  = height
        mPreviewSize = Size(width, height)
    }

    override fun setRotation(rotation: Int) {
        mRotation = rotation
    }

    override fun setLayoutParams(width: Int, height: Int) {
        mLayoutParamsWidth  = width
        mLayoutParamsHeight = height
    }

    override fun initParams() {
        if (mPreviewWidth == 0 || mPreviewHeight == 0) {
            mPreviewWidth   = mLayoutParamsWidth
            mPreviewHeight  = mLayoutParamsHeight
        }
    }

    override fun startPreview() {
    }

    override fun stopPreview() {
    }

    override fun release() {
    }

    override fun setCamera(@NonNull camera: CameraDevice) {
    }

    fun setCustomFrameAvailable(listener: SurfaceTexture.OnFrameAvailableListener) {
        mOnFrameAvailableListener = listener
    }

    fun setCustomTextureListener(listener: MySurfaceTextureListener) {
        mTextureListener = listener
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
//        surfaceTexture?.updateTexImage()
        mOnFrameAvailableListener?.onFrameAvailable(surfaceTexture)
        Timber.e("onFrameAvailable")
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        mSurfaceStateCallback?.onSurfaceSizeChange(surface, width, height)
        mTextureListener?.onSurfaceTextureSizeChanged(surface, width, height)
        configureTransform(mPreviewSize!!, mRotation, width, height)
        Timber.e("onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        mTextureListener?.onSurfaceTextureUpdated(surface)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        Timber.e("onSurfaceTextureDestroyed")
        mTextureListener?.onSurfaceTextureDestroyed(surface)
        mSurfaceStateCallback?.onSurfaceDestroyed(surface)
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Timber.e("onSurfaceTextureAvailable")
        mTextureListener?.onSurfaceTextureAvailable(surface, width, height)
        mSurfaceStateCallback?.onSurfaceCreate(surface)
        if (mLayoutParamsWidth == 0 || mLayoutParamsHeight == 0) {
            mLayoutParamsWidth  = width
            mLayoutParamsHeight = height
        }
        configureTransform(mPreviewSize!!, mRotation, width, height)
    }

    private var mPreviewSize: Size? = null

    fun configureTransform(previewSize: Size, rotation: Int, viewWidth: Int, viewHeight: Int) {
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = (viewHeight.toFloat() / previewSize.height).coerceAtLeast(viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                postScale(scale, scale, centerX, centerY)
                postRotate(90 * (rotation - 2f), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        Timber.e("previewSize[${previewSize.width}x${previewSize.height}] view[${viewWidth}x$viewHeight rotation = $rotation]")
        setTransform(matrix)
    }

    private var ratioWidth: Int     = 0

    private var ratioHeight: Int    = 0

    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
            }
        }
    }

    @Keep
    abstract class MySurfaceTextureListener: SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }


    }


    companion object {
        val ORIENTATIONS = SparseIntArray()
        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

    }
}