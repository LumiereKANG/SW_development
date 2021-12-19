package com.example.receipt.ocr

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.receipt.MainActivity
import com.example.receipt.recycle.OcrViewAdapter
import com.example.receipt.recycle.OcrView
import com.example.receipt.R
import com.example.receipt.databinding.ActivityListextractBinding//
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_listextract.*
import org.json.JSONArray
import com.example.receipt.room.Mylist
import com.example.receipt.room.MylistDB
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.*
import java.text.SimpleDateFormat
import android.net.Uri as Uri
import java.time.LocalDate

class ListExtractActivity : PermissionClass() {

    private val PERM_STORAGE = 9
    private val PERM_CAMERA = 10
    private val REQ_CAMERA = 11
    private val REQ_GALLERY = 12

    private val ocr = OcrClass()
    private val TAG = "AppDebug"
    private var mylistDb: MylistDB? = null

    private val iconImage = listOf<Int>(
        R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3,
        R.drawable.icon_4, R.drawable.icon_5, R.drawable.icon_6, R.drawable.icon_7,
        R.drawable.icon_8, R.drawable.icon_9, R.drawable.icon_10
    )

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

        /**
         * ocr
         */

        dataPath = filesDir.toString() + "/tesseract/"

        val assetManager: AssetManager = getAssets()

        checkFile(File(dataPath + "tessdata/"), "kor",dataPath ,assetManager)

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

    var realUri: Uri? = null

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

        if(resultCode == RESULT_OK){
            when(requestCode){
                REQ_CAMERA ->{
                    realUri?.let{ uri->
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
                                val extractIndex = processImage(bitmap,tess,applicationContext) // ocr로 추출한 키워드 표시
                                recycle(extractIndex,ocr.jsonParsing(extractIndex))
                            }
                        }
                        binding.imagePreViewOCR.setImageURI(result.uri)
                    }
                    else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                        Log.e(TAG, "Crop error: ${result.error}")
                    }
                }
            }
        }
    }

    /**
     *  OCR 구현
     */

    fun copyFile(lang: String) {
        try {

            val filePath: String = dataPath + "/tessdata/" + lang + ".traineddata"

            //AssetManager를 사용하기 위한 객체 생성
            val assetManager: AssetManager = getAssets()

            //byte 스트림을 읽기 쓰기용으로 열기
            val inputStream: InputStream = assetManager.open("tessdata/" + lang + ".traineddata")
            val outStream: OutputStream = FileOutputStream(filePath)


            //위에 적어둔 파일 경로쪽으로 해당 바이트코드 파일을 복사한다.
            val buffer = ByteArray(1024)

            var read: Int
            read = inputStream.read(buffer)
            while (read != -1) {
                outStream.write(buffer, 0, read)
                read = inputStream.read(buffer)
            }
            outStream.flush()
            outStream.close()
            inputStream.close()

        } catch (e: FileNotFoundException) {
            Log.v("오류발생", e.toString())
        } catch (e: IOException) {
            Log.v("오류발생", e.toString())
        }
    }

    fun checkFile(dir: File, lang: String) {

        if (!dir.exists() && dir.mkdirs()) {
            copyFile(lang)
        }
        if (dir.exists()) {
            val datafilePath: String = dataPath + "/tessdata/" + lang + "traineddata"
            val dataFile = File(datafilePath)
            if (!dataFile.exists()) {
                copyFile(lang)
            }
        }
    }


    fun processImage(bitmap: Bitmap) {
        Toast.makeText(applicationContext, "잠시 기다려 주세요", Toast.LENGTH_SHORT).show()
        tess.setImage(bitmap)
        var ocrResult = tess.utF8Text
        val extractOcr: ArrayList<Int> = ocrExtract(ocrResult) as ArrayList<Int>
        Log.d("OCR 추출", extractOcr[0].toString())
        recycle(extractOcr, jsonParsing())
    }


    private fun launchImageCrop(uri: Uri): Bitmap? {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
        return loadBitmap(uri)

    }


    fun jsonParsing(): JSONArray {
        val jsonString = assets.open("json/data.json").reader().readText()
        return JSONArray(jsonString)
    }

    fun textExtract(extractAllingre: List<String>, jsonArray: JSONArray): List<Int> {
        val itemName = mutableListOf<String>()

        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val name = jsonObject.getString("Food")
            itemName.add(name)
        }

        val indexlist = ArrayList<Int>()

        for ((index, value) in itemName.withIndex()) {
            val displaying = extractAllingre.filter { X -> X.contains(value) }
            if (displaying.isNotEmpty()) {
                indexlist.add(index)
            }
        }
        return indexlist
    }

    fun ocrExtract(ocr_text: String): List<Int> {
        val ocrList = ocr_text.split("\\n")
        val resultList = textExtract(ocrList, jsonParsing())
        return resultList
    }



    // 제이슨 파일에서 들고와서 데이터 베이스 저장
    fun insertDB_item(ingre_result: ArrayList<Int>, jsonArray: JSONArray) {
        val today: LocalDate = LocalDate.now()
        val newitem = Mylist()

        for (i in ingre_result) {
            val jsonObject = jsonArray.getJSONObject(i)
            newitem.item = iconImage[i]
            newitem.name = jsonObject.getString("Food")
            newitem.category = jsonObject.getString("Category")
            newitem.index =  jsonObject.getInt("Index")

            val day = jsonObject.getString("ExpirationDate")
            newitem.dates = today.plusDays(day.toLong()).toString()

            mylistDb = MylistDB.getInstance(this)
            mylistDb?.mylistDao()?.insert(newitem)
        }
    }

    fun recycle(extractIndex: ArrayList<Int>, profilelist : ArrayList<OcrView>) {

        rv_profile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)

        rv_profile.adapter = OcrViewAdapter(profilelist)
        //intent.putExtra("OCR_list", ingre_result )

        val addRunnable = Runnable {
            insertDB_item(extractIndex, ocr.jsonParsing_array())
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
        MylistDB.destroyInstance()
        super.onDestroy()
    }
}

