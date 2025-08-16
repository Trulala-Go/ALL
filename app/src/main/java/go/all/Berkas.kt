package go.all

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.*
import android.view.*
import android.widget.*
import android.content.*
import java.io.*

class Berkas : AppCompatActivity(){

  private lateinit var liner:LinearLayout
  private lateinit var jalur:TextView

  override fun onCreate(savedInstanceState:Bundle?){
    super.onCreate(savedInstanceState)
      setContentView(R.layout.berkas)
      
      liner = findViewById(R.id.liner)
      jalur = findViewById(R.id.jalur)
      
      val memo = Environment.getExternalStorageDirectory()
      Segarkan(memo)
      
      Tombol()
      
  }
  
  override fun onBackPressed(){
  val memo = File(jalur.text.toString())
    if(liner.visibility == View.VISIBLE){liner.visibility = View.GONE}
    else{
      val paren = memo.parentFile
      if(paren == null){Toast.makeText(this, "Mentok", Toast.LENGTH_SHORT).show()}
      else{Segarkan(paren)}
    }
  }
  
  private fun Keluar(){
    AlertDialog.Builder(this)
      .setTitle("Keluar")
      .setMessage("Dari Berkas")
      .setPositiveButton("Ya"){_,_-> finish()}
      .setNegativeButton("Tidak", null)
      .show()
  }
  
  private fun Tombol(){
    findViewById<TextView>(R.id.keluar).setOnClickListener{
      Keluar()
    }
    
    findViewById<ImageView>(R.id.nav).setOnClickListener{
      liner.visibility = if(liner.visibility == View.GONE)View.VISIBLE else View.GONE
    }
    
    findViewById<ImageView>(R.id.apk).setOnClickListener{
        val memo = filesDir
        Segarkan(memo)
        liner.visibility = View.GONE
      }
      
      findViewById<ImageView>(R.id.sd).setOnClickListener{
        val memo = Environment.getExternalStorageDirectory()
        Segarkan(memo)
        liner.visibility = View.GONE
      }
      
  }
  
  private fun Segarkan(memo:File){
    jalur.text = memo.absolutePath
    
    AturGrid(memo)
  }
  
  private fun AturGrid(memo:File){
    val grid = findViewById<GridLayout>(R.id.grid)
      grid.removeAllViews()
      
    val item = LayoutInflater.from(this).inflate(R.layout.item_vertical, grid, false)
    val nama = item.findViewById<TextView>(R.id.nama)
    val gambar = item.findViewById<ImageView>(R.id.gambar)
    
    nama.text = memo.name
    gambar.setImageResource(
      if(memo.isDirectory){R.drawable.folder}
      else{R.drawable.file}
    )
    
    grid.addView(item)
  }
}
