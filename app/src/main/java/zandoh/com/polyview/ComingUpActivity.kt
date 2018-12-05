package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_classes.*
import kotlinx.android.synthetic.main.activity_comingup.*
import java.util.*
import kotlin.collections.ArrayList

fun timestampToDuration(timestamp: Long): String {
    var secondsLeft = timestamp - System.currentTimeMillis() / 1000

    val days = secondsLeft / (3600 * 24)
    secondsLeft -= days * (3600 * 24)

    val hours = secondsLeft / (3600)
    secondsLeft -= hours * 3600

    val minutes = secondsLeft / 60

    var result = ""
    if(days > 0) {
        result += "$days days "
    }
    if(hours > 0) {
        result += "$hours hours "
    }

    if(result.length > 0) {
        result += "and "
    }
    result += "$minutes minutes"

    return "due in $result"
}

class ComingUpActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_comingup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity!!.title = "Coming Up"

        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)
        model.tempAssignments = model.assignments.items.filter { it.due > System.currentTimeMillis() / 1000 } as ArrayList<PolyAssignment>

        comingup_list.layoutManager = LinearLayoutManager(activity)
        comingup_list.adapter = ComingUpActivity.AssignmentAdapter(model.tempAssignments)
    }

    class AssignmentAdapter(private val assignments: List<PolyAssignment>): RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val itemView = LayoutInflater.from(p0.context).inflate(R.layout.comingup_list_item, p0, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val assignment = assignments[position]

            holder?.assignment_name?.text = assignment.name
            holder?.assignment_details?.text = timestampToDuration(assignment.due)

            val color = if(assignment.submitted) {
                0xB2228B22.toInt()
            }
            else {
                0xB2800000.toInt()
            }

            holder?.assignment_background?.setBackgroundColor(color)
            holder?.assignment_background?.invalidate()
        }

        override fun getItemCount(): Int {
            return assignments.size
        }

        class ViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
            var assignment_name: TextView? = null
            var assignment_details: TextView? = null
            var assignment_background: View? = null
            init {
                itemView?.setOnClickListener {
                    val activity = it.context
                    if(activity is MainActivity) {
                        val model = ViewModelProviders.of(activity).get(PolylearnModel::class.java)

                        if(adapterPosition >= model.tempAssignments.size) {
                            Toast.makeText(activity, "Wait: collecting data", Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                        val assignment = model.tempAssignments[adapterPosition]
                        model.webViewUrl = assignment.url

                        activity.supportFragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.fragment, WebViewActivity())
                                .commit()
                    }
                }

                assignment_name = itemView?.findViewById(R.id.assignment_name)
                assignment_details = itemView?.findViewById(R.id.assignment_details)
                assignment_background = itemView?.findViewById(R.id.assignment_background)
            }
        }
    }
}