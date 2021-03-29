package com.zzx.media.custom.view.opengl.egl

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import timber.log.Timber
import java.nio.FloatBuffer

/**@author Tomy
 * Created by Tomy on 2019/11/19.
 * GL program.提供2D纹理模型的方法.
 */
class Texture2DProgram(var mProgramType: ProgramType = ProgramType.TEXTURE_2D, context: Context) {

    enum class ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BW
    }


    private var mProgramHandle  = 0
    private var muMVPMatrixLoc  = 0
    private var muTexMatrixLoc  = 0
    private var muKernelLoc     = 0
    private var muTexOffsetLoc  = 0
    private var muColorAdjustLoc= 0
    private var maPositionLoc   = 0
    private var maTextureCoordLoc= 0

    private var mTextureTarget   = 0

    private var mKernel = FloatArray(KERNEL_SIZE)
    private var mTexOffset: FloatArray? = null
    private var mColorAdjust = 0f

    init {
        when (mProgramType) {
            ProgramType.TEXTURE_2D -> {
                mTextureTarget   = GLES20.GL_TEXTURE_2D
                mProgramHandle  = GLUtil.createProgram(context, VERTEX_SHADER, FRAGMENT_SHADER)
            }
            ProgramType.TEXTURE_EXT -> {
                mTextureTarget   = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
                mProgramHandle  = GLUtil.createProgram(context, VERTEX_SHADER, FRAGMENT_SHADER_EXT)
            }
            ProgramType.TEXTURE_EXT_BW -> {
                mProgramHandle  = GLUtil.createProgram(context, VERTEX_SHADER, FRAGMENT_SHADER_EXT_BW)
            }
        }
        if (mProgramHandle == 0) {
            throw RuntimeException("unable to create Program for type : $mProgramType")
        }
        Timber.i("create Program: $mProgramHandle for type($mProgramType)")

        /**
         * 获取各个属性的值
         */
        maPositionLoc   = GLES20.glGetAttribLocation(mProgramHandle, KEY_POSITION)
        GLUtil.checkLocation(maPositionLoc, KEY_POSITION)

        maTextureCoordLoc   = GLES20.glGetAttribLocation(mProgramHandle, KEY_TEXTURE_COORD)
        GLUtil.checkLocation(maTextureCoordLoc, KEY_TEXTURE_COORD)

        muMVPMatrixLoc  = GLES20.glGetUniformLocation(mProgramHandle, KEY_MVP_MATRIX)
        GLUtil.checkLocation(muMVPMatrixLoc, KEY_MVP_MATRIX)

        muTexMatrixLoc  = GLES20.glGetUniformLocation(mProgramHandle, KEY_TEX_MATRIX)
        GLUtil.checkLocation(muTexMatrixLoc, KEY_TEX_MATRIX)

        muKernelLoc     = GLES20.glGetUniformLocation(mProgramHandle, KEY_KERNEL)

        if (muKernelLoc < 0) {
            //no kernel in this one.
            muKernelLoc = -1
            muTexOffsetLoc  = -1
            muColorAdjustLoc    = -1
        } else {
            //has kernel, must also have tex offset and color adj
            muTexOffsetLoc  = GLES20.glGetUniformLocation(mProgramHandle, KEY_TEX_OFFSET)
            GLUtil.checkLocation(muTexOffsetLoc, KEY_TEX_OFFSET)

            muColorAdjustLoc= GLES20.glGetUniformLocation(mProgramHandle, KEY_COLOR_ADJUST)
            GLUtil.checkLocation(muColorAdjustLoc, KEY_COLOR_ADJUST)

            setKernel(floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f), 0f)
            setTexSize(256, 256)
        }
    }

    /**
     * Releases the program.
     * 用来创建此Program的EGLContext必须是当前正在使用的.
     */
    fun release() {
        Timber.i("release program: $mProgramHandle")
        GLES20.glDeleteProgram(mProgramHandle)
        mProgramHandle  = -1
    }

    fun getProgramType() = mProgramType

    /**
     * 创建并绑定一个当前Program合适的Texture对象
     * @return Int
     */
    fun createTextureObject(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val textureID = textures[0]
        GLES20.glBindTexture(mTextureTarget, textureID)
        GLUtil.checkError("glBindTexture: $textureID")

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLUtil.checkError("glTexParameter")
        return textureID
    }

    /**
     * 配置过滤器的值.
     * @param values FloatArray
     * @param colorAdj Float
     */
    fun setKernel(values: FloatArray, colorAdj: Float) {
        if (values.size != KERNEL_SIZE) {
            throw IllegalArgumentException("kernel size is ${values.size}. But need $KERNEL_SIZE")
        }
        System.arraycopy(values, 0, mKernel, 0, KERNEL_SIZE)
        mColorAdjust = colorAdj
    }


    /**
     * 设置纹理的大小.这是用来在过滤的时候寻找合适的texels
     * @param width Int
     * @param height Int
     */
    fun setTexSize(width: Int, height: Int) {
        val rw = 1f / width
        val rh = 1f / height

        //Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset  = floatArrayOf(
                -rw, -rh,   0f, -rh,    rw, -rh,
                -rw, 0f,    0f, 0f,     rw, 0f,
                -rw, rh,    0f, rh,     rw, rh
        )
    }

    private var count = 2

    /**
     * 渲染调用.每次调用都执行完整配置.
     * @param mvpMatrix FloatArray      4x4的投影矩阵.
     * @param vertexBuff FloatBuffer    顶点数据的缓存.
     * @param firstVertex Int           顶点缓存数据中使用的第一个顶点索引.
     * @param vertexCount Int           顶点缓存数据中保存的顶点个数.
     * @param coordsPerVertex Int       每一个顶点坐标所包含的坐标数.(如x,y是2个; x,y,z是3个)
     * @param vertexStride Int          每一个顶点的位置数据的宽度,单位是byte.(通常是[vertexCount] * sizeof(Float))
     * @param texMatrix FloatArray      一个给纹理坐标使用的4x4的转换矩阵.(主要是用在使用SurfaceTexture)
     * @param texBuffer FloatBuffer     顶点纹理数据的缓存
     * @param textureID Int
     * @param texStride Int             每一个顶点的纹理数据的宽度,单位是byte.
     */
    fun draw(mvpMatrix: FloatArray, vertexBuff: FloatBuffer,
             firstVertex: Int, vertexCount: Int,
             coordsPerVertex: Int, vertexStride: Int,
             texMatrix: FloatArray, texBuffer: FloatBuffer,
             textureID: Int, texStride: Int) {
        if (count > 0) {
            count--
            Timber.i("mvpMatrix: $mvpMatrix")
            Timber.i("vertexBuff: $vertexBuff")
            Timber.i("firstVertex: $firstVertex")
            Timber.i("texMatrix: $texMatrix")
            Timber.i("texBuffer: $texBuffer")
            Timber.i("textureID: $textureID")
        }

        GLUtil.checkError("draw start")
        //选择Program
        GLES20.glUseProgram(mProgramHandle)
        GLUtil.checkError("glUseProgram")

        //设置绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(mTextureTarget, textureID)

        //Copy the model / view / projection matrix over.
        //拷贝 模型/视图/投影 矩阵.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GLUtil.checkError("glUniformMatrix4fv muTexMatrixLoc")

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0)
        GLUtil.checkError("glUniformMatrix4fv muTexMatrixLoc")

        //Enable the "aPosition" vertex attribute.
        //启用aPosition纹理属性
        GLES20.glEnableVertexAttribArray(maPositionLoc)
        GLUtil.checkError("glEnableVertexAttribArray.maPositionLoc")

        //Connect vertexBuff to "aPosition"
        //纹理缓存连接到aPosition上
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuff)
        GLUtil.checkError("glVertexAttribPointer.maPositionLoc")

        //启用并关联"aTextureCoord"纹理属性
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc)
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, texStride, texBuffer)
        GLUtil.checkError("glVertexAttribPointer.maTextureCoordLoc")

        //若提供了kernel则填充
        if (muKernelLoc >= 0) {
            GLES20.glUniform1fv(muKernelLoc, KERNEL_SIZE, mKernel, 0)
            GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0)
            GLES20.glUniform1f(muColorAdjustLoc, mColorAdjust)
        }

        //渲染区域
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        GLUtil.checkError("glDrawArrays")

        //完成 -- 停止 顶点数组,纹理及程序
        GLES20.glDisableVertexAttribArray(maPositionLoc)
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc)
        GLES20.glBindTexture(mTextureTarget, 0)
        GLES20.glUseProgram(0)

    }


    companion object {
        const val KERNEL_SIZE = 9

        const val VERTEX_SHADER = "vertex_shader.glsl"
        const val FRAGMENT_SHADER   = "fragment_shader_2d.glsl"
        const val FRAGMENT_SHADER_EXT   = "fragment_shader_ext.glsl"
        const val FRAGMENT_SHADER_EXT_BW   = "fragment_shader_ext_bw.glsl"

        const val KEY_POSITION  = "aPosition"
        const val KEY_TEXTURE_COORD = "aTextureCoord"
        const val KEY_MVP_MATRIX    = "uMVPMatrix"
        const val KEY_TEX_MATRIX    = "uTexMatrix"
        const val KEY_KERNEL        = "uKernel"
        const val KEY_TEX_OFFSET    = "uTexOffset"
        const val KEY_COLOR_ADJUST  = "uColorAdjust"
    }
}