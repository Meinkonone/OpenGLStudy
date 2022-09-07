package com.konone.openglstudy.gles

/**
 *  author : konone
 *  date : 2022/9/3
 */
abstract class ShaderParameter(name: String) {
    var handle: Int = 0
    protected var mName: String = name

    abstract fun loadHandle(program: Int)

    override fun toString(): String {
        return "$mName for handle $handle"
    }
}