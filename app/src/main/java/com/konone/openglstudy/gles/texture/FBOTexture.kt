package com.konone.openglstudy.gles.texture

import android.opengl.GLES20
import android.util.Log
import com.konone.openglstudy.gles.ShaderParameter
import com.konone.openglstudy.gles.util.Utils

/**
 *  author : konone
 *  date : 2022/9/5
 */
class FBOTexture : BasicTexture() {
    private val mFrameBufferTextures = IntArray(1)
    private val mFrameBuffers = IntArray(1)

    override fun setSize(renderWidth: Int, renderHeight: Int, textureWidth: Int, textureHeight: Int) {
        super.setSize(renderWidth, renderHeight, textureWidth, textureHeight)
        prepareFrameBuffer()
    }


    private fun prepareFrameBuffer() {
        GLES20.glGenFramebuffers(1, mFrameBuffers, 0)
        Utils.checkGLError("gen frame buffer")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0])

        GLES20.glGenTextures(1, mFrameBufferTextures, 0)
        Utils.checkGLError("gen frame buffer texture")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0])
        Utils.checkGLError("bind texture")
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        Utils.checkGLError("set texture param")
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0)
        Log.i(TAG, "prepareFrameBuffer size is $mTextureWidth x $mTextureHeight")
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            mRenderWidth,
            mRenderHeight,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        Utils.checkFramebufferStatus("create frame buffer memory")

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        Utils.checkGLError("unbind texture")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        Utils.checkGLError("unbind frame buffer")
    }

    override fun getTextureId(): Int {
        return mFrameBufferTextures[0]
    }

    override fun bindFrameBufferTexture(
        shaderParameters: Array<ShaderParameter>,
        target: Int,
        textureId: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0])
        Utils.checkFramebufferStatus("attach texture to frame buffer")
        GLES20.glBindTexture(target, textureId)
        Utils.checkGLError("bind texture for draw")
    }

    override fun unBindFrameBuffer(target: Int) {
        GLES20.glBindTexture(target, 0)
        Utils.checkGLError("unbind texture")
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        Utils.checkGLError("unbind frame buffer")
    }

    override fun getTargetType(): Int {
        return GLES20.GL_TEXTURE_2D
    }

    companion object {
        private const val TAG = "FboTexture"
    }
}