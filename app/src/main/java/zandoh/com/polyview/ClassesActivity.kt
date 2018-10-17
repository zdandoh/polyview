package zandoh.com.polyview

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Adapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_classes.*
import kotlinx.android.synthetic.main.activity_classes.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*

class ClassesActivity : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_classes, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        classes_list.layoutManager = LinearLayoutManager(activity)
        classes_list.adapter = ClassesAdapter(Arrays.asList(
                PolyClass("CSC 300", "Professional Responsibilities", "Business Building"),
                PolyClass("CSC 349", "Design of Algorithms", "Frank E Pilling"),
                PolyClass("CSC 436", "Mobile Application Development", "Frank E Pilling")
        ))
    }

    class ClassesAdapter(private val classes: List<PolyClass>): RecyclerView.Adapter<ClassesAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val itemView = LayoutInflater.from(p0.context).inflate(R.layout.class_list_item, p0, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder?.class_name?.text = classes[position].short_name
            holder?.class_full_name?.text = classes[position].full_name
            holder?.class_location?.text = classes[position].location
        }

        override fun getItemCount(): Int {
            return classes.size
        }

        class ViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
            var class_name: TextView? = null
            var class_full_name: TextView? = null
            var class_location: TextView? = null
            init {
                class_name = itemView?.findViewById(R.id.class_name)
                class_full_name = itemView?.findViewById(R.id.class_full_name)
                class_location = itemView?.findViewById(R.id.class_building)
            }
        }
    }
}