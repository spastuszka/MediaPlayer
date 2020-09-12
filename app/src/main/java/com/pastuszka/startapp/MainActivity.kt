package com.pastuszka.startapp

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.song_ticket.view.*
import java.lang.Exception

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    var listSongs=ArrayList<SongInfo>()
    var adapter:MySongAdapter?=null
    var mp:MediaPlayer?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //LoadURLOnline()
        CheckUserPermission()
        var mytracking = mySongTrack()
        mytracking.start()
    }

    fun LoadURLOnline(){
        listSongs.add(SongInfo("Song 1","Lorem","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"))
        listSongs.add(SongInfo("Song 2","Lorem","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"))
        listSongs.add(SongInfo("Song 3","Lorem","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3"))
        listSongs.add(SongInfo("Song 4","Lorem","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"))
    }

    inner class MySongAdapter:BaseAdapter{

        var myListSong = ArrayList<SongInfo>()

        constructor(myListSong:ArrayList<SongInfo>):super(){
            this.myListSong=myListSong
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val myView = layoutInflater.inflate(R.layout.song_ticket,null)

            val Song = this.myListSong[position]

            myView.tvSongName.text = Song.Title
            myView.tvAuthor.text = Song.AuthorName

            myView.buPlay.setOnClickListener(View.OnClickListener {

                if(myView.buPlay.text.equals("STOP")){
                    mp!!.stop()
                    myView.buPlay.text = "Start"
                }else {

                    mp = MediaPlayer()
                    try {
                        mp!!.setDataSource(Song.SongURL)
                        mp!!.prepare()
                        mp!!.start()
                        myView.buPlay.text = "STOP"
                        sbProgress.max = mp!!.duration
                    } catch (ex: Exception) {
                    }
                }
            })

            return myView
        }

        override fun getItem(position: Int): Any {
            return this.myListSong[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return this.myListSong.size
        }

    }

    inner class mySongTrack:Thread(){


        override fun run() {
            while (true){
                try {
                    Thread.sleep(1000)
                }catch (ex:Exception){}

                runOnUiThread(){

                    if(mp!=null) {
                        sbProgress.progress = mp!!.currentPosition
                    }
                }
            }
        }
    }

    fun CheckUserPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_CODE_ASK_PERMISSIONS)
                return
            }
        }
        LoadSong()
    }

    private val REQUEST_CODE_ASK_PERMISSIONS=123

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

     when(requestCode){
         REQUEST_CODE_ASK_PERMISSIONS -> if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
             LoadSong()
         }else{
            Toast.makeText(this,"Denied",Toast.LENGTH_LONG).show()
         }else ->  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
     }
    }

    fun LoadSong(){
        val allSongsURL=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val cursor = contentResolver.query(allSongsURL,null,selection,null,null)

        if(cursor!=null){
            if(cursor.moveToFirst()){
                do{
                    val songURl=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val songAuthor=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val songName=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    listSongs.add(SongInfo(songName,songAuthor,songURl))

                }while (cursor.moveToNext())
            }
            cursor.close()
            adapter=MySongAdapter(listSongs)
            lvSongs.adapter=adapter
        }
    }
}
