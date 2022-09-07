package com.konone.openglstudy.gles.renderer

import com.konone.openglstudy.gles.texture.BasicTexture

/**
 *  author : konone
 *  date : 2022/9/6
 */
open class BasicRenderer {

    var mTargetTexture: BasicTexture? = null




    fun setTargetTexture(texture: BasicTexture) {
        mTargetTexture = texture
    }

    open fun onSurfaceCreated() {
    }

    open fun onSurfaceChanged(width: Int, height: Int) {
    }

    open fun onDrawFrame() {
        mTargetTexture?.draw()
    }
}