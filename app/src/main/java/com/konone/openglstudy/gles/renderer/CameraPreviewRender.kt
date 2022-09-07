package com.konone.openglstudy.gles.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.Matrix
import com.konone.openglstudy.gles.renders.effect.BasicEffect
import com.konone.openglstudy.gles.renders.effect.GaussianEffect
import com.konone.openglstudy.gles.texture.FBOTexture

/**
 *  author : konone
 *  date : 2022/9/6
 */
class CameraPreviewRender(private val context: Context) : BasicRenderer() {
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mListener: CameraRenderListener? = null

    private val mTextureMatrix = FloatArray(16)

    private var mDoBlur = false

    private val mPreviewTexture = FBOTexture()

    private val mGaussianTexture = FBOTexture()

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    interface CameraRenderListener {
        fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture?)
    }

    fun setRenderListener(listener: CameraRenderListener?) {
        mListener = listener
    }

    fun doBlur(blur: Boolean) {
        mDoBlur = blur
        mTargetTexture?.let {
            if (mDoBlur) {
                val previewEffect = it.getEffect()
                mPreviewTexture.setEffect(previewEffect!!)

                val effect = BasicEffect(context)
                mGaussianTexture.setEffect(effect)
            }
        }
    }

    init {
        Matrix.setIdentityM(mTextureMatrix, 0)
    }

    override fun onSurfaceCreated() {
        val textureId = mTargetTexture?.getTexture() ?: 0
        if (textureId != 0) {
            mSurfaceTexture = SurfaceTexture(textureId)
            mListener?.onSurfaceTextureReady(mSurfaceTexture)
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        mTargetTexture?.setSize(width, height, width, height)
        mWidth = width
        mHeight = height
    }

    override fun onDrawFrame() {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        mSurfaceTexture?.let {
            it.updateTexImage()
            it.getTransformMatrix(mTextureMatrix)
        }

        if (mDoBlur) {
            mPreviewTexture.setSize(mWidth, mHeight, mWidth, mHeight)
            mGaussianTexture.setSize(mWidth, mHeight, mWidth, mHeight)
            GLES20.glUseProgram(mTargetTexture!!.getEffect()!!.getProgram())
            mPreviewTexture.let {
                //1. draw preview to fbo textureId
                it.bindFrameBufferTexture(it.getEffect()!!.getShaderParameters(), mTargetTexture!!.getTargetType(), mTargetTexture!!.getTextureId())
                it.setTextureMatrix(mTextureMatrix)
                it.getEffect()!!.drawEffect(true)
                val fboTextureId = it.getTextureId()
                //2. draw by fbo texture
                mGaussianTexture.draw(textureId = fboTextureId)
            }
        } else {
            mTargetTexture?.let {
                it.setTextureMatrix(mTextureMatrix)
                it.draw()
            }
        }
    }
}