package zandoh.com.polyview

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_calendar.*

class CalendarActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_calendar, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cal_cell_1_1.setBackgroundColor(0x88008000.toInt())
        cal_cell_1_1.text = "CSC 349"

        cal_cell_3_12.setBackgroundColor(0x88008000.toInt())
        cal_cell_3_12.text = "Recruiting"
        cal_cell_3_13.setBackgroundColor(0x88008000.toInt())
    }
}