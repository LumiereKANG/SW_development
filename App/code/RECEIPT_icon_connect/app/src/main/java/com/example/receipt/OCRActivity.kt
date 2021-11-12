package com.example.receipt

import android.content.ContentValues
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.receipt.databinding.ActivityOcractivityBinding//
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_ocractivity.*
import org.json.JSONArray
import java.io.*
import java.text.SimpleDateFormat
import android.net.Uri as Uri
import kotlin.collections.List as List
import java.time.LocalDate

class OCRActivity : PermissionActivity() {

    val PERM_STORAGE = 9
    val PERM_CAMERA = 10
    val REQ_CAMERA = 11
    val REQ_GALLERY = 12

    val Iconimage= listOf<Int>(R.drawable.icon_1, R.drawable.icon_2,R.drawable.icon_3,
        R.drawable.icon_4,R.drawable.icon_5, R.drawable.icon_6,R.drawable.icon_7,
        R.drawable.icon_8, R.drawable.icon_9,R.drawable.icon_10)

    lateinit var tess : TessBaseAPI
    var dataPath : String = ""


    val binding by lazy { ActivityOcractivityBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root);


        ocrOk.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        requirePermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STORAGE)
        /**
         * ocr
         */
        dataPath = filesDir.toString() + "/tesseract/"
        checkFile(File(dataPath + "tessdata/"), "kor")
        var lang: String = "kor"
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

    // 3.카메라에 찍은 사진을 저장하기 위한 uri넘겨준다.
    fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageUri(newFileName(), "image/jpg")?.let{ uri ->
            realUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            startActivityForResult(intent, REQ_CAMERA)
        }
    }

    fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQ_GALLERY)

    }

    //원본 이미지를 저장할 Uri를 MediaStore(데이터베이스)에 생성하는 메서드
    fun createImageUri(filename:String, mimeType:String) : Uri?{
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename)
        values.put(MediaStore.Images.Media.MIME_TYPE,mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
    }

    // 파일 이름을 생성하는 메서드
    fun newFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    //원본 이미지를 불러오는 메서드
    fun loadBitmap(photoUri: Uri): Bitmap? {
        var image: Bitmap? =null
        try {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                image = ImageDecoder.decodeBitmap(source)
            }else {
                image = MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
            }

        }catch(e:Exception){
            e.printStackTrace()
        }
        return image
    }

    override fun permissionGranted(requestCode: Int) {
        Log.d("test", requestCode.toString())
        when(requestCode){
            PERM_STORAGE -> initViews()
            PERM_CAMERA -> openCamera()
        }

    }

    override fun permissionDenied(requestCode: Int) {
        Log.d("test", "permissiondenied")
        when(requestCode) {
            PERM_STORAGE -> {
                Toast.makeText(this, "공용 저장소 권한을 승인해야 앱을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            PERM_CAMERA -> Toast.makeText(this, "카메라 권한을 승인해야 카메라를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    //4.카메라를 찍은 후에 호출된다. 6.갤러리에서 선택 후 추가된다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                REQ_CAMERA ->{
                    realUri?.let{ uri->
                        val bitmap = loadBitmap(uri)
                        binding.imagePreViewOCR.setImageBitmap(bitmap)// 촬영한 이미지 가져옴
                        if (bitmap != null) {
                            processImage(bitmap.copy(Bitmap.Config.ARGB_8888,true)) //ocr로 추출한 키워드 표시
                            //intent.putExtra("bitmap", Data(bitmap) as Serializable)
                            //startActivity(intent)
                        }
                    }
                    //val bitmap = data?.extras?.get("data") //미리보기 이미지
                    //binding.imagePreview.setImageBitmap(bitmap)

                }

                REQ_GALLERY -> {
                    data?.data?.let { uri ->
                        val bitmap = loadBitmap(uri)
                        binding.imagePreViewOCR.setImageURI(uri)
                        if (bitmap != null) {
                            processImage(bitmap.copy(Bitmap.Config.ARGB_8888,true)) // ocr로 추출한 키워드 표시
                        }

                    }
                }
            }

        }
    }

    /**
     *  OCR 구현
     */

    fun copyFile(lang : String){
        try{

            var filePath : String = dataPath+"/tessdata/"+lang+".traineddata"

            //AssetManager를 사용하기 위한 객체 생성
            var assetManager : AssetManager = getAssets();

            //byte 스트림을 읽기 쓰기용으로 열기
            var inputStream : InputStream = assetManager.open("tessdata/"+lang+".traineddata")
            var outStream : OutputStream = FileOutputStream(filePath)


            //위에 적어둔 파일 경로쪽으로 해당 바이트코드 파일을 복사한다.
            var buffer : ByteArray = ByteArray(1024)

            var read : Int = 0
            read = inputStream.read(buffer)
            while(read!=-1){
                outStream.write(buffer,0,read)
                read = inputStream.read(buffer)
            }
            outStream.flush()
            outStream.close()
            inputStream.close()

        }catch(e : FileNotFoundException){
            Log.v("오류발생",e.toString())
        }catch (e : IOException)
        {
            Log.v("오류발생",e.toString())
        }
    }

    fun checkFile(dir : File, lang : String){

        if(!dir.exists()&&dir.mkdirs()){
            copyFile(lang)
        }

        if(dir.exists()){
            var datafilePath : String =  dataPath + "/tessdata/"+lang+"traineddata"
            var dataFile : File = File(datafilePath)
            if(!dataFile.exists()){
                copyFile(lang)
            }
        }

    }

    fun processImage(bitmap : Bitmap){
        Toast.makeText(applicationContext,"잠시 기다려 주세요", Toast.LENGTH_SHORT).show()
        var ocrResult : String? = null;
        tess.setImage(bitmap)
        ocrResult = tess.utF8Text
        //binding.ocrlist.text = ocrResult // 그냥 추출한 텍스트 가공 안 거치고 출력
        //val extractOcr: String = ocr_extract(ocrResult).joinToString("\n") //리스트를 다시 문자로 변환
        val extractOcr: ArrayList<Int> = ocr_extract(ocrResult) as ArrayList<Int>
        recycle(extractOcr, jsonParsing())
        //binding.ocrlist.text = extractOcr //문자열로 변환된 키워드 출력
    }




    fun uri_to_bitmap(uri: Uri): Bitmap? {
        var bitmap: Bitmap? =null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri))
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
            }

        } catch (e: IOException){
            e.printStackTrace()
        }

        return bitmap
    }

    /**
     * 키워드 추출
     */

    fun jsonParsing(): JSONArray{
        var jsonString = assets.open("json/data.json").reader().readText()
        val jsonArray = JSONArray(jsonString)

        return jsonArray
    }

    fun textExtract(extractAllingre : List<String>, jsonArray: JSONArray): List<Int>{
        //extractAllingre = 영수증에서 추출된 하나의 요소들
        //ingredient_list = 미리 지정해놓은 재료 카테고리
        val ingredient_list = mutableListOf<String>()
        for(index in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(index)

            val name = jsonObject.getString("Food")
//            val id = jsonObject.getString("Category")
//            val date = jsonObject.getString("ExpirationDate")
            ingredient_list.add(name)

        }

        var displaylist = ArrayList<Int>()

        for((index, value) in ingredient_list.withIndex()) {
            val displaying = extractAllingre.filter { X -> X.contains(value) }
            if (displaying.isNotEmpty()){
                displaylist.add(index)
            }
        }
        return displaylist
    }

    fun ocr_extract(ocr_text : String): List<Int> {
        var ocr_list = ocr_text.split("\\n")
        var result_list = textExtract(ocr_list,jsonParsing())
        return result_list
    }

    fun recycle(ingre_result: ArrayList<Int>, jsonArray: JSONArray) {
        val today: LocalDate = LocalDate.now()
        val profilelist = ArrayList<Profiles>()
        for (i in ingre_result){
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("Food")
            val cate = jsonObject.getString("Category")
            val day = jsonObject.getString("ExpirationDate")

            profilelist.add(Profiles(Iconimage[i], name, cate, today.plusDays(day.toLong())))
        }


        rv_profile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        rv_profile.setHasFixedSize(true)

        rv_profile.adapter = ProfileAdapter(profilelist)
        //intent.putExtra("OCR_list", ingre_result )

        ocrOk.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OCR",ingre_result)
            startActivity(intent)
        }
    }
}

