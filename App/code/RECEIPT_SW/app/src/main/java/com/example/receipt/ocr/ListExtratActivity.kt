package com.example.receipt.ocr

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.receipt.MainActivity
import com.example.receipt.databinding.ActivityListextractBinding
import com.example.receipt.possetionList.MyListDb
import com.example.receipt.recycle.ListExtractViewAdapter
import com.example.receipt.recycle.ListExtractView
import com.googlecode.tesseract.android.TessBaseAPI
import org.json.JSONArray
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_listextract.*
import java.text.SimpleDateFormat
import android.net.Uri as Uri

open class ListExtratActivity : PermissionClass() {

    private val PERM_STORAGE = 9
    private val PERM_CAMERA = 10
    private val REQ_CAMERA = 11
    private val REQ_GALLERY = 12

    private val TAG = "AppDebug"
    private val ocr = OcrClass()

    lateinit var tess: TessBaseAPI
    var dataPath: String = ""

    val binding by lazy { ActivityListextractBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ocrOk.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        requirePermissions(
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERM_STORAGE
        )

        dataPath = filesDir.toString() + "/tesseract/"
        ocr.checkFile(dataPath, "kor", getAssets())

        val lang = "kor"

        tess = TessBaseAPI()
        tess.init(dataPath, lang)
    }

    fun initViews() {
        //2.카메라 요청시 권한을 먼저 체크하고 승인되었으면 카메라를 연다.
        Log.d("test", "initview")
        binding.OcrCamera.setOnClickListener {
            requirePermissions(arrayOf(android.Manifest.permission.CAMERA), PERM_CAMERA)
        }
        //5. 갤러리 버튼이 클리되면 갤러리를 연다.
        binding.OcrGerraly.setOnClickListener {
            openGallery()
        }
    }

    //원본 이미지의 주소를 저장할 변수
    private var realUri: Uri? = null

    // 3.카메라에 찍은 사진을 저장하기 위한 uri넘겨준다.
    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageUri(newFileName(), "image/jpg")?.let { uri ->
            realUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            startActivityForResult(intent, REQ_CAMERA)
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQ_GALLERY)

    }

    //원본 이미지를 저장할 Uri를 MediaStore(데이터베이스)에 생성하는 메서드
    fun createImageUri(filename: String, mimeType: String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    // 파일 이름을 생성하는 메서드
    fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    //원본 이미지를 불러오는 메서드
    fun loadBitmap(photoUri: Uri): Bitmap? {
        var image: Bitmap? = null
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                image = ImageDecoder.decodeBitmap(source)
            } else {
                image = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return image
    }

    override fun permissionGranted(requestCode: Int) {
        when (requestCode) {
            PERM_STORAGE -> initViews()
            PERM_CAMERA -> openCamera()
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when (requestCode) {
            PERM_STORAGE -> {
                Toast.makeText(this, "공용 저장소 권한을 승인해야 앱을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            PERM_CAMERA -> Toast.makeText(this, "카메라 권한을 승인해야 카메라를 사용할 수 있습니다.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //4.카메라를 찍은 후에 호출된다. 6.갤러리에서 선택 후 추가된다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQ_CAMERA -> {
                    realUri?.let { uri->
                        launchImageCrop(uri)
                    }
                }
                REQ_GALLERY -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode== Activity.RESULT_OK) {
                        result.uri?.let{uri ->
                            val bitmap = loadBitmap(uri)
                            binding.imagePreViewOCR.setImageURI(uri)

                            if (bitmap != null) {
                                imageExtract(bitmap.copy(Bitmap.Config.ARGB_8888,true)) // ocr로 추출한 키워드 표시
                            }
                        }
                        binding.imagePreViewOCR.setImageURI(result.uri)
                    }
                    else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                        Log.e(TAG, "Crop error: ${result.error}")
                    }
                }
            }
        }
    }

    private fun launchImageCrop(uri: Uri): Bitmap? {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
        return loadBitmap(uri)

    }

    /**
     *  OCR 구현
     */


    fun imageExtract(bitmap: Bitmap) {
        Toast.makeText(applicationContext, "잠시 기다려 주세요", Toast.LENGTH_SHORT).show()
        tess.setImage(bitmap)
        val ocrResult = tess.utF8Text
        val extractIndex = ocr.ocrExtract(ocrResult, jsonParsing())
        val ocrViewList = ocr.jsonParsing(extractIndex as ArrayList<Int>, jsonParsing())
        recycle(extractIndex, ocrViewList, jsonParsing())
    }

    /**
     * 키워드 추출
     */

    fun jsonParsing(): JSONArray {
        val jsonString = assets.open("json/data.json").reader().readText()
        return JSONArray(jsonString)
    }

    // 제이슨 파일에서 들고와서 데이터 베이스 저장
    fun recycle(extractIndex: ArrayList<Int>, listExtractViewList: ArrayList<ListExtractView>, jsonArray: JSONArray) {
        rv_profile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter = ListExtractViewAdapter(listExtractViewList)

        val addRunnable = Runnable {
            ocr.insertDB_item(extractIndex, jsonArray, this)
        }

        ocrOk.setOnClickListener {
            //데이터 베이스에 저장
            val addThread = Thread(addRunnable)
            addThread.start()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OCR", extractIndex)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        MyListDb.destroyInstance()
        super.onDestroy()
    }
}

