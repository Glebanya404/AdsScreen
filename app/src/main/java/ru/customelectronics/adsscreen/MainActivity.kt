package ru.customelectronics.adsscreen

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.repository.ServerRepository
import ru.customelectronics.adsscreen.repository.SqlRepository
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.room.AppDatabase
import java.io.File
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.name
    private val updateDelay = 1000 * 30.toLong()
    private val macAddress by lazy {
        getMacAddr()
    }

    private val handler = Handler()
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val videoDao = AppDatabase.getDatabase(applicationContext).videoDao()
        val urlDao = AppDatabase.getDatabase(applicationContext).urlDao()
        val viewModelFactory = MainViewModelFactory(ServerRepository(macAddress), SqlRepository(videoDao, urlDao), "${getExternalFilesDir(null)}${File.separator}")
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        activity_main__macAddress_textView.text = macAddress
        activity_main__connStatus_textView.text = "Status:"
        activity_main__videoList_recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = VideoAdapter()
        activity_main__videoList_recyclerView.adapter = adapter

        activity_main__showVideoButton.setOnClickListener {
            if (activity_main__videoView.visibility == View.INVISIBLE) {
                activity_main__videoView.setVideoPath(viewModel.getNextVideo())
                activity_main__videoView.visibility = View.VISIBLE
                activity_main__videoView.start()
            }
            else {
                activity_main__videoView.stopPlayback()
                activity_main__videoView.visibility = View.INVISIBLE
            }
        }

        activity_main__videoView.setMediaController(null)
        activity_main__videoView.setOnCompletionListener {
            activity_main__videoView.setVideoPath(viewModel.getNextVideo())
            activity_main__videoView.start()
        }
        activity_main__videoView.setOnErrorListener { mediaPlayer, i, i2 ->
            Log.d(TAG, "onCreate: Error while trya paly video")
            return@setOnErrorListener true
        }


        viewModel.connectionState.observe(this){
            activity_main__connStatus_textView.text = "Status: ${it.msg}"
        }
        viewModel.sqlVideoList.observe(this, { videoList ->
            for (video in videoList) {
                Log.d(TAG, "onCreate: From SQL: $video")
            }
            adapter.setVideoList(videoList)
        })
        viewModel.sqlUrlList.observe(this, { urlList ->
            for (url in urlList) {
                Log.d(TAG, "onCreate: From SQL of Url: $url")
            }
        })
        viewModel.defaultQueue.observe(this, {
            if (activity_main__videoView.duration == -1 && viewModel.defaultQueue.value?.size != 0) {
                activity_main__videoView.setVideoPath(viewModel.getNextVideo())
                activity_main__videoView.visibility = View.VISIBLE
                activity_main__videoView.start()
            }
        })


        val runnable = object : Runnable {
            override fun run() {
                Log.d(TAG, "run: Check server update")
                viewModel.checkServerUpdate()
                handler.postDelayed(this, updateDelay)
            }
        }
        handler.postDelayed(runnable, 1000 * 5L)


    }
    fun getMacAddr(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.getName().equals("wlan0", ignoreCase=true)) continue

                val macBytes = nif.getHardwareAddress() ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
        }
        return "02:00:00:00:00:00"
    }

    inner class VideoAdapter: RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

        private var videoList = emptyList<Video>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapter.ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.video_item_view, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: VideoAdapter.ViewHolder, position: Int) {
            holder.titleTV?.text = videoList[position].title
            holder.fileNameTV?.text = videoList[position].fileName
            holder.dateTV?.text = videoList[position].dateOfUpload
        }

        override fun getItemCount() = videoList.size

        fun setVideoList(newVideoList: List<Video>) {
            this.videoList = newVideoList
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            var titleTV: TextView? = null
            var fileNameTV: TextView? = null
            var dateTV: TextView? = null
            init {
                titleTV = itemView.findViewById(R.id.video_item_view__title)
                fileNameTV = itemView.findViewById(R.id.video_item_view__filename)
                dateTV = itemView.findViewById(R.id.video_item_view__dateOfUpload)
            }
        }

    }


}