package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_classes.*

class ClassesActivity : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_classes, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model = ViewModelProviders.of(activity!!).get(PolylearnModel::class.java)

        classes_list.layoutManager = LinearLayoutManager(activity)
        classes_list.adapter = ClassesAdapter(model.classes!!.items)
    }

    class ClassesAdapter(private val classes: List<JSONClass>): RecyclerView.Adapter<ClassesAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val itemView = LayoutInflater.from(p0.context).inflate(R.layout.class_list_item, p0, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Log.d("POLYINFO", classes[position].toString())

            val times = classes[position].times[0]
            holder?.class_name?.text = classes[position].name + (if(classes[position].classType == "LAB") " LAB" else "")
            holder?.class_full_name?.text = "${classes[position].longName} in ${times.building}-${times.room} (${times.buildingName})"
        }

        override fun getItemCount(): Int {
            return classes.size
        }

        class ViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
            var class_name: TextView? = null
            var class_full_name: TextView? = null
            init {
                itemView?.setOnClickListener {
                    val activity = it.context
                    if(activity is MainActivity) {
                        activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment, PolylearnActivity())
                                .commit()
                    }
                }

                class_name = itemView?.findViewById(R.id.class_name)
                class_full_name = itemView?.findViewById(R.id.class_full_name)
            }
        }
    }
}