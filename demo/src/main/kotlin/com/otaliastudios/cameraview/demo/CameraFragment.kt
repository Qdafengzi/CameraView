package com.otaliastudios.cameraview.demo

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.params.RggbChannelVector
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.CameraOptions
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.demo.databinding.DialogBinding
import com.otaliastudios.cameraview.demo.databinding.FragmentCameraBinding
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.MultiFilter
import com.otaliastudios.cameraview.filter.NoFilter
import com.otaliastudios.cameraview.filters.custom.ImageLevelFilter
import com.otaliastudios.cameraview.filters.custom.WhiteBalanceFilter
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import com.otaliastudios.cameraview.utils.XLogger
import java.io.ByteArrayOutputStream
import java.io.File

class CameraFragment:Fragment(),OptionView.Callback  {

    companion object {
        private val LOG = CameraLogger.create("DemoApp")
        private const val USE_FRAME_PROCESSOR = false
        private const val DECODE_BITMAP = false
    }

    private var captureTime: Long = 0

    private val allFilters = Filters.values()
    private var mLevelFilter: ImageLevelFilter? = null
    private var mBackgroundFilter: ImageLevelFilter? = null
    private var mWhiteBalanceFilter: WhiteBalanceFilter? = null
    private var currentFilter = 0

    
    lateinit var mBinding:FragmentCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CameraLogger.setLogLevel(CameraLogger.LEVEL_INFO)
        mBinding.camera.setLifecycleOwner(this)
        mBinding.camera.addCameraListener(Listener())

        if (USE_FRAME_PROCESSOR) {
            mBinding.camera.addFrameProcessor(object : FrameProcessor {
                private var lastTime = System.currentTimeMillis()
                override fun process(frame: Frame) {
                    val newTime = frame.time
                    val delay = newTime - lastTime
                    lastTime = newTime
                    LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)
                    if (DECODE_BITMAP) {
                        if (frame.format == ImageFormat.NV21
                            && frame.dataClass == ByteArray::class.java
                        ) {
                            val data = frame.getData<ByteArray>()
                            val yuvImage = YuvImage(
                                data,
                                frame.format,
                                frame.size.width,
                                frame.size.height,
                                null
                            )
                            val jpegStream = ByteArrayOutputStream()
                            yuvImage.compressToJpeg(
                                Rect(
                                    0, 0,
                                    frame.size.width,
                                    frame.size.height
                                ), 100, jpegStream
                            )
                            val jpegByteArray = jpegStream.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(
                                jpegByteArray,
                                0, jpegByteArray.size
                            )
                            bitmap.toString()
                        }
                    }
                }
            })
        }

        initDialog()
        initListener()

        initWaterMaker()
        
    }

    private fun initWaterMaker() {
        val animator = ValueAnimator.ofFloat(1f, 0.8f)
        animator.duration = 300
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            mBinding.watermark.scaleX = scale
            mBinding.watermark.scaleY = scale
            mBinding.watermark.rotation = mBinding.watermark.rotation + 2
        }
        animator.start()
    }



    lateinit var options: CameraOptions
    private inner class Listener : CameraListener() {
        override fun onCameraOpened(options: CameraOptions) {
            this@CameraFragment.options = options
            val group = mDialogBinding.child
            for (i in 0 until group.childCount) {
                val view = group.getChildAt(i) as OptionView<*>
                view.onCameraOpened(mBinding.camera, options)
            }

            val config = "wb:${options.isSupportWhiteBalance}\n"
            mBinding.supportWhiteBalance.text = config
        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            message("Got CameraException #" + exception.reason, true)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            if (mBinding.camera.isTakingVideo) {
                message("Captured while taking video. Size=" + result.size, false)
                return
            }

            // This can happen if picture was taken with a gesture.
            val callbackTime = System.currentTimeMillis()
            if (captureTime == 0L) captureTime = callbackTime - 300
            LOG.w("onPictureTaken called! Launching activity. Delay:", callbackTime - captureTime)
            PicturePreviewActivity.pictureResult = result
            val intent = Intent(requireContext(), PicturePreviewActivity::class.java)
            intent.putExtra("delay", callbackTime - captureTime)
            startActivity(intent)
            captureTime = 0
            LOG.w("onPictureTaken called! Launched activity.")
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            LOG.w("onVideoTaken called! Launching activity.")
            VideoPreviewActivity.videoResult = result
            val intent = Intent(requireContext(), VideoPreviewActivity::class.java)
            startActivity(intent)
            LOG.w("onVideoTaken called! Launched activity.")
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            LOG.w("onVideoRecordingStart!")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            message("Video taken. Processing...", false)
            LOG.w("onVideoRecordingEnd!")
        }

        override fun onExposureCorrectionChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers)
            message("Exposure correction:$newValue", false)
        }

        @SuppressLint("SetTextI18n")
        override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
            super.onZoomChanged(newValue, bounds, fingers)
            if (this@CameraFragment::options.isInitialized){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val zoomRatio = (this@CameraFragment.options.zoomRange.upper - this@CameraFragment.options.zoomRange.lower) * newValue + this@CameraFragment.options.zoomRange.lower
                    mBinding.zoom.text = "maxRatio:${this@CameraFragment.options.maxZoomRatio} current:${zoomRatio} percent:${newValue*100}%"
                } else {
                    val zoomRatio = (this@CameraFragment.options.maxZoomRatio - 1f) * newValue + 1f
                    mBinding.zoom.text = "maxRatio:${this@CameraFragment.options.maxZoomRatio} current:${zoomRatio} percent:${newValue*100}%"
                }

                message("Zoom:${this@CameraFragment.options.maxZoomRatio* newValue}", false)
            }
        }

        override fun onWhiteBalance(rggbChannelVector: RggbChannelVector?) {
            super.onWhiteBalance(rggbChannelVector)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                XLogger.d("白平衡数据：${rggbChannelVector?.greenOdd}  ${rggbChannelVector?.greenEven}  ${rggbChannelVector?.blue}   ${rggbChannelVector?.red}")
            }
        }
    }


    var mConfigAlertDialog: AlertDialog? = null
    lateinit var mDialogBinding: DialogBinding

    var tone = 0f
    var background = 0f

    fun revers(upper: Float, lower: Float, self: Float): Float {
        return (2 * upper - 2 * lower - self)
    }

    private fun initDialog() {
        mDialogBinding = DialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        val group = mDialogBinding.child
        val options: List<Option<*>> = listOf(
            // Layout
            Option.Width(), Option.Height(),
            // Engine and preview
            Option.Mode(), Option.Engine(), Option.Preview(),
            // Some controls
            Option.Flash(), Option.WhiteBalance(), Option.Hdr(),
            Option.PictureMetering(), Option.PictureSnapshotMetering(),
            Option.PictureFormat(),
            // Video recording
            Option.PreviewFrameRate(), Option.VideoCodec(), Option.Audio(), Option.AudioCodec(),
            // Gestures
            Option.Pinch(), Option.HorizontalScroll(), Option.VerticalScroll(),
            Option.Tap(), Option.LongTap(),
            // Watermarks
            Option.OverlayInPreview(mBinding.watermark),
            Option.OverlayInPictureSnapshot(mBinding.watermark),
            Option.OverlayInVideoSnapshot(mBinding.watermark),
            // Frame Processing
            Option.FrameProcessingFormat(),
            // Other
            Option.Grid(), Option.GridColor(), Option.UseDeviceOrientation()
        )
        val dividers = listOf(
            // Layout
            false, true,
            // Engine and preview
            false, false, true,
            // Some controls
            false, false, false, false, false, true,
            // Video recording
            false, false, false, true,
            // Gestures
            false, false, false, false, true,
            // Watermarks
            false, false, true,
            // Frame Processing
            true,
            // Other
            false, false, true
        )
        for (i in options.indices) {
            val view = OptionView<Any>(requireContext())
            view.setOption(options[i] as Option<Any>, this)
            view.setHasDivider(dividers[i])
            group.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        builder.setView(mDialogBinding.root)

        builder.setPositiveButton("确定") { dialog, which -> // 当用户点击“确定”按钮时要执行的操作
            dialog.dismiss()
        }

        builder.setNegativeButton("取消") { dialog, which ->
            // 当用户点击“取消”按钮时要执行的操作
            dialog.dismiss()
        }

        mConfigAlertDialog = builder.create()
    }

    private fun showAlertDialog() {
        mConfigAlertDialog?.show()
    }

    private fun initListener() {
        mBinding.capturePicture.setOnClickListener {
            capturePicture()
        }
        mBinding.capturePictureSnapshot.setOnClickListener {
            capturePictureSnapshot()
        }
        mBinding.captureVideo.setOnClickListener {
            captureVideo()
        }
        mBinding.captureVideoSnapshot.setOnClickListener {
            captureVideoSnapshot()
        }
        mBinding.toggleCamera.setOnClickListener {
            toggleCamera()
        }
        mBinding.changeFilter.setOnClickListener {
            changeCurrentFilter()
        }
        mBinding.config.setOnClickListener {
            showAlertDialog()
        }

        mBinding.filterTone.addOnChangeListener { slider, value, fromUser ->
            LOG.i("slider:$value")
            tone = value

            val max = revers(0.3f, -0.3f, background)
//            val max = revers(0.3f, -0.3f, 0.56f)
            mLevelFilter?.setMin(value, 1.0f, max)
            mLevelFilter?.copy()
        }

        mBinding.filterBackground.addOnChangeListener { slider, value, fromUser ->
            LOG.i("slider:$value")
            background = value

            val min = tone
            val max = revers(0.9f, 0.3f, value)
            mBackgroundFilter?.setMin(min, 1.0f, max)
            mBackgroundFilter?.copy()
        }


        mBinding.filterTint.addOnChangeListener { slider, value, fromUser ->
            mWhiteBalanceFilter?.setTint(value - 50)
            mWhiteBalanceFilter?.copy()
        }

        mBinding.ibClose.setOnClickListener {
            mBinding.camera.filter = NoFilter()
        }

        mBinding.whiteBalanceCheckBox.setOnCheckedChangeListener { compoundButton, b ->
            mBinding.camera.toggleWhiteBalance(b)
        }

        mBinding.filterTemperature.addOnChangeListener { slider, value, fromUser ->
            val progress  = value/50
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //调节色温色调
                mBinding.camera.setTemperature(colorTemperature((progress / 2).toInt()))
            }
        }

        mBinding.filterCompensation.addOnChangeListener { slider, value, fromUser ->
            if (this::options.isInitialized) {
                val newValue = getValue(this.options.exposureCorrectionMaxValue, this.options.exposureCorrectionMinValue, 100f, 0f, value)
                mBinding.camera.exposureCorrection = newValue
            }
        }
        mBinding.openCheckBox.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                mBinding.camera.open()
                mBinding.cameraOpen.text = "open"
            }else{
                mBinding.camera.close()
                mBinding.cameraOpen.text = "close"
            }
        }
    }

    fun getValue(max: Float, min: Float, sliderMax: Float, sliderMin: Float, progress: Float): Float {
        val slope = (max - min) / (sliderMax - sliderMin)
        val intercept = min - slope * sliderMin
        return (slope * progress + intercept)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun colorTemperature(factor: Int): RggbChannelVector =
        RggbChannelVector(
            0.635f + (0.0208333f * factor),
            1.0f,
            1.0f,
            3.7420394f + (-0.0287829f * factor)
        )

    private fun capturePicture() {
        if (mBinding.camera.mode == Mode.VIDEO) return run {
            message("Can't take HQ pictures while in VIDEO mode.", false)
        }
        if (mBinding.camera.isTakingPicture) return
        captureTime = System.currentTimeMillis()
        message("Capturing picture...", false)
        mBinding.camera.takePicture()
    }

    private fun capturePictureSnapshot() {
        if (mBinding.camera.isTakingPicture) return
        if (mBinding.camera.preview != Preview.GL_SURFACE) return run {
            message("Picture snapshots are only allowed with the GL_SURFACE preview.", true)
        }
        captureTime = System.currentTimeMillis()
        message("Capturing picture snapshot...", false)
        mBinding.camera.takePictureSnapshot()
    }

    private fun captureVideo() {
        if (mBinding.camera.mode == Mode.PICTURE) return run {
            message("Can't record HQ videos while in PICTURE mode.", false)
        }
        if (mBinding.camera.isTakingPicture || mBinding.camera.isTakingVideo) return
        message("Recording for 5 seconds...", true)
        mBinding.camera.takeVideo(File(requireContext().filesDir, "video.mp4"), 5000)
    }

    private fun captureVideoSnapshot() {
        if (mBinding.camera.isTakingVideo) return run {
            message("Already taking video.", false)
        }
        if (mBinding.camera.preview != Preview.GL_SURFACE) return run {
            message("Video snapshots are only allowed with the GL_SURFACE preview.", true)
        }
        message("Recording snapshot for 5 seconds...", true)
        mBinding.camera.takeVideoSnapshot(File(requireContext().filesDir, "video.mp4"), 5000)
    }

    private fun toggleCamera() {
        if (mBinding.camera.isTakingPicture || mBinding.camera.isTakingVideo) return
        when (mBinding.camera.toggleFacing()) {
            Facing.BACK -> message("Switched to back camera!", false)
            Facing.FRONT -> message("Switched to front camera!", false)
        }
    }


    private fun changeCurrentFilter() {
        if (mBinding.camera.preview != Preview.GL_SURFACE) return run {
            message("Filters are supported only when preview is Preview.GL_SURFACE.", true)
        }
        if (currentFilter < allFilters.size - 1) {
            currentFilter++
        } else {
            currentFilter = 0
        }
//        val filter = allFilters[currentFilter]
        val filterLevel = allFilters[Filters.LEVEL.ordinal]
        val filterBackground = allFilters[Filters.LEVEL.ordinal]

//        message(filter.toString(), false)

        // Normal behavior:
        val level = filterLevel.newInstance()
        if (level is ImageLevelFilter) {
            mLevelFilter = level
        }

        val background = filterBackground.newInstance()
        if (background is ImageLevelFilter) {
            mBackgroundFilter = background
        }
        val whiteBalance = allFilters[Filters.WHITE_BALANCE.ordinal]
        val whiteBalanceFilter = whiteBalance.newInstance()


        if (whiteBalanceFilter is WhiteBalanceFilter) {
            mWhiteBalanceFilter = whiteBalanceFilter
        }

//        camera.filter = instance
        mBinding.camera.filter = MultiFilter(level, background, whiteBalanceFilter)

        // To test MultiFilter:
        // DuotoneFilter duotone = new DuotoneFilter();
        // duotone.setFirstColor(Color.RED);
        // duotone.setSecondColor(Color.GREEN);
        // camera.setFilter(new MultiFilter(duotone, filter.newInstance()));
    }

    override fun <T : Any> onValueChanged(option: Option<T>, value: T, name: String): Boolean {
        if (option is Option.Width || option is Option.Height) {
            val preview = mBinding.camera.preview
            val wrapContent = value as Int == ViewGroup.LayoutParams.WRAP_CONTENT
            if (preview == Preview.SURFACE && !wrapContent) {
                message(
                    "The SurfaceView preview does not support width or height changes. " +
                            "The view will act as WRAP_CONTENT by default.", true
                )
                return false
            }
        }
        option.set(mBinding.camera, value)
//        BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_HIDDEN
        message("Changed " + option.name + " to " + name, false)
        return true
    }

    private fun message(content: String, important: Boolean) {
        if (important) {
            XLogger.e(content)
            //Toast.makeText(this, content, Toast.LENGTH_LONG).show()
        } else {
            XLogger.d(content)
            //Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}