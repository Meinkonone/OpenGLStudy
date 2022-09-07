package com.konone.openglstudy.gles.renders.shape

import android.content.Context
import android.opengl.GLES20
import com.konone.openglstudy.gles.renders.BaseRender
import com.konone.openglstudy.gles.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 *  author : konone
 *  date : 2022/7/17
 */
class SquareRender(context: Context) : BaseRender(context){
    //通过绘制两个共边三角形来完整正方形的绘制

    //逆时针顺序定义坐标
    private val mSquareCoordinates = floatArrayOf(
        //top left
        -0.5f, 0.5f, 0f,
        //bottom left
        -0.5f, -0.5f, 0f,
        //bottom right
        0.5f, -0.5f, 0f,
        //top right
        0.5f, 0.5f, 0f
    )

    //坐标绘制的顺序，0，1，2组成一个三角形，0，2，3组成一个三角形
    private val mDrawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    //坐标buffer
    //capacity calculate : 1 float = 4 byte
    private val mSquareBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(mSquareCoordinates.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(mSquareCoordinates)
                position(0)
            }
        }

    //坐标绘制顺序buffer
    //capacity calculate : 1 short = 2 byte
    private val mSquareDrawOrderBuffer: ShortBuffer = ByteBuffer.allocateDirect(mDrawOrder.size * 2).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply {
            put(mDrawOrder)
            position(0)
        }
    }

    private val mColorBuffer = floatArrayOf(0.5f, 1f, 0.5f, 1.0f)

    private var mMvpMatrixHandle = -1
    private var mPositionHandle = -1
    private var mColorHandle = -1

    init {
        mVertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, Utils.loadShaderFile(context, "shape/shape_vertex.glsl"))
        mFragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, Utils.loadShaderFile(context, "shape/shape_fragment.glsl"))

        mVertexCount = mSquareCoordinates.size / COORDINATES_PER_VERTEX
        mVertexStride = COORDINATES_PER_VERTEX * 4
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame() {
        super.onDrawFrame()

        GLES20.glUseProgram(mProgram)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it, COORDINATES_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mSquareBuffer
            )
        }

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, mColorBuffer, 0)
        }

        mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mMvpMatrix, 0)
        }

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mDrawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            mSquareDrawOrderBuffer
        )

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }
}