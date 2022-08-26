package com.konone.openglstudy.camera

import android.Manifest
import android.content.Context
import android.os.HandlerThread
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CameraCharacteristics
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.media.ImageReader.OnImageAvailableListener
import android.graphics.ImageFormat
import com.konone.openglstudy.camera.CameraHelper
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.Surface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

/**
 * @author lulingpao
 */
class CameraHelper(private val mContext: Context, val cameraId: Int) {
    private var mCameraThread: HandlerThread? = null
    private var mCameraHandler: Handler? = null
    private val mCameraManager: CameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mCharacteristics: CameraCharacteristics? = null
    var previewSize: Size? = null
        private set
    private var mSurface: Surface? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private val mCameraStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                mCameraDevice = camera
                try {
                    mCharacteristics = mCameraManager.getCameraCharacteristics(cameraId.toString())
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
                initPreviewSize()
                startPreview()
            }

            override fun onDisconnected(camera: CameraDevice) {}
            override fun onError(camera: CameraDevice, error: Int) {}
        }
    private var mPreviewImageReader: ImageReader? = null
    private val mImageAvailableListener = OnImageAvailableListener { reader ->
        val image = reader.acquireNextImage() ?: return@OnImageAvailableListener
        image.close()
        /*int height = image.getHeight();
            int width = image.getWidth();
            ByteBuffer yBuf = image.getPlanes()[0].getBuffer();
            ByteBuffer vuBuf = image.getPlanes()[2].getBuffer();
            int stride = image.getPlanes()[0].getRowStride();
            Log.i("TTT", "onImageAvailable: width = " + width + ", stride = " + stride + ", height = " + height);
            ByteBuffer yuvData = ByteBuffer.allocate(stride * height * 3 / 2);
            yBuf.get(yuvData.array(), 0, yBuf.remaining());
            vuBuf.get(yuvData.array(), stride * height, vuBuf.remaining());
            byte[] previewBuffer = yuvData.array();
            yuvData.rewind();
            image.close();
            RectF rectF = new RectF((width - height) / 2f / width, 0, 1 - (width - height) / 2f / width, 1);
            YuvImage yuvImage = new YuvImage(previewBuffer, ImageFormat.NV21, height, height, new int[]{height, height, height});
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            yuvImage.compressToJpeg(new Rect(0,0, height, height), 80, bos);
            writeFile(mContext.getFilesDir() + File.separator + System.currentTimeMillis() + ".jpg", bos.toByteArray());*/
    }

    private fun init() {
        mCameraThread = HandlerThread("camera handler thread")
        mCameraThread!!.start()
        mCameraHandler = Handler(mCameraThread!!.looper)
    }

    private fun initPreviewSize() {
        /*StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size[] sizes = map.getOutputSizes(SurfaceHolder.class);
            mPreviewSize = sizes[0];
        }*/
        previewSize = Size(2400, 1080)
        mPreviewImageReader = ImageReader.newInstance(2400, 1080, ImageFormat.YUV_420_888, 2)
        mPreviewImageReader!!.setOnImageAvailableListener(mImageAvailableListener, mCameraHandler)
        Log.i(TAG, "initPreviewSize, mPreviewSize = $previewSize")
    }

    fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mCameraManager.openCamera(cameraId.toString(), mCameraStateCallback, mCameraHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun setSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        mSurfaceTexture = surfaceTexture
    }

    fun startPreview() {
        try {
            if (mSurfaceTexture == null) {
                Log.i(TAG, "startPreview return cause surfaceTexture not ready")
                return
            }
            if (mCameraDevice == null) {
                Log.i(TAG, "startPreview return cause camera not ready")
                return
            }
            Log.i(TAG, "startPreview")
            mSurfaceTexture!!.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
            mSurface = Surface(mSurfaceTexture)
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewBuilder!!.addTarget(mSurface!!)
            //mPreviewBuilder.addTarget(mPreviewImageReader.getSurface());
            val surfaces = ArrayList<Surface>()
            surfaces.add(mSurface!!)
            //surfaces.add(mPreviewImageReader.getSurface());
            Log.i("TTT", "startPreview: start create session")
            mCameraDevice!!.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.i("TTT", "onConfigured: finish")
                        mCaptureSession = session
                        mPreviewBuilder!!.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )
                        try {
                            mCaptureSession!!.setRepeatingRequest(
                                mPreviewBuilder!!.build(),
                                null,
                                mCameraHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mCameraHandler
            )
            Log.i("TTT", "startPreview: end create session")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun stopPreview() {}
    fun writeFile(path: String?, data: ByteArray?) {
        var out: FileOutputStream? = null
        try {
            val file = File(path)
            if (!file.exists()) {
                file.createNewFile()
            }
            out = FileOutputStream(path)
            out.write(data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write data", e)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun closeCamera() {
        mSurfaceTexture = null
        mSurface?.release()
        mCaptureSession?.close()
        mCaptureSession = null
        mCameraDevice?.close()
        mCameraDevice = null
    }

    companion object {
        private const val TAG = "CameraHelper"
        const val WIDTH = 640
        const val HEIGHT = 480
    }

    init {
        init()
    }
}