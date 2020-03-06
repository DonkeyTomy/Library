package com.zzx.media.custom.view.opengl.egl

/**@author Tomy
 * Created by Tomy on 2020/3/4.
 */
class FullFrameRect(private var mProgram: Texture2DProgram?) {

    private val mRectDrawable = Drawable2D(Drawable2D.Prefab.FULL_RECTANGLE)

    fun release(doEGLCleanup: Boolean) {
        mProgram?.apply {
            if (doEGLCleanup) {
                release()
            }
            mProgram = null
        }
    }

    fun getProgram() = mProgram

    /**
     * 释放之前的程序,替换为指定程序.
     * PS:
     * 必须在makeCurrent之后调用
     * @param program Texture2DProgram
     */
    fun changeProgram(program: Texture2DProgram) {
        mProgram?.release()
        mProgram = program
    }

    fun createTextureObject() = mProgram?.createTextureObject() ?: -1

    /**
     * 绘制一个矩形窗口.渲染到指定的纹理对象中.
     * @param textureId Int
     * @param texMatrix FloatArray
     */
    fun drawFrame(textureId: Int, texMatrix: FloatArray) {
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        mProgram?.draw(GLUtil.IDENTITY_MATRIX, mRectDrawable.getVertexArray(), 0,
            mRectDrawable.getVertexCount(), mRectDrawable.getCoordPerVertex(),
            mRectDrawable.getVertexStride(), texMatrix,
            mRectDrawable.getTexCoordArray(), textureId,
            mRectDrawable.getTexCoordStride())
    }

}