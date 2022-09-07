package com.konone.openglstudy.gles.texture

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.konone.openglstudy.gles.ShaderParameter
import com.konone.openglstudy.gles.renders.effect.BasicEffect
import com.konone.openglstudy.gles.renders.effect.GaussianEffect
import com.konone.openglstudy.gles.util.Utils

/**
 *  author : konone
 *  date : 2022/9/5
 */
open class BasicTexture {

    private val mTempIdArray = IntArray(1)
    private var mTextureId = 0

    private var mEffect: BasicEffect? = null

    private var mTextureTargetType = GLES20.GL_TEXTURE_2D

    var mRenderWidth = 0
    var mRenderHeight = 0
    var mTextureWidth: Int = 0
    var mTextureHeight: Int = 0

    fun setEffect(effect: BasicEffect) {
        mEffect = effect
    }

    fun getEffect(): BasicEffect? {
        return mEffect
    }

    fun setTextureMatrix(textureMatrix: FloatArray) {
        mEffect?.setTextureMatrix(textureMatrix)
    }

    fun setTextureTargetType(type: Int) {
        mTextureTargetType = type
        if (mEffect is GaussianEffect) {
            (mEffect as GaussianEffect).setOes(type == GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
        }
    }

    open fun setSize(renderWidth: Int, renderHeight: Int, textureWidth: Int, textureHeight: Int) {
        mEffect?.setSize(renderWidth, renderHeight, textureWidth, textureHeight)
        mRenderWidth = renderWidth
        mRenderHeight = renderHeight
        mTextureWidth = textureWidth
        mTextureHeight = textureHeight
    }

    fun getTexture(): Int {
        if (mTextureId == 0) {
            if (!generateTexture()) {
                Log.e(TAG, "texture can't generate normally! please check override method is correct")
            }
        }
        return mTextureId
    }

    fun draw(textureId: Int = mTextureId) {
        mEffect?.let {
            GLES20.glUseProgram(it.getProgram())
            if (it is GaussianEffect) {//do blur
                val gaussianEffect = mEffect as GaussianEffect
                gaussianEffect.apply {
                    bindFrameBufferTexture(it.getShaderParameters(), target = getTargetType(), textureId = textureId)
                    setVertical(true)
                    drawEffect(true)
                    unBindFrameBuffer()

                    setVertical(false)
                    bindScreenTexture(it.getShaderParameters(), target = getTargetType(), textureId = getTextureId())
                }
            } else {
                bindScreenTexture(it.getShaderParameters(), target = getTargetType(), textureId = textureId)
            }
            it.drawEffect()
        }
    }

    open fun getTextureId(): Int {
        return mTextureId
    }

    open fun getTargetType(): Int {
        return mTextureTargetType
    }

    open fun bindScreenTexture(shaderParameters: Array<ShaderParameter>, target: Int = GLES20.GL_TEXTURE_2D, textureId: Int = mTextureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        Utils.checkGLError("active texture 0")
        GLES20.glBindTexture(target, textureId)
        Utils.checkGLError("bind texture")
        GLES20.glUniform1i(shaderParameters[BasicEffect.INDEX_TEXTURE_SAMPLER].handle, 0)
        Utils.checkGLError("set texture uniform")

        //set alpha param
        GLES20.glUniform1f(shaderParameters[BasicEffect.INDEX_ALPHA].handle, 1f)
        Utils.checkGLError("set alpha")
    }

    open fun bindFrameBufferTexture(shaderParameters: Array<ShaderParameter>, target: Int = GLES20.GL_TEXTURE_2D, textureId: Int = 0) {
    }

    open fun unBindFrameBuffer(target: Int = GLES20.GL_TEXTURE_2D) {
    }

    private fun generateTexture(): Boolean {
        if (mTextureId != 0) {
            return true
        }
        GLES20.glGenTextures(1, mTempIdArray, 0)
        Utils.checkGLError("gen texture")
        mTextureId = mTempIdArray[0]
        return mTextureId != 0
    }

    companion object {
        private const val TAG = "BasicTexture"
    }
}