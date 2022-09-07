package com.konone.openglstudy.gles.renders.shape

import android.content.Context
import android.opengl.GLES20
import com.konone.openglstudy.gles.renders.BaseRender
import com.konone.openglstudy.gles.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin


/**
 *  author : konone
 *  date : 2022/7/19
 */
class CircleRender(context: Context) : BaseRender(context) {

    private var mMvpMatrixHandle = -1
    private var mPositionHandle = -1
    private var mColorHandle = -1

    private var mPositionCoordinates: FloatArray
    private var mCircleBuffer: FloatBuffer

    private val mCircleRadius = 0.5f

    private val mColorBuffer = floatArrayOf(0.5f, 1f, 0.5f, 1.0f)

    init {
        mVertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, Utils.loadShaderFile(context, "shape/shape_vertex.glsl"))
        mFragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, Utils.loadShaderFile(context, "shape/shape_fragment.glsl"))

        mPositionCoordinates = createPosition()

        mCircleBuffer = ByteBuffer.allocateDirect(mPositionCoordinates.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(mPositionCoordinates)
                position(0)
            }
        }
        //顶点之间的偏移量
        mVertexStride = 0
    }


    private fun createPosition(): FloatArray{
        val posList = ArrayList<Float>()
        //圆心位置
        posList.add(0f)
        posList.add(0f)
        posList.add(0f)

        val angleSpan = 360f / CIRCLE_DIVIDER_COUNT
        var i = 0f
        while (i < 360 + angleSpan) {
            posList.add((mCircleRadius * sin(i * Math.PI / 180f)).toFloat())
            posList.add((mCircleRadius * cos(i * Math.PI / 180f)).toFloat())
            posList.add(0f)
            i += angleSpan
        }
        return posList.toFloatArray()
    }


    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame() {
        super.onDrawFrame()

        GLES20.glUseProgram(mProgram)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, COORDINATES_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mCircleBuffer)
        }

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, mColorBuffer, 0)
        }

        mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mMvpMatrix, 0)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mPositionCoordinates.size / 3)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        //一个圆划分为360份的三角形(三角形越多，画出来的多边形连接越圆)
        private const val CIRCLE_DIVIDER_COUNT = 360
    }
}