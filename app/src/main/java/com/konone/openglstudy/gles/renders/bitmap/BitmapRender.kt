package com.konone.openglstudy.gles.renders.bitmap

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.konone.openglstudy.R
import com.konone.openglstudy.gles.renders.BaseRender
import com.konone.openglstudy.gles.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *  author : konone
 *  date : 2022/8/26
 */
class BitmapRender(context: Context) : BaseRender(context) {

    private var vPositionHandle = 0
    private var vPosMatrixHandle = 0
    private var vMatrix = FloatArray(16)

    private var vCoordHandle = 0
    private var vCoordMatrixHandle = 0
    private var vCoordMatrix = FloatArray(16)

    private var vTextureHandle = 0

    private var isVerticalHandle = 0

    private var mTextureId = IntArray(1)


    private var vFrameBufferMatrix = FloatArray(16)
    //FBO++++++++++++++++
    private var mFrameBuffers = IntArray(2)
    private var mFrameBufferTextures = IntArray(2)
    //FBO----------------


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


    //纹理坐标值，和顶点坐标一一对应， 屏幕左上角为坐标原点
    private var mFragmentCoordinates = floatArrayOf(
        //left top
        0f, 0f,
        //left bottom
        0f, 1f,
        //right top
        1f, 0f,
        //right bottom
        1f, 1f,
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

    //FBO 纹理坐标值，和顶点坐标一一对应， 屏幕左下角为坐标原点
    private var mFrameBufferFragmentCoordinates = floatArrayOf(
        //left top
        0f, 1f,
        //left bottom
        0f, 0f,
        //right top
        1f, 1f,
        //right bottom
        1f, 0f,
    )

    //FBO 纹理坐标buffer，传给openGL图形管道进行参数设置
    private val mFrameBufferFragmentBuffer: FloatBuffer = ByteBuffer.allocateDirect(
        mFragmentCoordinates.size * 4
    ).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            clear()
            put(mFrameBufferFragmentCoordinates)
        }
    }

    private val mBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.cat)

    //添加FBO完成高斯模糊算法(横竖两次)

    init {
        Matrix.setIdentityM(vMatrix, 0)
        Matrix.setIdentityM(vCoordMatrix, 0)
        Matrix.setIdentityM(vFrameBufferMatrix, 0)
    }

    override fun onSurfaceCreate() {
        //create program
        mProgram = Utils.createProgram(Utils.loadShaderFile(context, "bitmap/bitmap_vertex.glsl"),
            Utils.loadShaderFile(context, "bitmap/bitmap_fragment.glsl"))

        //1. 创建纹理id并绑定bitmap
        mTextureId = Utils.generateBitmapTexture(mBitmap)

        //2. 获取openGL的参数句柄
        prepareHandle()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        Matrix.setIdentityM(vMatrix, 0)
        val xRatio = mBitmap.width.toFloat() / width.toFloat()
        val yRatio = mBitmap.height.toFloat() / height.toFloat()
        Log.i(TAG, "onSurfaceChanged: width = $width, height = $height, xRatio is $xRatio, yRatio is $yRatio")
        Matrix.scaleM(vMatrix, 0, xRatio, yRatio, 1f)

        //创建frameBuffer
        prepareFrameBuffer(width, height)
    }

    override fun draw() {
        super.draw()
        GLES20.glUseProgram(mProgram)

        var isVertical = 0

        //insert frameBuffer
        for (i in 0 until 50) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[isVertical])
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[isVertical], 0)
            if (i == 0) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0])
            } else {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[isVertical])
            }
            GLES20.glUniform1i(isVerticalHandle, isVertical)
            setCoordPos(mFrameBufferFragmentBuffer, vFrameBufferMatrix)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4) //一条公共边，2，3两个点
            isVertical = (isVertical + 1) % 2
        }
        //解绑frameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        //2. 设置gl程序的参数
        setCoordPos(mFragmentBuffer, vMatrix)
        //2.3 设置绘制纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0])
        GLES20.glUniform1i(vTextureHandle, 0)

        //2.4 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4) //一条公共边，2，3两个点
        Utils.checkGLError("drawArray")
    }

    private fun prepareHandle() {
        //2. 获取openGL的参数句柄
        vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        vPosMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")

        vCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoord")
        vCoordMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vCoordMatrix")

        vTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture")

        isVerticalHandle = GLES20.glGetUniformLocation(mProgram, "isVertical")
    }


    private fun prepareFrameBuffer(width: Int, height: Int) {
        //1. 创建frameBuffer以及frameBufferTexture
        GLES20.glGenFramebuffers(2, mFrameBuffers, 0)
        GLES20.glGenTextures(2, mFrameBufferTextures, 0)
        //2. 初始化frameBuffer参数
        for (i in 0..1) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[i])
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }
    }

    private fun setCoordPos(fragmentBuffer: FloatBuffer, matrix: FloatArray) {
        //设置顶点坐标以及matrix
        mVertexBuffer.position(0)
        vPositionHandle.also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
            Utils.checkGLError("setVertexPos")
        }
        GLES20.glUniformMatrix4fv(vPosMatrixHandle, 1, false, matrix, 0)
        Utils.checkGLError("setVertexMatrix")

        //设置纹理坐标以及matrix
        fragmentBuffer.position(0)
        vCoordHandle.also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 0, fragmentBuffer)
            Utils.checkGLError("setCoordPos")
        }
        GLES20.glUniformMatrix4fv(vCoordMatrixHandle, 1, false, vCoordMatrix, 0)
        Utils.checkGLError("setCoordMatrix")
    }

    companion object {
        const val TAG = "BitmapRender"
    }

}