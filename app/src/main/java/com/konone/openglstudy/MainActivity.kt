package com.konone.openglstudy

import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.konone.openglstudy.gles.renderer.BasicRenderer
import com.konone.openglstudy.gles.renderer.BitmapBlurRender
import com.konone.openglstudy.gles.renders.effect.GaussianEffect
import com.konone.openglstudy.gles.texture.FBOTexture
import com.konone.openglstudy.gles.views.CustomSurfaceView
import com.konone.openglstudy.util.WindowUtils

class MainActivity : AppCompatActivity() {

    private lateinit var mSurfaceView: CustomSurfaceView

    private lateinit var mTargetRenderer: BasicRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowUtils.setWindowFullScreen(window)

        mSurfaceView = findViewById(R.id.effect_gl_view)

        mTargetRenderer = BitmapBlurRender()
        (mTargetRenderer as BitmapBlurRender).setRenderBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.beauty
            )
        )
        val texture = FBOTexture()
        val effect = GaussianEffect(applicationContext)
        texture.setEffect(effect)
        mTargetRenderer.setTargetTexture(texture)

        mSurfaceView.setTargetRender(mTargetRenderer)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    companion object {
        const val CAMERA_ID = 0
    }
}