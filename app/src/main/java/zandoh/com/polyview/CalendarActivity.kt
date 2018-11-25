package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.system.Os.bind
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.content_main.*

class CalendarActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_calendar, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)

        // Populate calendar
//        val dayArr = arrayListOf<String>("", "M", "T", "W", "R", "F")
//        for(classItem in model.classes!!.items) {
//            val times = classItem.times[0]
//            // MTWRF
//            for(dayNo in 1..5) {
//                if(!times.days.contains(dayArr[dayNo])) {
//                    // This class does not occur on this day
//                    continue
//                }
//            }
//        }
//
//        val item = resources.getIdentifier("cal-cell-1-1", "id", context!!.packageName)
//
//        Log.d("POLYINFO", calendar_layout.findViewById<TextView?>(item).toString() + " " + item)




        cal_cell_1_1.setBackgroundColor(0x88008000.toInt())
        cal_cell_1_1.text = "CSC 349"

        cal_cell_1_40.setBackgroundColor(0x88008000.toInt())
//
//        cal_cell_1_1.setOnClickListener {
//            fragmentManager?.beginTransaction()
//                    ?.replace(R.id.fragment, PolylearnActivity())
//                    ?.commit()
//        }
//
//        cal_cell_3_12.setBackgroundColor(0x88008000.toInt())
//        cal_cell_3_12.text = "Recruiting"
//        cal_cell_3_13.setBackgroundColor(0x88008000.toInt())
    }
}