package zandoh.com.polyview;

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.CookieHandler
import java.net.CookieManager

class PolyDataProvider {
    var client: OkHttpClient
    var loggedIn = false

    constructor(context: Context) {
        val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
    }

    fun postCredentials(url: String) {
        val body = FormBody.Builder()
                .add("j_username", "zdohnale")
                .add("j_password", "***REMOVED***")
                .add("_eventId_proceed", "")
                .build()

        val request = okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .header("Referer", url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Host", "idp.calpoly.edu")
                .header("Cache-Control", "max-age=0")
                .header("Origin", "https://idp.calpoly.edu")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("MYAPP", "FAIL")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        loggedIn = true
                        getClassData()
                    }
                })
    }

    fun polyLogin() {
        val first_url = "https://idp.calpoly.edu/idp/profile/cas/login?service=https://myportal.calpoly.edu/Login"

        val request = okhttp3.Request.Builder().url(first_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("MYAPP", e.toString())
                        Log.d("MYAPP", "FAILED")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val post_url = response.request().url().toString()
                        postCredentials(post_url)
                    }
                })
    }

    fun getClassData() {
        val data_url = "https://myportal.calpoly.edu/f/u17l1s6/p/myclasses.u17l1n1696/normal/getCurrentEnrollment.resource.uP"

        val request = okhttp3.Request.Builder().url(data_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("MYJSON", "NO DICE")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        var returnedJSON = response.body()?.string()!!

                        val classes = Gson().fromJson(returnedJSON, JSONClasses::class.java)
                        Log.d("MYJSON", classes.toString())

                        getPolylearnData()
                    }
                })
    }

    fun getPolylearnData() {

    }
}

fun logLong(longStr: String) {
    for(i in 0..longStr.length step 1000) {
        Log.d("MYAPP", longStr.substring(i, Math.min(i + 1000, longStr.length)))
    }
}

data class JSONClasses(val items: ArrayList<JSONClass>, val term: Term)

data class Term(
        @SerializedName("termCode")
        val code: String
)

data class JSONClass(
        @SerializedName("classLabel")
        val name: String,
        @SerializedName("courseCatalogDescription")
        val longName: String,
        @SerializedName("meetingPatterns")
        val times: ArrayList<JSONSchedule>
)

data class JSONSchedule(
        val days: String,
        val startTime: String,
        val endTime: String,
        @SerializedName("facilityBuildingCode")
        val building: String,
        @SerializedName("facilityRoom")
        val room: String
)

data class PolyClass(val short_name: String, val full_name: String, val location: String)

data class PolyAssignment(val assignment_name: String, val assignment_due: String, val submitted: Boolean)