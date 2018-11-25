package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import android.content.Intent
import android.support.v4.app.Fragment
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.HttpClientStack
import com.android.volley.toolbox.HttpStack
import android.arch.lifecycle.ViewModelProviders;
import android.util.Log
import com.android.volley.RequestQueue
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val model = ViewModelProviders.of(this).get(PolylearnModel::class.java)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, LoginActivity())
                .commit()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
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
            R.id.nav_map -> {

            }
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, newFragment!!)
                .commit()

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
