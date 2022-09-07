package com.konone.openglstudy.gles.renders.effect

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.konone.openglstudy.gles.ShaderParameter
import com.konone.openglstudy.gles.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *  author : konone
 *  date : 2022/9/2
 */
open class BasicEffect(val context: Context) {
    //VBO
    private var mBoxCoordinateId = 0

    private val uMatrix = FloatArray(16)
    private val uTextureMatrix = FloatArray(16)


    private var mInitialized = false
    private var mProgram: Int = 0

    private var mIsOesTarget: Boolean = false

    private val mProgramShaderParameters = arrayOf(
        AttributeShaderParameter(POSITION_ATTRIBUTE),
        UniformShaderParameter(MATRIX_UNIFORM),
        UniformShaderParameter(TEXTURE_MATRIX_UNIFORM),
        UniformShaderParameter(TEXTURE_SAMPLER_UNIFORM),
        UniformShaderParameter(ALPHA_UNIFORM),
        AttributeShaderParameter(POSITION_TEXTURE_ATTRIBUTE)
    )

    private var mRenderWidth = 0
    private var mRenderHeight = 0
    protected var mTextureWidth = 0
    protected var mTextureHeight = 0

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
    private val mFBOTextureCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(
        mFrameBufferFragmentCoordinates.size * 4
    ).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            clear()
            put(mFrameBufferFragmentCoordinates)
        }
    }

    init {
        Matrix.setIdentityM(uMatrix, 0)
        Matrix.setIdentityM(uTextureMatrix, 0)
    }

    private fun initialize(): Boolean {
        if (mInitialized) {
            return true
        }
        val vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        val fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader())
        if (vertexShader != 0 && fragmentShader != 0) {
            mProgram = Utils.setupProgram(vertexShader, fragmentShader)
        }
        mInitialized = mProgram != 0
        if (mInitialized) {
            for(shaderParameter in getShaderParameters()) {//get parameter handle
                shaderParameter.loadHandle(mProgram)
            }
            generateBoxCoordinate()
        }
        return mInitialized
    }

    private fun generateBoxCoordinate() {
        val tempId = IntArray(1)
        GLES20.glGenBuffers(1, tempId, 0)
        Utils.checkGLError("gen vbo buffer")
        mBoxCoordinateId = tempId[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBoxCoordinateId)
        Utils.checkGLError("bind array buffer")
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
            BOX_COORDINATE_BUFFER.capacity() * FLOAT_SIZE,
            BOX_COORDINATE_BUFFER,
            GLES20.GL_STATIC_DRAW //一次设置，多次使用
        )
        Utils.checkGLError("set array buffer")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        Utils.checkGLError("unbind array buffer")
    }

    open fun getVertexShader(): String {
        return Utils.loadShaderFile(context, BASE_VERTEX_SHADER_PATH)
    }

    open fun getFragmentShader(): String {
        return Utils.loadShaderFile(
            context,
            if (mIsOesTarget) BASE_OES_FRAGMENT_SHADER_PATH else BASE_FRAGMENT_SHADER_PATH
        )
    }

    open fun getShaderParameters(): Array<ShaderParameter> {
        return mProgramShaderParameters
    }

    fun setSize(renderWidth: Int, renderHeight: Int, textureWidth: Int, textureHeight: Int) {
        mRenderWidth = renderWidth
        mRenderHeight = renderHeight
        mTextureWidth = textureWidth
        mTextureHeight = textureHeight
    }

    fun setOesState(isOes: Boolean) {
        mIsOesTarget = isOes
    }

    open fun getProgram(): Int {
        if (!mInitialized) {
            initialize()
        }
        return mProgram
    }

    fun drawEffect(useFBO: Boolean = false) {
        if (useFBO) {
            Matrix.setIdentityM(uMatrix, 0)
            setCoordPos(mFBOTextureCoordBuffer, uMatrix)
        } else {
            setPosition()
            setMatrix()
        }
        setEffectParams()
        draw()
    }

    open fun setEffectParams() {
    }

    fun setTextureMatrix(matrix: FloatArray) {
        System.arraycopy(matrix, 0, uTextureMatrix, 0, matrix.size)
    }

    protected fun createBuffer(values: FloatArray): FloatBuffer? {
        val size: Int = values.size * FLOAT_SIZE
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(values, 0, values.size).position(0)
            }
    }

    protected fun rewriteBuffer(values: FloatArray, buffer: FloatBuffer?) {
        buffer?.let {
            it.clear()
            it.put(values, 0, values.size).position(0)
        }
    }

    private fun setPosition() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBoxCoordinateId)
        Utils.checkGLError("bind vbo buffer")
        GLES20.glVertexAttribPointer(
            mProgramShaderParameters[INDEX_POSITION].handle, COORD_PER_SIZE,
            GLES20.GL_FLOAT, false, VERTEX_STRIDE, 0
        )//vbo一开始的8个值即为顶点坐标
        GLES20.glVertexAttribPointer(
            mProgramShaderParameters[INDEX_TEXTURE_POSITION].handle,
            COORD_PER_SIZE,
            GLES20.GL_FLOAT,
            false,
            VERTEX_STRIDE,
            if (mIsOesTarget) POSITION_OES_TEXTURE_OFFSET else POSITION_TEXTURE_OFFSET
        )
        Utils.checkGLError("vertexAttribPointer")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        Utils.checkGLError("unbind vbo buffer")
    }

    private fun setCoordPos(fragmentBuffer: FloatBuffer, matrix: FloatArray) {
        //设置顶点坐标以及matrix
        mVertexBuffer.position(0)
        GLES20.glVertexAttribPointer(mProgramShaderParameters[INDEX_POSITION].handle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        Utils.checkGLError("setVertexPos")
        GLES20.glUniformMatrix4fv(mProgramShaderParameters[INDEX_MATRIX].handle, 1, false, matrix, 0)
        Utils.checkGLError("setVertexMatrix")

        //设置纹理坐标以及matrix
        fragmentBuffer.position(0)
        GLES20.glVertexAttribPointer(mProgramShaderParameters[INDEX_TEXTURE_POSITION].handle, 2, GLES20.GL_FLOAT, false, 0, fragmentBuffer)
        Utils.checkGLError("setCoordPos")
        GLES20.glUniformMatrix4fv(mProgramShaderParameters[INDEX_TEXTURE_MATRIX].handle, 1, false, matrix, 0)
        Utils.checkGLError("setCoordMatrix")
    }

    private fun setMatrix(useFBO: Boolean = false) {//calculate matrix & set to gl here
        Matrix.setIdentityM(uMatrix, 0)
        if (!useFBO) {
            val xRatio = mTextureWidth / mRenderWidth.toFloat()
            val yRatio = mTextureHeight / mRenderHeight.toFloat()
            Matrix.scaleM(uMatrix, 0, xRatio, yRatio, 1f)
        }
        GLES20.glUniformMatrix4fv(mProgramShaderParameters[INDEX_MATRIX].handle, 1, false, uMatrix, 0)
        Utils.checkGLError("set matrix")

        GLES20.glUniformMatrix4fv(mProgramShaderParameters[INDEX_TEXTURE_MATRIX].handle, 1, false, uTextureMatrix, 0)
        Utils.checkGLError("set texture matrix")
    }

    private fun draw() {
        GLES20.glEnableVertexAttribArray(mProgramShaderParameters[INDEX_POSITION].handle)
        GLES20.glEnableVertexAttribArray(mProgramShaderParameters[INDEX_TEXTURE_POSITION].handle)
        Utils.checkGLError("enable vertex attribute")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        Utils.checkGLError("draw array")
        GLES20.glDisableVertexAttribArray(mProgramShaderParameters[INDEX_POSITION].handle)
        GLES20.glDisableVertexAttribArray(mProgramShaderParameters[INDEX_TEXTURE_POSITION].handle)
        Utils.checkGLError("disable vertex attribute")
    }

    class AttributeShaderParameter(name: String) : ShaderParameter(name) {
        override fun loadHandle(program: Int) {
            handle = GLES20.glGetAttribLocation(program, mName)
            Utils.checkGLError("attribute in $mName")
        }
    }

    class UniformShaderParameter(name: String) : ShaderParameter(name) {
        override fun loadHandle(program: Int) {
            handle = GLES20.glGetUniformLocation(program, mName)
            Utils.checkGLError("uniform in $mName")
        }
    }

    companion object {
        private const val BASE_VERTEX_SHADER_PATH = "vertex_shader.glsl"
        private const val BASE_FRAGMENT_SHADER_PATH = "fragment_shader.glsl"
        private const val BASE_OES_FRAGMENT_SHADER_PATH = "oes_fragment_shader.glsl"


        private const val POSITION_ATTRIBUTE = "aPosition"
        private const val POSITION_TEXTURE_ATTRIBUTE = "aTexturePosition"
        private const val MATRIX_UNIFORM = "uMatrix"
        private const val TEXTURE_MATRIX_UNIFORM = "uTextureMatrix"
        private const val TEXTURE_SAMPLER_UNIFORM = "uTextureSampler"
        private const val ALPHA_UNIFORM = "uAlpha"

        //Handle indices
        const val INDEX_POSITION = 0
        const val INDEX_MATRIX = 1
        const val INDEX_TEXTURE_MATRIX = 2
        const val INDEX_TEXTURE_SAMPLER = 3
        const val INDEX_ALPHA = 4
        const val INDEX_TEXTURE_POSITION = 5

        private const val FLOAT_SIZE = java.lang.Float.SIZE / java.lang.Byte.SIZE

        private const val COORD_PER_SIZE = 2

        //vbo stride->两个值组成一个坐标，每个值的长度为FLOAT_SIZE， 所以整体步长为COORD_PER_SIZE * FLOAT_SIZE
        private const val VERTEX_STRIDE: Int = COORD_PER_SIZE * FLOAT_SIZE

        private const val POSITION_TEXTURE_OFFSET = 8 * FLOAT_SIZE

        private const val POSITION_OES_TEXTURE_OFFSET = 16 * FLOAT_SIZE


        private val BOX_COORDINATES = floatArrayOf(
            //顶点坐标
            -1f, 1f,        //left top
            -1f, -1f,       //left bottom
            1f, 1f,         //right top
            1f, -1f,        //right bottom
            //bitmap 纹理坐标
            0f, 0f,         //left top
            0f, 1f,         //left bottom
            1f, 0f,         //right top
            1f, 1f,         //right bottom
            //oes 纹理坐标
            0f, 1f,         //left top
            0f, 0f,         //left bottom
            1f, 1f,         //right top
            1f, 0f,         //right bottom
        )

        private val BOX_COORDINATE_BUFFER: FloatBuffer = ByteBuffer.allocateDirect(
            BOX_COORDINATES.size * FLOAT_SIZE).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                clear()
                put(BOX_COORDINATES)
                position(0)
            }
        }
    }
}