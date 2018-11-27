package zandoh.com.polyview

import android.arch.lifecycle.ViewModel
import android.content.SharedPreferences
import android.os.Parcel
import com.google.gson.Gson
import java.net.URL

class PolylearnModel: ViewModel() {
    var classes: JSONClasses? = null
    var polylearnData: PolylearnDataHolder = PolylearnDataHolder()
    var plDisplayClass = -1

    fun writeClasses(newClasses: JSONClasses, prefs: SharedPreferences.Editor) {
        this.classes = newClasses

        val classesStr = Gson().toJson(newClasses)
        prefs.putString("classes", classesStr)
        prefs.apply()
    }

    fun load(prefs: SharedPreferences) {
        val classesJson = prefs.getString("classes", null)

        if(classesJson != null) {
            this.classes = Gson().fromJson(classesJson, JSONClasses::class.java)
        }

        val plData = prefs.getString("polylearn_data", null)
        if(plData != null) {
            this.polylearnData = Gson().fromJson(plData, PolylearnDataHolder::class.java)
        }
    }

    fun writePolylearnData(url: String, data: PolylearnData, prefs: SharedPreferences.Editor) {
        polylearnData.items.put(url, data)

        val dataStr = Gson().toJson(polylearnData)
        prefs.putString("polylearn_data", dataStr)
        prefs.apply()
    }
}