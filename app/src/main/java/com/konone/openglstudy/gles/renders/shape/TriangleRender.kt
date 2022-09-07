package com.konone.openglstudy.gles.renders.shape

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.konone.openglstudy.gles.renders.BaseRender
import com.konone.openglstudy.gles.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *  author : konone
 *  date : 2022/7/15
 */
class TriangleRender(context: Context) : BaseRender(context = context) {

    //坐标值，此坐标是按照逆时针顺序定义
    private var mTriangleCoordinates = floatArrayOf(
        //top
        0f, 0.5f, 0f, //(x,y,z)
        //bottom left
        -0.5f, -0.5f, 0f,
        //bottom right
        0.5f, -0.5f, 0f
    ) //等边三角形

    /*private var mTriangleCoordinates = floatArrayOf(
        //top
        -0.5f, 0.5f, 0f, //(x,y,z)
        //bottom left
        -0.5f, -0.5f, 0f,
        //bottom right
        0.5f, -0.5f, 0f
    ) //等腰直角三角形*/


    //坐标buffer，传给openGL图形管道进行参数设置
    private val mVertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(
        mTriangleCoordinates.size * 4
    ).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(mTriangleCoordinates)
                .position(0)
        }
    }

    private val mColorBuffer = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    //shader中的参数具柄
    private var mMvpMatrixHandle: Int = -1
    private var mPositionHandle:Int = -1
    private var mColorHandle:Int = -1

    init {
        mVertexShader = Utils.loadShader(
            GLES20.GL_VERTEX_SHADER,
            Utils.loadShaderFile(context, "shape/shape_vertex.glsl")
        )
        mFragmentShader = Utils.loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            Utils.loadShaderFile(context, "shape/shape_fragment.glsl")
        )

        mVertexCount = mTriangleCoordinates.size / COORDINATES_PER_VERTEX
        mVertexStride = COORDINATES_PER_VERTEX * 4 //4 byte per vertex

        Matrix.setIdentityM(mMvpMatrix, 0)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
    }


    override fun onDrawFrame() {
        super.onDrawFrame()

        GLES20.glUseProgram(mProgram)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            //enable this handle to the vertices
            GLES20.glEnableVertexAttribArray(it)

            //设置顶点坐标参数
            GLES20.glVertexAttribPointer(
                it, COORDINATES_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                mVertexStride, mVertexBuffer
            )
        }

        mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mMvpMatrix, 0)
        }

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, mColorBuffer, 0)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexCount)

        //disable vertex handle for finish draw vertex
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    /**
     * 绘制需要
     * 1. 顶点着色程序(Vertex Shader)->用于渲染形状的顶点的openGL ES图形代码
     * 2. 片源着色程序(Fragment Shader)->用于使用颜色或者纹理渲染形状面的openGL ES图形代码
     * 3. GL ES程序对象(Program)-> 绘制一个或者多个图形的openGL ES程序对象
     */


    companion object {
        private const val TAG = "TriangleRender"

    }


}