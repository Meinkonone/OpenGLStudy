package com.konone.openglstudy.gles.renders.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.konone.openglstudy.gles.renders.BaseRender
import com.konone.openglstudy.gles.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *  author : konone
 *  date : 2022/8/24
 */
class CameraRender(context: Context): BaseRender(context) {
    private val mTextureId: IntArray = IntArray(1)
    private lateinit var mSurfaceTexture: SurfaceTexture


    private var vPositionHandle = 0
    private var vPosMatrixHandle = 0
    private var vMatrix = FloatArray(16)

    private var vCoordHandle = 0
    private var vCoordMatrixHandle = 0
    private var vCoordMatrix = FloatArray(16)

    private var vTextureHandle = 0

    //顶点坐标值，此坐标是按照逆时针顺序定义, 屏幕中心为坐标原点
    private var mVertexCoordinates = floatArrayOf(
        //left top
        -1f, 1f,
        //left bottom
        -1f, -1f,
        //right top
        1f, 1f,
        //right bottom
        1f, -1f,
    )

    //顶点坐标buffer，传给openGL图形管道进行参数设置
    private val mVertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(
        mVertexCoordinates.size * 4
    ).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            clear()
            put(mVertexCoordinates)
        }
    }


    //纹理坐标值，和顶点坐标一一对应， 屏幕左下角为坐标原点
    private var mFragmentCoordinates = floatArrayOf(
        //left top
        0f, 1f,
        //left bottom
        0f, 0f,
        //right top
        1f, 1f,
        //right bottom
        1f, 0f,
    )

    //纹理坐标buffer，传给openGL图形管道进行参数设置
    private val mFragmentBuffer: FloatBuffer = ByteBuffer.allocateDirect(
        mFragmentCoordinates.size * 4
    ).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            clear()
            put(mFragmentCoordinates)
        }
    }


    private var mListener: CameraRenderListener? = null

    interface CameraRenderListener{
        fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture)
    }


    init {
        Matrix.setIdentityM(vMatrix, 0)
        Matrix.setIdentityM(vCoordMatrix, 0)
    }

    fun setRenderListener(listener: CameraRenderListener?) {
        mListener = listener
    }

    override fun onSurfaceCreate() {
        mProgram = Utils.createProgram(Utils.readShaderFile(context, "camera/camera_vertex.glsl"),
            Utils.readShaderFile(context, "camera/camera_fragment.glsl"))

        //1. 创建纹理id
        GLES20.glGenTextures(1, mTextureId, 0)
        //2. 根据纹理id创建surfaceTexture
        mSurfaceTexture = SurfaceTexture(mTextureId[0])
        //3. 将surfaceTexture传给camera，并启动预览
        mListener?.onSurfaceTextureReady(mSurfaceTexture)


        vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        vPosMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")

        vCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoord")
        vCoordMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vCoordMatrix")

        vTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture")
    }

    override fun draw() {
        super.draw()

        //更新纹理数据(消费者消费，否则无法填充新数据)
        mSurfaceTexture.updateTexImage()
        Utils.checkGLError("updateTexImage")
        //获取纹理坐标matrix
        mSurfaceTexture.getTransformMatrix(vCoordMatrix)

        //更新参数到gl程序
        GLES20.glUseProgram(mProgram)

        //1. 设置顶点坐标以及matrix
        mVertexBuffer.position(0)
        vPositionHandle.also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
            Utils.checkGLError("setVertexPos")
        }
        GLES20.glUniformMatrix4fv(vPosMatrixHandle, 1, false, vMatrix, 0)
        Utils.checkGLError("setVertexMatrix")

        //2. 设置纹理坐标以及matrix
        mFragmentBuffer.position(0)
        vCoordHandle.also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 0, mFragmentBuffer)
            Utils.checkGLError("setCoordPos")
        }
        GLES20.glUniformMatrix4fv(vCoordMatrixHandle, 1, false, vCoordMatrix, 0)
        Utils.checkGLError("setCoordMatrix")

        //3. 设置纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0) //激活gl程序的第1个纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId[0]) //为gl程序绑定纹理id
        GLES20.glUniform1i(vTextureHandle, 0) //将纹理设置给gl程序
        Utils.checkGLError("setTexture")

        //4. 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4) //一条公共边，2，3两个点
        Utils.checkGLError("drawArray")

        //5. 停用顶点坐标以完成绘制
        GLES20.glDisableVertexAttribArray(vPosMatrixHandle)
        GLES20.glDisableVertexAttribArray(vCoordHandle)
        Utils.checkGLError("disableVertexPos")
    }
}