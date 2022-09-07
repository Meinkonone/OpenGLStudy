package com.konone.openglstudy.gles.renders

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.konone.openglstudy.gles.programs.BaseRenderProgram
import com.konone.openglstudy.gles.util.Utils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *  author : konone
 *  date : 2022/7/15
 */
open class BaseRender(val context: Context){

    protected var mProgram: Int = -1

    protected var mVertexShader: Int = -1;
    protected var mFragmentShader: Int = -1

    protected var mVertexCount: Int = 0
    protected var mVertexStride: Int = 0

    //1. 利用相机和投影(正交/透视)，得出所需要的变换矩阵
    //2. 通过view的比例计算来得出所需要的变换矩阵
    protected var mMvpMatrix = FloatArray(16)

    init {
        Matrix.setIdentityM(mMvpMatrix, 0)
    }

    private fun initProgram() {
        if (mVertexShader != -1 && mFragmentShader != -1) {
            mProgram = Utils.setupProgram(mVertexShader, mFragmentShader)
        }
    }

    fun setTranslationX(value: Float) {
        Log.i(TAG, "setTranslationX: value = $value")
        Matrix.translateM(mMvpMatrix, 0, value, 0f, 0f)
    }

    fun setTranslationY(value: Float) {
        Log.i(TAG, "setTranslationY: value = $value")
        Matrix.translateM(mMvpMatrix, 0, 0f, value, 0f)
    }

    open fun onSurfaceCreated() {
        initProgram()
    }

    open fun onSurfaceChanged(width: Int, height: Int) {
        Matrix.setIdentityM(mMvpMatrix, 0)
        val ratio = width / height.toFloat()
        Log.i(TAG, "onSurfaceChanged: width = $width, height = $height, ratio is $ratio")
        Matrix.scaleM(mMvpMatrix, 0, 1f, ratio, 1f)
    }

    open fun onDrawFrame() {
        if (mProgram == -1) {
            Log.w(TAG, "draw failed, program not init success")
            return
        }
    }

    companion object {
        //number of coordinate per vertex int this array
        //每个顶点的坐标数量 3个(因为绘制的是三角形，需要三个坐标来确定形状位置)
        var COORDINATES_PER_VERTEX = 3

        private const val TAG = "BaseRender"
    }
}
