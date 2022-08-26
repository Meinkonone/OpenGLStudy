package com.konone.openglstudy.gles.util

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.io.*
import java.nio.charset.StandardCharsets

/**
 *  author : konone
 *  date : 2022/7/17
 */
object Utils {

    fun loadShaderFile(context: Context, assetsPath: String): String {
        val ips: InputStream
        var result: String? = null
        try {
            ips = context.resources.assets.open(assetsPath)
            var count: Int
            val ops = ByteArrayOutputStream()
            while (ips.read().also { count = it } != -1) {
                ops.write(count)
            }
            val buff: ByteArray = ops.toByteArray()
            ops.close()
            ips.close()
            result = String(buff, StandardCharsets.UTF_8).replace("\\r\\n".toRegex(), "\n")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("TTT", "loadShaderFile: failed!")
        }
        return result ?: "null"
    }

    fun readShaderFile(context: Context, fileName: String): String {
        val buffer = StringBuffer()
        try {
            val inReader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
            var item = inReader.readLine()
            while (item != null) {
                buffer.append(item).append("\n")
                item = inReader.readLine()
            }
            inReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buffer.toString()
    }


    /**
     * 加载shaderCode
     * @param type: shadeType between GLES20.GL_VERTEX_SHADER and GLES20.GL_FRAGMENT_SHADER
     * @param shaderCode: shade代码
     */
    fun loadShader(type:Int, shaderCode: String): Int{
        return GLES20.glCreateShader(type).also { shader ->
            //为shader程序关联shaderCode
            GLES20.glShaderSource(shader, shaderCode)
            //编译shader
            GLES20.glCompileShader(shader)
            checkGLError("create shader")
        }
    }

    /**
     * 创建并初始化openGL ES程序
     * @param vertexShader: 顶点着色器
     * @param fragmentShader: 片源着色器
     */
    fun setupProgram(vertexShader: Int, fragmentShader:Int):Int{
        //创建一个空的openGL ES程序
        return GLES20.glCreateProgram().also {
            //program 连接顶点着色程序
            GLES20.glAttachShader(it, vertexShader)
            checkGLError("attach vertex shader")
            //program 连接片源着色程序
            GLES20.glAttachShader(it,fragmentShader)
            checkGLError("attach fragment shader")
            //创建openGL ES程序可执行文件(使得openGL ES程序可执行操作)
            GLES20.glLinkProgram(it)

            val status = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, status, 0)
            check(status[0] == GLES20.GL_TRUE) {
                "link program:" + GLES20.glGetProgramInfoLog(it)
            }
        }
    }

    /**
     * 创建并初始化openGL ES程序
     * @param vsi: 顶点着色器string
     * @param fsi: 片源着色器string
     */
    fun createProgram(vsi: String, fsi: String): Int {
        val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vShader, vsi)
        GLES20.glCompileShader(vShader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) { "顶点着色器创建失败！" }

        val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fShader, fsi)
        GLES20.glCompileShader(fShader)
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) { "片元着色器创建失败" }

        val mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vShader)
        GLES20.glAttachShader(mProgram, fShader)
        GLES20.glLinkProgram(mProgram)
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) { "link program:" + GLES20.glGetProgramInfoLog(mProgram) }

        GLES20.glDeleteShader(vShader)
        GLES20.glDeleteShader(fShader)
        return mProgram
    }

    fun generateBitmapTexture(bitmap: Bitmap): IntArray {
        val textureId = IntArray(1)
        GLES20.glGenTextures(1, textureId, 0)
        checkGLError("创建纹理id")

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        //解绑当前纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        checkGLError("创建绘制bitmap的纹理id")

        return textureId
    }

    fun checkGLError(msg: String) {
        val error: Int = GLES20.glGetError()
        Log.i("TTT", "checkGLError: $msg")
        if (error != GLES20.GL_NO_ERROR) {
            Log.d("TTT", "$msg: EGL error: $error")
            throw IllegalStateException("$msg: EGL error: $error")
        }
    }
}