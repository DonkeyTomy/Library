package com.zzx.media.custom.view.opengl.egl

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.zzx.utils.file.AssetsUtils
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**@author Tomy
 * Created by Tomy on 2019/11/19.
 */
object GLUtil {

    /**
     *
     * @param context Context
     * @param vertexName String 必须存放在assets目录下
     * @param fragmentName String 必须存放在assets目录下
     * @return Int
     */
    fun createProgram(context: Context, vertexName: String, fragmentName: String): Int {
        val vertexSource    = AssetsUtils.read(context, DIR_SHADER, vertexName)
        val fragmentSource  = AssetsUtils.read(context, DIR_SHADER, fragmentName)
        return createProgram(vertexSource!!, fragmentSource!!)
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader    = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        checkError("create Program")
        if (program == 0) {
            Timber.e("Could not create program.")
        }
        GLES20.glAttachShader(program, vertexShader)
        checkError("glAttachVertexShader")

        GLES20.glAttachShader(program, fragmentShader)
        checkError("glAttachFragmentShader")
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Timber.e("Link Program failed: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
    }


    /**
     * 加载渲染器
     * @param shaderType 渲染器类型.顶点着色器[GLES20.GL_VERTEX_SHADER] / 片段着色器[GLES20.GL_FRAGMENT_SHADER]
     * @param shaderSource String
     * @return Int 渲染器句柄
     */
    fun loadShader(shaderType: Int, shaderSource: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        GLES20.glShaderSource(shader, shaderSource)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Timber.e("Compiled shader $shaderType failed: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    fun checkError(msg: String) {
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            throw RuntimeException(msg)
        }
    }

    fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Unable to locate '$label' in program")
        }
    }

    /**
     * Creates a texture from raw data.
     * 为图片数据创建TextureID.
     * @param data ByteBuffer 原始图片数据
     * @param width Int
     * @param height Int
     * @param format Int
     */
    fun createImageTexture(data: ByteBuffer, width: Int, height: Int, format: Int): Int {
        val textureHandles = IntArray(1)

        GLES20.glGenTextures(1, textureHandles, 0)
        checkError("glGenTextures")
        val textureID = textureHandles[0]
        //Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        /**
         * Config min/mag filtering.当我们要渲染比原图片更大或者更小时用来缩放的方法.
         */
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        //将图像数据加载到纹理句柄中.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format,
                width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, data)
        checkError("loadImageTexture")
        return textureID
    }

    /**
     * 创建纹理并返回纹理ID.
     * @return Int
     */
    fun createTextureObject(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val textureID = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        checkError("glBindTexture: $textureID")

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        checkError("glTexParameter")
        return textureID
    }

    /**
     * 将坐标数据转换为底层调用的FloatBuffer.
     * @param coords FloatArray
     * @return FloatBuffer
     */
    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }



    internal var IDENTITY_MATRIX = FloatArray(16)

    init {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)
    }

    const val SIZEOF_FLOAT = 4
    const val DIR_SHADER = "shader"

}