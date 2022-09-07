package com.konone.openglstudy.gles.renderer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.konone.openglstudy.gles.util.Utils

/**
 *  author : konone
 *  date : 2022/9/6
 */
class BitmapBlurRender : BasicRenderer() {
    private var mBitmap: Bitmap? = null

    fun setRenderBitmap(bitmap: Bitmap) {
        mBitmap = bitmap
    }

    override fun onSurfaceCreated() {
        val textureId = mTargetTexture?.getTexture() ?: 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
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
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
        //解绑当前纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        Utils.checkGLError("创建绘制bitmap的纹理id")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        mBitmap?.let {
            mTargetTexture?.setSize(width, height, it.width, it.height)
        }
    }

}