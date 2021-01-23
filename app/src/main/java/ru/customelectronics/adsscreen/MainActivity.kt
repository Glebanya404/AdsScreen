package ru.customelectronics.adsscreen

import android.os.Bundle
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
import ru.customelectronics.adsscreen.repository.Repository
import java.net.NetworkInterface
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.name
    private val macAddress by lazy {
        getMacAddr()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        activity_main__macAddress_textView.text = macAddress
        activity_main__connStatus_textView.text = "Status:Not connected"
        activity_main__videoList_recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = VideoAdapter(viewModel.videoList)
        activity_main__videoList_recyclerView.adapter = adapter



        viewModel.checkServerUpdate()




        viewModel.connectionState.observe(this){
            activity_main__connStatus_textView.text = "Status: ${it.msg}"

        }
        viewModel.videoList.observe(this, {
            adapter.notifyDataSetChanged()
        })
//        button_post.setOnClickListener {
//            val password = password_editText.text.toString()
//            viewModel.signIn(User("foo", password))
//        }
//        viewModel.signInResponse.observe(this, Observer { response ->
//            if (response.isSuccessful) {
//                RetrofitInstance.jwt = JSONObject(response.body().toString()).getString("jwt")
//                Log.d("Response", RetrofitInstance.jwt)
//            }
//            Log.d("Response", response.code().toString())
//        })
//
//        button_get.setOnClickListener{
//            val id = id_editText.text.toString()
//            if (id == "") return@setOnClickListener
//            viewModel.getVideo(Integer.parseInt(id))
//
//        }
//        viewModel.videoResponse.observe(this, Observer { response ->
//            if (response.isSuccessful) {
//                textView.text = response.body().toString()
//            } else {
//                textView.text = response.code().toString()
//            }
//        })
//
//        button_getall.setOnClickListener {
//            viewModel.getVideos()
//        }
//        viewModel.videosResponse.observe(this, { response ->
//            if (response.isSuccessful) {
//                textView.text = response.body().toString()
//                response.body()?.forEach {
//                    Log.d(TAG, "onCreate: $it")
//                }
//            } else {
//                textView.text = response.code().toString()
//            }
//        })
//
//        button_download.setOnClickListener {
//            val id = id_editText.text.toString()
//            if (id == "") return@setOnClickListener
//            viewModel.downloadVideo(Integer.parseInt(id), getExternalFilesDir(null).toString())
//        }
//        viewModel.downloadResponse.observe(this, { response ->
//            if (response.isSuccessful) {
//
//            } else {
//                textView.text = response.code().toString()
//            }
//        })
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
                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
        }
        return "02:00:00:00:00:00"
    }

    inner class VideoAdapter(val videoList: MutableLiveData<List<Video>>): RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapter.ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.video_item_view, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: VideoAdapter.ViewHolder, position: Int) {
            holder.titleTV?.text = videoList.value!![position].title
            holder.fileNameTV?.text = videoList.value!![position].fileName
            holder.dateTV?.text = videoList.value!![position].dateOfUpload
        }

        override fun getItemCount() = videoList.value?.size ?: 0

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