package com.konone.openglstudy.gles.views

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.konone.openglstudy.gles.renders.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *  author : konone
 *  date : 2022/7/15
 */
class MyGLSurfaceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs), View.OnTouchListener {

    private val mGLRender: MyGLRender

    private var mCurrentRender: BaseRender? = null

    init {
        setEGLContextClientVersion(2)

        mGLRender = MyGLRender()
        setRenderer(mGLRender)

        renderMode = RENDERMODE_WHEN_DIRTY
        setOnTouchListener(this)
    }

    fun setRender(render: BaseRender) {
        mCurrentRender = render
    }

    inner class MyGLRender : GLSurfaceView.Renderer {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            mCurrentRender?.onSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)

            mCurrentRender?.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            mCurrentRender?.onDrawFrame()
        }
    }

    private var mPreviousX: Float = 0f
    private var mPreviousY: Float = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        /*val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val diffX = x - mPreviousX
                val diffY = y - mPreviousY
                if (abs(diffY) > abs(diffX)) {
                    mCurrentRender?.setTranslationY(-diffY * TOUCH_SCALE_FACTOR)
                } else {
                    mCurrentRender?.setTranslationX(diffX * TOUCH_SCALE_FACTOR)
                }
                requestRender()
            }
        }
        mPreviousX = x
        mPreviousY = y*/
        return true
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 1f / 540f

        private const val TAG = "MyGLSurfaceView"
    }

}