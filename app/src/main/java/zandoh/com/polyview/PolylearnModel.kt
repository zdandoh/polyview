package zandoh.com.polyview

import android.arch.lifecycle.ViewModel
import android.content.SharedPreferences
import android.os.Parcel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_comingup.*
import java.net.URL

class PolylearnModel: ViewModel() {
    var classes: JSONClasses? = null
    var polylearnData: PolylearnDataHolder = PolylearnDataHolder()
    var assignments: PolyAssignmentHolder = PolyAssignmentHolder()
    var tempAssignments: ArrayList<PolyAssignment> = arrayListOf()
    var plDisplayClass = -1
    var username: String? = null
    var password: String? = null
    var loading: Boolean = false
    var webViewUrl: String? = null

    fun resetData() {
        classes = null
        polylearnData = PolylearnDataHolder()
        assignments = PolyAssignmentHolder()
        tempAssignments = arrayListOf()
    }

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

        this.username = prefs.getString("username", null)
        this.password = prefs.getString("password", null)

        val assignmentsJson = prefs.getString("assignments", null)
        if(assignmentsJson != null) {
            this.assignments = Gson().fromJson(assignmentsJson, PolyAssignmentHolder::class.java)
        }
    }

    fun writePolylearnData(url: String, data: PolylearnData, prefs: SharedPreferences.Editor) {
        polylearnData.items.put(url, data)

        val dataStr = Gson().toJson(polylearnData)
        prefs.putString("polylearn_data", dataStr)
        prefs.apply()
    }

    fun writeAssignment(assignment: PolyAssignment, prefs: SharedPreferences.Editor, activity: MainActivity) {
        assignments.items.add(assignment)
        assignments.items.sortBy { it.due }

        val dataStr = Gson().toJson(assignments)
        prefs.putString("assignments", dataStr)
        prefs.apply()

        if(activity.comingup_list != null) {
            activity.runOnUiThread {
                activity.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.fragment, ComingUpActivity())
                        ?.commit()
            }
        }
    }

    fun getUsernameAsEmail(): String {
        if(username == null) {
            return "unknown@calpoly.edu"
        }

        if(username!!.endsWith("@calpoly.edu")) {
            return username!!
        }

        return username + "@calpoly.edu"
    }
}