package com.konone.openglstudy

import android.Manifest
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.konone.openglstudy.camera.CameraHelper
import com.konone.openglstudy.gles.renders.BaseRender
import com.konone.openglstudy.gles.renders.bitmap.BitmapRender
import com.konone.openglstudy.gles.renders.camera.CameraRender
import com.konone.openglstudy.gles.views.MyGLSurfaceView

class MainActivity : AppCompatActivity() {

    private lateinit var mSurfaceView: MyGLSurfaceView
    private val mCameraHelper by lazy {
        CameraHelper(applicationContext, CAMERA_ID)
    }

    private lateinit var mRender: BaseRender

    private var mSurfaceTexture: SurfaceTexture? = null

    private val mFrameAvailableListener = SurfaceTexture.OnFrameAvailableListener {
        //mSurfaceView.requestRender() //循环绘制
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(Manifest.permission.CAMERA), 1000)

        mSurfaceView = findViewById(R.id.surface_view)

        /*//camera render
        mRender = CameraRender(context = applicationContext)
        mSurfaceView.setRender(mRender)

        (mRender as CameraRender).setRenderListener(object : CameraRender.CameraRenderListener {
            override fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture) {
                mSurfaceTexture = surfaceTexture

                mSurfaceTexture?.apply {
                    setOnFrameAvailableListener(mFrameAvailableListener)
                    mCameraHelper.setSurfaceTexture(this)
                    mCameraHelper.startPreview()
                }
            }
        })*/

        mRender = BitmapRender(context =  applicationContext)
        mSurfaceView.setRender(render = mRender)
    }

    override fun onResume() {
        super.onResume()
        mCameraHelper.openCamera()
    }

    override fun onPause() {
        super.onPause()
        mCameraHelper.closeCamera()
    }

    companion object {
        const val CAMERA_ID = 0
    }
}