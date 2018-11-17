package zandoh.com.polyview

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_classes.*
import kotlinx.android.synthetic.main.activity_comingup.*
import kotlinx.android.synthetic.main.activity_polylearn.*
import org.w3c.dom.Text
import java.util.*

class PolylearnActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_polylearn, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        polylearn_list.layoutManager = LinearLayoutManager(activity)
        polylearn_list.adapter = PolylearnActivity.PolylearnAdapter(Arrays.asList(
                PolylearnItem("Project 3", "CPE 357 | due in 9 hours", "", ""),
                PolylearnItem("Milestone 2", "CSC 436 | due in 2 days", "This is a hard thing and i like to see what happens when things go wrong but sometimes they go right and that's cool too", ""),
                PolylearnItem("Quiz 3", "CSC 300 | due in 5 days", "", "")
        ))

        polylearn_class_name.text = "CSC 436"
    }

    class PolylearnAdapter(private val items: List<PolylearnItem>): RecyclerView.Adapter<PolylearnAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val itemView = LayoutInflater.from(p0.context).inflate(R.layout.polylearn_list_item, p0, false)
        return ViewHolder(itemView)
    }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position];

            holder?.icon?.setImageResource(R.drawable.pdf_icon)
            holder?.name?.text = item.title

            if(item.description.length == 0) {
                holder?.description?.visibility = View.GONE
            }
            else {
                holder?.description?.text = item.description
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        class ViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
            var icon: ImageView? = null
            var name: TextView? = null
            var description: TextView? = null

            init {
                icon = itemView?.findViewById(R.id.poly_item_icon)
                name = itemView?.findViewById(R.id.poly_item_name)
                description = itemView?.findViewById(R.id.poly_item_description)
            }
        }
    }
}