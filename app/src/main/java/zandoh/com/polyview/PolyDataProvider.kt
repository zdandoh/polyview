package zandoh.com.polyview;

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.util.Log
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
import kotlinx.android.parcel.Parcelize


class PolyDataProvider {
    var client: OkHttpClient
    lateinit var callback: (()->Unit)
    val fragment: Fragment

    constructor(context: Context, fragment: Fragment) {
        val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        this.fragment = fragment
        client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
    }

    fun collectData(username: String, password: String, callback: (()->Unit)) {
        this.callback = callback
        polyLogin(username, password)
    }

    private fun postCredentials(url: String, username: String, password: String) {
        val body = FormBody.Builder()
                .add("j_username", username)
                .add("j_password", password)
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
                        Log.d("POLYHTTP", "LOGIN FAIL")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        getClassData()
                    }
                })
    }

    private fun polyLogin(username: String, password: String) {
        val first_url = "https://idp.calpoly.edu/idp/profile/cas/login?service=https://myportal.calpoly.edu/Login"

        val request = okhttp3.Request.Builder().url(first_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "PRE-LOGIN FAILED")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val post_url = response.request().url().toString()
                        postCredentials(post_url, username, password)
                    }
                })
    }

    private fun getClassData() {
        val data_url = "https://myportal.calpoly.edu/f/u17l1s6/p/myclasses.u17l1n1696/normal/getCurrentEnrollment.resource.uP"

        val request = okhttp3.Request.Builder().url(data_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "CLASS DATA REQUEST FAILED")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        var returnedJSON = response.body()?.string()!!
                        response.body()?.close()

                        val classes = Gson().fromJson(returnedJSON, JSONClasses::class.java)

                        classes.items = classes.items.filter {
                            !it.times.isEmpty()
                        } as ArrayList<JSONClass>

                        classes.items.sortBy {
                            it.name
                        }

                        for(classItem in classes.items) {
                            classItem.name = classItem.name.substring(0, classItem.name.lastIndexOf("-"))
                            classItem.name = classItem.name.replaceFirst("-", " ")

                            classItem.times[0].building = classItem.times[0].building.removePrefix("0")
                            classItem.times[0].room = classItem.times[0].room.removePrefix("0")

                            classItem.times[0].buildingName = classItem.times[0].buildingName.substring(0, classItem.times[0].buildingName.lastIndexOf(" "))
                        }

                        getPolylearnLinks(classes)
                    }
                })
    }

    private fun getPolylearnLinks(classData: JSONClasses) {
        val polylearn_url = "https://myportal.calpoly.edu/f/u17l1s6/p/myclasses.u17l1n1696/normal/moodleLinks.resource.uP?terms=2188"

        val request = okhttp3.Request.Builder().url(polylearn_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "FAILED TO GET CLASS URLS")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        var returnedJson = response.body()?.string()
                        response.body()?.close()

                        val polyLearnLinks = Gson().fromJson(returnedJson, JSONMap::class.java)

                        polyLearnLinks.map.keys.forEach {key ->
                            for(classInfo in classData.items) {
                                if(key.equals(classInfo.name)) {
                                    classInfo.polylearnUrl = polyLearnLinks.map[key]!!.url
                                }
                            }
                        }

                        val model = ViewModelProviders.of(fragment.activity!!).get(PolylearnModel::class.java)
                        val prefs = fragment.activity!!.getPreferences(MODE_PRIVATE).edit()

                        model.writeClasses(classData, prefs)
                        callback.invoke()
                        Log.d("POLYINFO", "LOGIN SUCCESSFUL")
                    }
                })
    }

    fun getPolylearnData(url: String) {
        val request = okhttp3.Request.Builder().url(url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "FAILED TO GET POLYLEARN DATA")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val source = response.body()?.string()!!
                        response.body()?.close()
                        val data = parsePolylearn(source)
                        logLong(source)
                        Log.d("MYDATA", data.toString())
                    }
                })
    }
}

fun logLong(longStr: String) {
    for(i in 0..longStr.length step 1000) {
        Log.d("MYAPP", longStr.substring(i, Math.min(i + 1000, longStr.length)))
    }
}

@Parcelize
data class JSONClasses(
        var items: ArrayList<JSONClass>,
        val term: Term): Parcelable

@Parcelize
data class Term(
        @SerializedName("termCode")
        val code: String
): Parcelable

@Parcelize
data class JSONClass(
        @SerializedName("classLabel")
        var name: String,
        @SerializedName("componentCode")
        val classType: String,
        @SerializedName("courseCatalogDescription")
        val longName: String,
        @SerializedName("meetingPatterns")
        val times: ArrayList<JSONSchedule>,
        var polylearnUrl: String?
): Parcelable

@Parcelize
data class JSONSchedule(
        val days: String,
        val startTime: String,
        val endTime: String,
        @SerializedName("facilityBuildingCode")
        var building: String,
        @SerializedName("facilityRoom")
        var room: String,
        @SerializedName("facilityDescription")
        var buildingName: String
): Parcelable

data class JSONMap(val map: Map<String, JSONMapLink>)

data class JSONMapLink(val url: String)

data class PolyAssignment(val assignment_name: String, val assignment_due: String, val submitted: Boolean)