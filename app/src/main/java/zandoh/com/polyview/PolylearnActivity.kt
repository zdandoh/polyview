package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
import java.lang.Exception
import java.util.*

class PolylearnActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_polylearn, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)

        val classData = model.classes!!.items.get(model.plDisplayClass)
        val polyData = model.polylearnData.items.get(classData.polylearnUrl)
        Log.d("POLYINFO", polyData.toString())

        polylearn_list.layoutManager = LinearLayoutManager(activity)
        polylearn_list.adapter = PolylearnActivity.PolylearnAdapter(polyData!!)

        polylearn_class_name.text = classData.name
    }

    class PolylearnAdapter(private val polyData: PolylearnData): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return when(viewType) {
                1 -> CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.polylearn_category_item, parent, false))
                2 -> ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.polylearn_list_item, parent, false))
                else -> throw(Exception("Bad state"))
            }
        }

        override fun getItemViewType(position: Int): Int {
            if(polyData.get(position) is Category) {
                return 1
            }
            else {
                return 2
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(getItemViewType(position)) {
                1 -> {
                    val item = polyData.get(position) as Category
                    holder as CategoryViewHolder

                    holder.title?.text = item.title
                }
                2 -> {
                    val item = polyData.get(position) as PolylearnItem
                    holder as ViewHolder

                    holder.icon?.setImageResource(R.drawable.pdf_icon)
                    holder.name?.text = item.title

                    if(item.description.length == 0) {
                        holder.description?.visibility = View.GONE
                    }
                    else {
                        holder.description?.text = item.description
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            var count = 0
            for(category in polyData.categories) {
                count += category.items.size
            }

            return count + polyData.categories.size
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

        class CategoryViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
            var title: TextView? = null

            init {
                title = itemView?.findViewById(R.id.category_title)
            }
        }
    }
}