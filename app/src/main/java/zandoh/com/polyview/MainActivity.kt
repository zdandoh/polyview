package zandoh.com.polyview

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.support.v4.app.Fragment
import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler
import android.support.v4.os.HandlerCompat.postDelayed
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.Manifest.permission
import android.Manifest.permission.READ_PHONE_STATE
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.abc_dialog_title_material.view.*
import kotlinx.android.synthetic.main.activity_classes.*
import kotlinx.android.synthetic.main.activity_comingup.*
import kotlinx.android.synthetic.main.activity_polylearn.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var provider: PolyDataProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        checkPermissions()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val model = ViewModelProviders.of(this).get(PolylearnModel::class.java)
        model.load(getPreferences(Context.MODE_PRIVATE))

        setNameHeader(model)

        model.polylearnData.items.forEach {
            addPLToSidebar(this, model, it.key)
        }

        refresh_button.setOnClickListener {
            if(model.username == null) {
                return@setOnClickListener
            }
            val spinAnim = AnimationUtils.loadAnimation(this, R.anim.spin_anim)
            refresh_button.startAnimation(spinAnim)
            model.loading = true
            getDataProvider().collectData(model.username!!, model.password!!, refreshDataCallback = {
                refresh_button.clearAnimation()

                // refresh recylcerviews
                this.runOnUiThread {
                    polylearn_list?.adapter = PolylearnActivity.PolylearnAdapter(getPolyData(model)!!)
                    classes_list?.adapter = ClassesActivity.ClassesAdapter(model.classes!!.items)
                }
            }, callback = {})
        }

        val launchFrag: Fragment
        if(model.classes == null) {
            launchFrag = LoginActivity()
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        else {
            launchFrag = ClassesActivity()
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, launchFrag)
                .commit()
    }

    fun setNameHeader(model: PolylearnModel) {
        val navHeader = nav_view.getHeaderView(0) as LinearLayout
        val displayNameTextView = navHeader.findViewById<TextView>(R.id.display_name)
        displayNameTextView.text = model.getUsernameAsEmail()
    }

    fun checkPermissions() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    fun getDataProvider(): PolyDataProvider {
        if(this.provider == null) {
            this.provider = PolyDataProvider(this, this)
        }

        return this.provider!!
    }

    fun startDataUpdater() {
        val handler = Handler()
        val check_update_delay = 10000L

        handler.postDelayed(object : Runnable {
            override fun run() {
                val provider = getDataProvider()
                provider.checkForUpdate()
                handler.postDelayed(this, check_update_delay)
            }
        }, check_update_delay)

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var newFragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_login -> {
                newFragment = LoginActivity()
            }
            R.id.nav_classes -> {
                newFragment = ClassesActivity()
            }
            R.id.nav_calendar -> {
                newFragment = CalendarActivity()
            }
            R.id.nav_comingup -> {
                newFragment = ComingUpActivity()
            }
            else -> {
                // This is a dynamically added polylearn
                val model = ViewModelProviders.of(this).get(PolylearnModel::class.java)
                val classItemPosition = model.classes!!.items.indexOfFirst { it.name == item.title }
                model.plDisplayClass = classItemPosition

                newFragment = PolylearnActivity()
            }
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, newFragment)
                .commit()

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
