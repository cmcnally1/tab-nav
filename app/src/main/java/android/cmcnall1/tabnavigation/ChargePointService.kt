package android.cmcnall1.tabnavigation

import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

class ChargePointService {

    var listener: DataListener? = null

    fun fetchJson(long: String, lat: String){

        val url = "https://api.openchargemap.io/v2/poi/?output=json&longitude=$long&latitude=$lat&maxresults=30"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object: Callback {

            override fun onResponse(call: Call, response: Response) {

                val body = response.body()?.string()

                val gson = GsonBuilder().create()

                val result = gson.fromJson(body, Array<PointInfo?>::class.java)

                listener?.onHttpResponseReceived(result)

            }

            override fun onFailure(call: Call, e: IOException) {
                //Print message if an error occurs while fetching information
                println("Failed to execute request")
            }
        })

    }

    interface DataListener {
        fun onHttpResponseReceived(response: Array<PointInfo?>)
    }
}