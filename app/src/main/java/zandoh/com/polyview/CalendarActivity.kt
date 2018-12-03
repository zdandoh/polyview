package zandoh.com.polyview

import android.app.ActionBar
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.system.Os.bind
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.calendar_cell.view.*
import kotlinx.android.synthetic.main.calendar_row.view.*
import kotlinx.android.synthetic.main.content_main.*
import android.widget.LinearLayout
import org.w3c.dom.Text


class CalendarActivity: Fragment() {
    val dayArr = arrayListOf("", "M", "T", "W", "R", "F")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_calendar, container, false)
    }

    fun getCalendarCell(row: Int, col: Int): View {
        val rowView = calendar_table.getChildAt(row)
        return (rowView as TableRow).getChildAt(col)
    }

    fun getCalendarCell(day: String, time: String): View {
        val col = dayArr.indexOf(day)
        val row = timeToInt(time) / 60 + 1

        return getCalendarCell(row, col)
    }

    fun getCalendarCell(day: String, time: Int): View {
        val col = dayArr.indexOf(day)
        val row = time / 60 + 1

        return getCalendarCell(row, col)
    }

    fun getCalendarTextView(day: String, time: Int): TextView {
        val cell = getCalendarCell(day, time)

        if(time % 60 == 30) {
            return cell.lower
        }
        return cell.upper
    }

    fun timeToInt(time: String): Int {
        // Takes a string like "3:10 PM" and converts it to an int representing the minutes since 7 AM
        val timeParts = time.split(":")
        var hour = timeParts[0].toInt()
        var minute = timeParts[1].split(" ")[0].toInt()
        val hasPm = time.lastIndexOf("PM") != -1

        var timeInt = 0
        if(hour == 12 && hasPm) {
            hour -= 12
        }

        if(hasPm) {
            timeInt = 60 * 5
        }
        else {
            hour -= 7
        }

        timeInt += hour * 60

        if(minute == 10 || minute == 40) {
            minute -= 10
        }
        timeInt += minute

        return timeInt
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)

        for(i in 1..16) {
            var hour = i + 6
            var text = "AM"
            if(hour > 12) {
                text = "PM"
                hour -= 12
            }
            else if(hour == 12) {
                text = "PM"
            }

            val labelCell = getCalendarCell(i, 0) as TextView
            labelCell.setText("$hour $text")
        }

        // Populate calendar
        for((index, classItem) in model.classes!!.items.withIndex()) {
            val times = classItem.times[0]
            // MTWRF
            for(dayNo in 1..5) {
                var labeled = 0
                if(!times.days.contains(dayArr[dayNo])) {
                    // This class does not occur on this day
                    continue
                }

                val day = dayArr[dayNo]
                var currTime = timeToInt(times.startTime)
                val endTime = timeToInt(times.endTime)

                while(currTime < endTime) {
                    val cellView = getCalendarTextView(day, currTime)

                    cellView.setOnClickListener {
                        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)
                        model.plDisplayClass = index

                        fragmentManager?.beginTransaction()
                                ?.replace(R.id.fragment, PolylearnActivity())
                                ?.commit()
                    }

                    cellView.setBackgroundColor(0x88008000.toInt())
                    if(labeled == 0) {
                        cellView.setText(" " + classItem.name.split("-")[0])
                    }
                    if(labeled == 1 && classItem.classType == "LAB") {
                        cellView.setText(" LAB")
                    }
                    labeled++

                    val drawable = cellView.compoundDrawables.get(3)
                    if(currTime + 30 < endTime) {
                        drawable?.setColorFilter(resources.getColor(R.color.calendarColor), PorterDuff.Mode.CLEAR)
                    }

                    if(currTime + 30 == endTime && drawable == null) {
                        cellView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.line_vertical, 0, 0, R.drawable.line_horizontal)
                    }

                    currTime += 30
                }
            }
        }
    }
}