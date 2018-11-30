package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProviders
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
import kotlinx.android.synthetic.main.activity_polylearn.*
import java.lang.Exception
import android.content.Intent
import android.net.Uri
import android.widget.ProgressBar
import android.widget.RelativeLayout
import java.io.File


enum class FileTypes {
    FOLDER,
    URL,
    FORUM,
    FILE,
    ASSIGNMENT,
    QUIZ,
}

fun getPolyData(model: PolylearnModel): PolylearnData {
    val classData = model.classes!!.items.get(model.plDisplayClass)
    val polyData = model.polylearnData.items.get(classData.polylearnUrl)

    return polyData!!
}

class PolylearnActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_polylearn, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)

        val classData = model.classes!!.items.get(model.plDisplayClass)
        val polyData = getPolyData(model)
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

                    Log.d("POLYINFO", item.type)
                    Log.d("POLYINFO", FileTypes.values().toString())

                    when(item.type) {
                        FileTypes.URL.name -> holder.icon?.setImageResource(R.drawable.link_icon)
                        FileTypes.FOLDER.name -> holder.icon?.setImageResource(R.drawable.folder_icon)
                        FileTypes.FILE.name -> holder.icon?.setImageResource(R.drawable.file_icon)
                        FileTypes.FORUM.name -> holder.icon?.setImageResource(R.drawable.forum_icon)
                        FileTypes.ASSIGNMENT.name -> holder.icon?.setImageResource(R.drawable.assignment_icon)
                        FileTypes.QUIZ.name -> holder.icon?.setImageResource(R.drawable.quiz_icon)
                        else -> {
                            holder.icon?.setImageResource(R.drawable.unknown_icon)
                        }
                    }
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
            var progressBar: RelativeLayout? = null

            init {
                itemView?.setOnClickListener {
                    val model = ViewModelProviders.of(it.context as MainActivity).get(PolylearnModel::class.java)

                    val polyData = getPolyData(model)
                    val item = polyData.get(adapterPosition) as PolylearnItem

                    val activity = itemView.context as MainActivity

                    if(item.type == FileTypes.FILE.name || item.type == FileTypes.URL.name) {
                        progressBar!!.visibility = View.VISIBLE
                        icon!!.visibility = View.INVISIBLE

                        activity.getDataProvider().openActualUrl(item.url, model.username!!, model.password!!) {
                            progressBar!!.visibility = View.INVISIBLE
                            icon!!.visibility = View.VISIBLE
                        }
                    }
                    else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                        activity.startActivity(browserIntent)
                    }
                }

                icon = itemView?.findViewById(R.id.poly_item_icon)
                name = itemView?.findViewById(R.id.poly_item_name)
                description = itemView?.findViewById(R.id.poly_item_description)
                progressBar = itemView?.findViewById(R.id.progressBar)
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