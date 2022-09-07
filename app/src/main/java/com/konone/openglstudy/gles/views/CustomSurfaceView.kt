package com.konone.openglstudy.gles.views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.konone.openglstudy.gles.renderer.BasicRenderer
import com.konone.openglstudy.gles.texture.BasicTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *  author : konone
 *  date : 2022/9/5
 */
class CustomSurfaceView @JvmOverloads constructor(
    context: Context,
    attribute: AttributeSet? = null
) : GLSurfaceView(context, attribute) {

    private val mRender = CustomRender()

    private var mTargetRender: BasicRenderer? = null


    init {
        setEGLContextClientVersion(2)
        setRenderer(mRender)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setTargetRender(renderer: BasicRenderer) {
        mTargetRender = renderer
    }

    inner class CustomRender : Renderer {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            mTargetRender?.onSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            mTargetRender?.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            mTargetRender?.onDrawFrame()
        }
    }

}