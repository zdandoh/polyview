package zandoh.com.polyview

import android.graphics.Color
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

class ComingUpActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_comingup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        comingup_list.layoutManager = LinearLayoutManager(activity)
        comingup_list.adapter = ComingUpActivity.AssignmentAdapter(Arrays.asList(
                PolyAssignment("Project 3", "CPE 357 | due in 9 hours", false),
                PolyAssignment("Milestone 2", "CSC 436 | due in 2 days", true),
                PolyAssignment("Quiz 3", "CSC 300 | due in 5 days", false)
        ))
    }

    class AssignmentAdapter(private val assignments: List<PolyAssignment>): RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val itemView = LayoutInflater.from(p0.context).inflate(R.layout.comingup_list_item, p0, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val assignment = assignments[position];

            holder?.assignment_name?.text = assignment.assignment_name
            holder?.assignment_details?.text = assignment.assignment_due

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
                        activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment, PolylearnActivity())
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