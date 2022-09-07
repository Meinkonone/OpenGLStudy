package com.konone.openglstudy.gles.renders.effect

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.konone.openglstudy.gles.ShaderParameter
import com.konone.openglstudy.gles.util.Utils
import java.nio.FloatBuffer
import kotlin.math.ceil
import kotlin.math.exp

/**
 *  author : konone
 *  date : 2022/9/2
 */
class GaussianEffect(context: Context) : BasicEffect(context) {

    private var uKernelSize = 0
    private var mKernelOffset = FloatArray(220)
    private var mKernelValue = FloatArray(220)

    private var uKernelValueBuffer: FloatBuffer? = null
    private var uKernelOffsetH: FloatBuffer? = null
    private var uKernelOffsetV: FloatBuffer? = null

    private var mVertical = false

    private var mOesBlur = false

    private val mGaussianShaderParameters = arrayOf(
        UniformShaderParameter(KERNEL_SIZE),
        UniformShaderParameter(KERNEL_OFFSET),
        UniformShaderParameter(KERNEL_VALUE)
    )

    override fun getFragmentShader(): String {
        return Utils.loadShaderFile(
            context,
            if (mOesBlur) OES_GAUSSIAN_BLUR_FRAGMENT_SHADER else GAUSSIAN_BLUR_FRAGMENT_SHADER
        )
    }

    override fun getShaderParameters(): Array<ShaderParameter> {
        return super.getShaderParameters() + mGaussianShaderParameters
    }

    override fun setEffectParams() {
        createKernelValue(KERNEL_BLUR_STEP, mTextureWidth, mTextureHeight)
        /*Log.i("TTT", "setEffectParams: uKernelSize = $uKernelSize, uKernelSize handle is ${mGaussianShaderParameters[INDEX_SIZE].handle}")
        GLES20.glUniform1i(mGaussianShaderParameters[INDEX_SIZE].handle, uKernelSize)
        Utils.checkGLError("set kernel size")*/
        Log.i("TTT", "setEffectParams: mKernelOffset = $mKernelOffset, uKernelOffsetH handle is ${mGaussianShaderParameters[INDEX_OFFSET].handle}")
        GLES20.glUniform2fv(
            mGaussianShaderParameters[INDEX_OFFSET].handle,
            mKernelOffset.size,
            if (mVertical) uKernelOffsetV else uKernelOffsetH
        )
        Utils.checkGLError("set kernel offset")
        Log.i("TTT", "setEffectParams: mKernelValue = $mKernelValue, mKernelValue handle is ${mGaussianShaderParameters[INDEX_VALUE].handle}")
        GLES20.glUniform1fv(
            mGaussianShaderParameters[INDEX_VALUE].handle,
            mKernelValue.size,
            uKernelValueBuffer
        )
        Utils.checkGLError("set kernel value")
    }

    fun setVertical(isVertical: Boolean) {
        mVertical = isVertical
    }

    fun setOes(isOes: Boolean) {
        mOesBlur = isOes
    }

    /****************************************************************************************
     * Gauss algorithm: the weighted average is more reasonable, the closer the distance is,
     * the greater the weight of the point is, and the smaller the distance is.
     * G(x, y) = 1/(2Πσ²) * Math.exp(-(x²+y²)/2σ²)
     */
    private fun createKernelValue(σ: Float, width: Int, height: Int) {
        val scale = (-1 / (2 * σ * σ)).toDouble()
        val cons = 1 / (2 * Math.PI * σ * σ)
        uKernelSize = (ceil((σ * 3).toDouble()) * 2 + 1).toInt()
        val center: Int = uKernelSize / 2
        var sum = 0.0f
        for (i in 0 until uKernelSize) {
            val x = i - center
            mKernelValue[i] = (cons * exp(scale * x * x)).toFloat()
            sum += mKernelValue[i]
        }

        // The sum of the weighted averages is 1,
        // so each item needs to be divided by sum.
        for (i in 0 until uKernelSize) {
            mKernelValue[i] /= sum
        }
        if (uKernelValueBuffer == null) {
            uKernelValueBuffer = createBuffer(mKernelValue)
        } else {
            rewriteBuffer(mKernelValue, uKernelValueBuffer)
        }
        var offset = 0
        // horizontal Gaussian blur. GLES20.GL_CLAMP_TO_EDGE setting,
        // so that texture coordinates will not exceed the horizontal boundary.
        for (i in -center..center) {
            mKernelOffset[offset] = i.toFloat() / width
            mKernelOffset[offset + 1] = 0.0.toFloat()
            offset += 2
        }
        if (uKernelOffsetH == null) {
            uKernelOffsetH = createBuffer(mKernelOffset)
        } else {
            rewriteBuffer(mKernelOffset, uKernelOffsetH)
        }
        offset = 0
        // vertical Gaussian blur. GLES20.GL_CLAMP_TO_EDGE setting,
        // so that texture coordinates will not exceed the vertical boundary.
        for (i in -center..center) {
            mKernelOffset[offset] = 0.0.toFloat()
            mKernelOffset[offset + 1] = i.toFloat() / height
            offset += 2
        }
        if (uKernelOffsetV == null) {
            uKernelOffsetV = createBuffer(mKernelOffset)
        } else {
            rewriteBuffer(mKernelOffset, uKernelOffsetV)
        }
    }

    companion object {
        private const val GAUSSIAN_BLUR_FRAGMENT_SHADER = "effect/gaussian_blur_fragment.glsl"
        private const val OES_GAUSSIAN_BLUR_FRAGMENT_SHADER =
            "effect/oes_gaussian_blur_fragment.glsl"


        private const val KERNEL_SIZE = "uKernelSize"
        private const val KERNEL_OFFSET = "uKernelOffset"
        private const val KERNEL_VALUE = "uKernelValue"

        private const val INDEX_SIZE = 0
        private const val INDEX_OFFSET = 1
        private const val INDEX_VALUE = 2

        private const val KERNEL_BLUR_STEP = 15f
    }

}