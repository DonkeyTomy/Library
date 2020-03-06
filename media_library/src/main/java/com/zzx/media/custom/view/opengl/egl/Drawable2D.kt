package com.zzx.media.custom.view.opengl.egl

import java.nio.FloatBuffer

/**@author Tomy
 * Created by Tomy on 2019/11/19.
 * Base class for stuff(材料/素材) we like to draw.
 */
class Drawable2D {
    companion object {
        const val SIZEOF_FLOAT = 4

        /**
         * Simple equilateral triangle (1.0 per side).  Centered on (0,0).
         * 简单的等边 三角形(每一边长1.0).中心点是(0,0)
         */
        private val TRIANGLE_COORDS = floatArrayOf(
            0.0f, 0.577350269f,  // 0 top
            -0.5f, -0.288675135f,  // 1 left bottom
            0.5f, -0.288675135f // 2 right bottom
        )
        private val TRIANGLE_TEX_COORDS = floatArrayOf(
            0.5f, 0.0f,  // 0 top center
            0.0f, 1.0f,  // 1 left bottom
            1.0f, 1.0f
        )
        private val TRIANGLE_VERTEX_BUF: FloatBuffer = GLUtil.createFloatBuffer(TRIANGLE_COORDS)
        private val TRIANGLE_TEX_BUF: FloatBuffer = GLUtil.createFloatBuffer(TRIANGLE_TEX_COORDS)

        /**
         * Simple square, specified as a triangle strip.  The square is centered on (0,0) and has
         * a size of 1x1.
         *
         * 简单的正方形.
         * (0,0) (1,0)
         * (0,1) (1,1)
         *
         * Triangles are 0-1-2 and 2-1-3 (counter-clockwise winding).
         *
         */
        private val RECTANGLE_COORDS = floatArrayOf(
            -0.5f, -0.5f,   // 0 left top 左上
            0.5f, -0.5f,    // 1 right top 右上
            -0.5f, 0.5f,    // 2 left bottom左下
            0.5f, 0.5f      // 3 right bottom右下
        )
        private val RECTANGLE_TEX_COORDS = floatArrayOf(
            0.0f, 0.0f,     // 0 left top 左上
            1.0f, 0.0f,     // 1 right top 右上
            0.0f, 1.0f,     // 2 left bottom左下
            1.0f, 1.0f      // 3 right bottom右下
        )
        private val RECTANGLE_VERTEX_BUF: FloatBuffer = GLUtil.createFloatBuffer(RECTANGLE_COORDS)
        private val RECTANGLE_TEX_BUF: FloatBuffer = GLUtil.createFloatBuffer(RECTANGLE_TEX_COORDS)


        /**
         * A "full" square, extending from -1 to +1 in both dimensions.  When the model/view/projection
         * matrix is identity, this will exactly cover the viewport.
         *
         *
         * The texture coordinates are Y-inverted relative to RECTANGLE.  (This seems to work out
         * right with external textures from SurfaceTexture.)
         */
        private val FULL_RECTANGLE_COORDS = floatArrayOf(
            -1.0f, -1.0f,  // 0 左上
            1.0f, -1.0f,  // 1 右上
            -1.0f, 1.0f,  // 2 左下
            1.0f, 1.0f      //右下
        )
        private val FULL_RECTANGLE_TEX_COORDS = floatArrayOf(
            0.0f, 0.0f,  // 0 bottom left
            1.0f, 0.0f,  // 1 bottom right
            0.0f, 1.0f,  // 2 top left
            1.0f, 1.0f // 3 top right
        )
        private val FULL_RECTANGLE_VERTEX_BUF: FloatBuffer =
                GLUtil.createFloatBuffer(FULL_RECTANGLE_COORDS)
        private val FULL_RECTANGLE_TEX_BUF: FloatBuffer =
                GLUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS)
    }

    private lateinit var mVertexArray: FloatBuffer

    private lateinit var mTexCoordArray: FloatBuffer

    //坐标轴数.如2个(x,y) 3个(x,y,z)
    private var mCoordsPerVertex = 0
    //坐标个数.坐标数组总数/坐标轴数
    private var mVertexCount = 0

    private var mVertexStride = 0
    private var mTexCoordStride = 0
    private var mPrefab: Prefab = Prefab.RECTANGLE

    enum class Prefab {
        TRIANGLE, RECTANGLE, FULL_RECTANGLE
    }

    constructor(shape: Prefab) {
        when(shape) {
            Prefab.TRIANGLE -> {
                mVertexArray    = TRIANGLE_VERTEX_BUF
                mTexCoordArray  = TRIANGLE_TEX_BUF
                mCoordsPerVertex= 2
                mVertexStride   = mCoordsPerVertex * SIZEOF_FLOAT
                mVertexCount    = TRIANGLE_COORDS.size / mCoordsPerVertex
            }
            Prefab.RECTANGLE -> {
                mVertexArray    = RECTANGLE_VERTEX_BUF
                mTexCoordArray  = RECTANGLE_TEX_BUF
                mCoordsPerVertex= 2
                mVertexStride   = mCoordsPerVertex * SIZEOF_FLOAT
                mVertexCount    = RECTANGLE_COORDS.size / mCoordsPerVertex
            }
            Prefab.FULL_RECTANGLE -> {
                mVertexArray    = FULL_RECTANGLE_VERTEX_BUF
                mTexCoordArray  = FULL_RECTANGLE_TEX_BUF
                mCoordsPerVertex= 2
                mVertexStride   = mCoordsPerVertex * SIZEOF_FLOAT
                mVertexCount    = FULL_RECTANGLE_COORDS.size / mCoordsPerVertex
            }
            else -> {
                throw RuntimeException("Unknown shape $shape")
            }
        }
        mTexCoordStride = 2 * SIZEOF_FLOAT
        mPrefab = shape
    }

    fun getVertexArray() = mVertexArray

    fun getTexCoordArray() = mTexCoordArray

    fun getVertexCount() = mVertexCount

    fun getVertexStride() =mVertexStride

    fun getTexCoordStride() = mTexCoordStride

    fun getCoordPerVertex() = mCoordsPerVertex

}