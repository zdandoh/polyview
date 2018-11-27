package zandoh.com.polyview

import android.arch.lifecycle.ViewModel
import android.content.SharedPreferences
import android.os.Parcel
import com.google.gson.Gson

class PolylearnModel: ViewModel() {
    var classes: JSONClasses? = null
    var plDisplayClass = -1

    fun writeClasses(newClasses: JSONClasses, prefs: SharedPreferences.Editor) {
        this.classes = newClasses

        val classesStr = Gson().toJson(newClasses)
        prefs.putString("classes", classesStr)
        prefs.apply()
    }

    fun loadClasses(prefs: SharedPreferences) {
        val classesJson = prefs.getString("classes", null)

        if(classesJson != null) {
            this.classes = Gson().fromJson(classesJson, JSONClasses::class.java)
        }
    }
}