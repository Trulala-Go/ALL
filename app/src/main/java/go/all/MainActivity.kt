
package go.all

import androidx.appcompat.app.AppCompatActivity
import android.os.*
import android.Manifest
import android.widget.*
import android.view.*
import androidx.appcompat.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.telephony.TelephonyManager

class MainActivity : AppCompatActivity() {

    private lateinit var liner:LinearLayout
    private lateinit var status:ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        liner = findViewById(R.id.liner)
        status = findViewById(R.id.status)
        
        Tombol()
        AturIkon()
        BacaCpu()
        BacaBaterai()
        BacaRam()
        BacaPenyimpanan()
        BacaPerangkat()
        BacaJaringan()
    }
    
    override fun onBackPressed(){
      if(liner.visibility == View.VISIBLE){liner.visibility = View.GONE}
      else{ Keluar() }
    }
    
    private fun Keluar(){
      AlertDialog.Builder(this)
        .setTitle("Keluar")
        .setMessage("Dari Aplikasi ?")
        .setPositiveButton("Ya"){_,_-> finish()}
        .setNegativeButton("Tidak", null)
        .show()
    }
    
    private fun AturIkon(){
      status.setImageResource(
        if(SemuaFile()){R.drawable.sd_terima}
        else{R.drawable.sd_tolak}
      )
    }
    
    override fun onResume(){
      super.onResume()
      AturIkon()
      BacaCpu()
      BacaRam()
      BacaBaterai()
    }
    
    private fun SemuaFile(): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            android.os.Environment.isExternalStorageManager()
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        }

        else -> {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
  }
    
    private fun BukaIzin() {
    when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R -> {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }

        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ),
                100
            )
        }

        else -> {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                101
            )
        }
    }
  }
    
    private fun BacaCpu() {
    val jenis = findViewById<TextView>(R.id.jenis_cpu)
    val suhu = findViewById<TextView>(R.id.suhu_cpu)

    val cpuInfo = try {
        java.io.File("/proc/cpuinfo").readLines().firstOrNull { it.startsWith("Hardware") }
            ?.split(":")
            ?.getOrNull(1)
            ?.trim()
            ?: "Tidak diketahui"
    } catch (e: Exception) {
        "Error baca CPU"
    }

    val cpuTemp = try {
        val file = java.io.File("/sys/class/thermal/thermal_zone0/temp")
        if (file.exists()) {
            val tempStr = file.readText().trim()
            val temp = tempStr.toFloat() / 1000f
            "%.1f °C".format(temp)
        } else {
            "Tidak tersedia"
        }
    } catch (e: Exception) {
        "Error baca suhu"
    }

    jenis.text = "Jenis CPU: $cpuInfo"
    suhu.text = "Suhu CPU: $cpuTemp"
  }
  
  private fun BacaBaterai() {
    val jenis = findViewById<TextView>(R.id.jenis_b)
    val persen = findViewById<TextView>(R.id.persen_b)
    val suhu = findViewById<TextView>(R.id.suhu_b)

    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = registerReceiver(null, intentFilter)

    batteryStatus?.let { status ->
        val level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val persenValue = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val technology = status.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Tidak diketahui"

        val temp = status.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val suhuValue = if (temp != -1) temp / 10.0 else -1.0

        jenis.text = "Jenis: $technology"
        persen.text = "Persen: $persenValue%"
        suhu.text = "Suhu: $suhuValue°C"
    }
  }
  
  private fun BacaRam() {
    val tersedia = findViewById<TextView>(R.id.tersedia)
    val terpakai = findViewById<TextView>(R.id.terpakai)
    val total = findViewById<TextView>(R.id.total)

    val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
    val memInfo = android.app.ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memInfo)

    val totalRam = memInfo.totalMem / (1024 * 1024) 
    val availRam = memInfo.availMem / (1024 * 1024) 
    val usedRam = totalRam - availRam

    tersedia.text = "Tersedia: $availRam MB"
    terpakai.text = "Terpakai: $usedRam MB"
    total.text = "Total: $totalRam MB"
  }
  
  private fun BacaPenyimpanan() {
    val sdSatu = findViewById<TextView>(R.id.sd_satu)
    val sdDua = findViewById<TextView>(R.id.sd_dua)
    val totalSatu = findViewById<TextView>(R.id.total_satu)
    val totalDua = findViewById<TextView>(R.id.total_dua)

    val internal = android.os.Environment.getDataDirectory()
    val statInternal = android.os.StatFs(internal.path)
    val totalInternal = statInternal.blockCountLong * statInternal.blockSizeLong
    val freeInternal = statInternal.availableBlocksLong * statInternal.blockSizeLong

    sdSatu.text = "Tersedia: ${formatSize(freeInternal)}"
    totalSatu.text = "Total: ${formatSize(totalInternal)}"

    val external = getExternalFilesDirs(null)
    if (external.size > 1 && external[1] != null) {
        val extPath = external[1]
        val statExternal = android.os.StatFs(extPath!!.path)
        val totalExternal = statExternal.blockCountLong * statExternal.blockSizeLong
        val freeExternal = statExternal.availableBlocksLong * statExternal.blockSizeLong

        sdDua.visibility = android.view.View.VISIBLE
        totalDua.visibility = android.view.View.VISIBLE

        sdDua.text = "Tersedia: ${formatSize(freeExternal)}"
        totalDua.text = "Total: ${formatSize(totalExternal)}"
    } else {
        sdDua.text = ""
        totalDua.text = ""
        sdDua.visibility = android.view.View.GONE
        totalDua.visibility = android.view.View.GONE
      }
  }

    private fun formatSize(size: Long): String {
      val kb = size / 1024
      val mb = kb / 1024
      val gb = mb / 1024
      return when {
        gb > 0 -> "$gb GB"
        mb > 0 -> "$mb MB"
        else -> "$kb KB"
      }
  }
  
  private fun BacaPerangkat() {
    val model = findViewById<TextView>(R.id.model_hp)
    val tahun = findViewById<TextView>(R.id.tahun) // pembuatan
    val versi = findViewById<TextView>(R.id.versi) // android

    val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

    val buildTime = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(android.os.Build.TIME))

    val androidVersion = android.os.Build.VERSION.RELEASE
    val apiLevel = android.os.Build.VERSION.SDK_INT

    model.text = "Model: $deviceModel"
    tahun.text = "Tahun: $buildTime"
    versi.text = "Android: $androidVersion (API $apiLevel)"
  }
  
  @SuppressLint("MissingPermission", "HardwareIds")
  private fun BacaJaringan() {
    val simSatu = findViewById<TextView>(R.id.sim_satu)
    val simDua = findViewById<TextView>(R.id.sim_dua)
    val imeSatu = findViewById<TextView>(R.id.ime_satu)
    val imeDua = findViewById<TextView>(R.id.ime_dua)

    val telManager = getSystemService(TELEPHONY_SERVICE) as android.telephony.TelephonyManager

    simSatu.text = "SIM 1: ${telManager.simOperatorName ?: "Tidak ada"}"

    if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                imeSatu.text = "IMEI 1: ${telManager.getImei(0) ?: "Tidak tersedia"}"
                imeDua.text = "IMEI 2: ${telManager.getImei(1) ?: "Tidak tersedia"}"
            } catch (e: Exception) {
                imeSatu.text = "IMEI tidak dapat dibaca"
                imeDua.text = ""
            }
        } else {
            imeSatu.text = "IMEI 1: ${telManager.deviceId ?: "Tidak tersedia"}"
            imeDua.text = ""
        }
    } else {
        imeSatu.text = "Izin diperlukan"
        imeDua.text = ""
    }
  }
  
  private fun Tombol(){
      findViewById<TextView>(R.id.keluar).setOnClickListener{
        Keluar()
      }
      
      findViewById<ImageView>(R.id.nav).setOnClickListener{
        liner.visibility = if(liner.visibility == View.GONE)View.VISIBLE else View.GONE
      }
      
      status.setOnClickListener{
        BukaIzin()
      }
      
      findViewById<LinearLayout>(R.id.berkas).setOnClickListener{
        startActivity(Intent(this, Berkas::class.java))
      }
    }
    
}