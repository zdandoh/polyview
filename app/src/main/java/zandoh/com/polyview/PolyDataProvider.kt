package zandoh.com.polyview;

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.DrawableUtils
import android.util.Log
import android.widget.Toast
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


val SIDEBAR_PL_GROUP_ID = 5

fun addPLToSidebar(activity: MainActivity, model: PolylearnModel, url: String) {
    val relatedClassIndex = model.classes?.items?.indexOfFirst { it.polylearnUrl == url }
    if(relatedClassIndex == -1 || relatedClassIndex == null) {
        return
    }
    val relatedClass = model.classes?.items?.get(relatedClassIndex)

    if(relatedClass != null) {
        activity.runOnUiThread {
            val newSidebarItem = activity.nav_view.menu.add(SIDEBAR_PL_GROUP_ID, relatedClassIndex, relatedClassIndex, relatedClass.name)
            newSidebarItem.setIcon(R.drawable.pl_sidebar_icon)
        }
    }
}

class PolyDataProvider {
    var client: OkHttpClient
    lateinit var callback: (()->Unit)
    val activity: MainActivity

    constructor(context: Context, activity: MainActivity) {
        val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        this.activity = activity
        client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
    }

    fun collectData(username: String, password: String, callback: (()->Unit), refreshDataCallback: (() -> Unit)? = null) {
        val model = ViewModelProviders.of(activity).get(PolylearnModel::class.java)
        model.resetData()
        activity.nav_view.menu.removeGroup(SIDEBAR_PL_GROUP_ID)

        this.callback = callback
        polyLogin(username, password, refreshDataCallback = refreshDataCallback)
    }

    private fun postCredentials(url: String, username: String, password: String, refreshDataCallback: (() -> Unit)? = null, loginCallback: (() -> Unit)? = null, loginFailCallback: (() -> Unit)? = null) {
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
                        loginFailCallback?.invoke()
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        if(refreshDataCallback != null) {
                            getClassData(refreshDataCallback)
                        }

                        if(loginCallback != null) {
                            loginCallback.invoke()
                        }
                    }
                })
    }

    fun polyLogin(username: String, password: String, refreshDataCallback: (() -> Unit)? = null, loginCallback: (() -> Unit)? = null, loginFailCallback: (() -> Unit)? = null) {
        val first_url = "https://idp.calpoly.edu/idp/profile/cas/login?service=https://myportal.calpoly.edu/Login"

        val request = okhttp3.Request.Builder().url(first_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "PRE-LOGIN FAILED")
                        loginFailCallback?.invoke()
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val post_url = response.request().url().toString()
                        response.body()?.close()
                        postCredentials(post_url, username, password, refreshDataCallback=refreshDataCallback, loginCallback = loginCallback, loginFailCallback = loginFailCallback)
                    }
                })
    }

    private fun getClassData(refreshDataCallback: (() -> Unit)?) {
        val data_url = "https://myportal.calpoly.edu/f/u17l1s6/p/myclasses.u17l1n1696/normal/getCurrentEnrollment.resource.uP"

        val request = okhttp3.Request.Builder().url(data_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "CLASS DATA REQUEST FAILED")
                        refreshDataCallback?.invoke()
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        var returnedJSON = response.body()?.string()!!
                        response.body()?.close()

                        val classes = Gson().fromJson(returnedJSON, JSONClasses::class.java)

                        classes.items = classes.items.filter {
                            !it.times.isEmpty()
                        } as ArrayList<JSONClass>

                        classes.items = classes.items.filter {
                            it.enrollmentStatus.statusCode != "D"
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

                        getPolylearnLinks(classes, refreshDataCallback)
                    }
                })
    }

    private fun getPolylearnLinks(classData: JSONClasses, refreshDataCallback: (() -> Unit)?) {
        val polylearn_url = "https://myportal.calpoly.edu/f/u17l1s6/p/myclasses.u17l1n1696/normal/moodleLinks.resource.uP?terms=" + classData.term.code

        val request = okhttp3.Request.Builder().url(polylearn_url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "FAILED TO GET CLASS URLS")
                        refreshDataCallback?.invoke()
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        var returnedJson = response.body()?.string()
                        response.body()?.close()

                        val polyLearnLinks = Gson().fromJson(returnedJson, JSONMap::class.java)

                        polyLearnLinks.map.keys.forEach {key ->
                            for(classInfo in classData.items) {
                                val keyParts = key.split("-")
                                val classNameParts = classInfo.name.split(" ")
                                if(keyParts[0].equals(classNameParts[0]) && keyParts[1].equals(classNameParts[1].split("-")[0])) {
                                    classInfo.polylearnUrl = polyLearnLinks.map[key]!!.url
                                }
                            }
                        }

                        val model = ViewModelProviders.of(activity).get(PolylearnModel::class.java)
                        val prefs = activity.getPreferences(MODE_PRIVATE).edit()

                        model.writeClasses(classData, prefs)
                        callback.invoke()
                        Log.d("POLYINFO", "LOGIN SUCCESSFUL")

                        val urlSet = HashSet<String>()
                        for(classItem in classData.items) {
                            if(classItem.polylearnUrl != null) {
                                urlSet.add(classItem.polylearnUrl!!)
                            }
                        }

                        model.assignments.items.clear()
                        getPolylearnData(urlSet, refreshDataCallback)
                    }
                })
    }

    fun checkForUpdate() {

    }

    private fun getPolylearnData(urls: HashSet<String>, refreshDataCallback: (() -> Unit)?) {
        if(urls.isEmpty()) {
            refreshDataCallback?.invoke()
            return
        }

        val url = urls.first()
        urls.remove(url)
        val request = okhttp3.Request.Builder().url(url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "FAILED TO GET POLYLEARN DATA")
                        Log.d("POLYHTTP", e.message)
                        refreshDataCallback?.invoke()
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val source = response.body()?.string()!!
                        response.body()?.close()
                        val data = parsePolylearn(source)

                        getAssignments(data)

                        val model = ViewModelProviders.of(activity).get(PolylearnModel::class.java)
                        val prefs = activity.getPreferences(MODE_PRIVATE).edit()
                        model.writePolylearnData(url, data, prefs)

                        addPLToSidebar(activity, model, url)

                        getPolylearnData(urls, refreshDataCallback)
                    }
                })
    }

    private fun getAssignments(data: PolylearnData) {
        for(cat in data.categories) {
            for(item in cat.items) {
                if(item.type == FileTypes.ASSIGNMENT.name) {
                    getAssignment(item)
                }
            }
        }
    }

    private fun getAssignment(item: PolylearnItem) {
        val request = okhttp3.Request.Builder().url(item.url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "FAILED TO DOWNLOAD ACTIVITY")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val source = response.body()?.string()!!
                        response.body()?.close()

                        val newAssignment = parseAssignmentInfo(item, source)
                        if(newAssignment == null) {
                            return
                        }

                        val model = ViewModelProviders.of(activity).get(PolylearnModel::class.java)
                        val prefs = activity.getPreferences(MODE_PRIVATE).edit()
                        model.writeAssignment(newAssignment, prefs, activity)
                    }
                })
    }

    private fun parseAssignmentInfo(item: PolylearnItem, source: String): PolyAssignment? {
        var pattern = Pattern.compile("Due date<\\/td>\\n<td.+>(.+)<\\/td>")
        var matcher = pattern.matcher(source)
        matcher.find()

        if(matcher.hitEnd()) {
            return null
        }
        val timeString = matcher.group(1)

        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy, hh:mm aa", Locale.US)
        val timestamp = sdf.parse(timeString).time / 1000

        pattern = Pattern.compile("Submission status<\\/td>\\n<td class.+>(.+)<\\/td>")
        matcher = pattern.matcher(source)
        matcher.find()
        val attemptString = matcher.group(1)
        var attempted = true
        if(attemptString == "No attempt") {
            attempted = false
        }

        var newAssignment = PolyAssignment(
                item.title,
                timestamp,
                attempted,
                item.url

                )

        return newAssignment
    }

    fun openActualUrl(url: String, username: String, password: String, finishCallback: () -> Unit) {
        var newUrl = url

        polyLogin(username, password, loginCallback = {
            if(url.contains("polylearn.calpoly.edu")) {
                newUrl += "&redirect=1"
            }

            activity.runOnUiThread(finishCallback)

            val model = ViewModelProviders.of(activity).get(PolylearnModel::class.java)
            model.webViewUrl = newUrl

            activity.supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment, WebViewActivity())
                    .commit()
        })
    }

    fun openPLUrl(url: String, finishCallback: () -> Unit) {
        Log.d("POLYINFO", "OPENING URL $url")
        val request = okhttp3.Request.Builder().url(url).build()

        client.newCall(request)
                .enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("POLYHTTP", "FAILED TO GET POLYLEARN URL")
                        Log.d("POLYHTTP", e.message)
                        activity.runOnUiThread(finishCallback)
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Failed to access resource", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val source = response.body()?.string()!!
                        response.body()?.close()

                        val pattern = Pattern.compile("Click <a href=\"(.+)\" onclick=\"this\\.target=")
                        val matcher = pattern.matcher(source)

                        matcher.find()

                        val spawnUrl: String
                        if(matcher.hitEnd()) {
                            spawnUrl = url
                        }
                        else {
                            spawnUrl = matcher.group(1)
                        }

                        activity.runOnUiThread(finishCallback)

                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(spawnUrl))
                        activity.startActivity(browserIntent)
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

data class PolyAssignmentHolder(var items: ArrayList<PolyAssignment> = arrayListOf())

data class PolyAssignment(
        var name: String,
        var due: Long,
        var submitted: Boolean,
        var url: String
)

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
        val enrollmentStatus: JSONEnrollmentStatus,
        var polylearnUrl: String?,
        var polylearnData: ArrayList<Category>?
): Parcelable

@Parcelize
data class JSONEnrollmentStatus(
        val statusCode: String
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