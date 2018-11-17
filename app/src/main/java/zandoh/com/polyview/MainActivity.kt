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


val POLY_USERNAME = ""
val POLY_PASSWORD = ""
val DO_DATA_COLLECTION = false

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    /*
    This app needs to login to Polylearn in order to scrape data for display. Currently all data is output to the
    console, you can filter for MYJSON to see it. In order to validate that this portion of the app works,
    valid poly portal credentials need to be entered above in the POLY_USERNAME and POLY_PASSWORD variables
    and the DO_DATA_COLLECTION variable needs to be set to true. I am not sure if the HTML for the poly
    portal is significantly different for a teacher vs a student, but hopefully there will not be issues with that.
    Below is a printout of some objects that the app is able to generate when I run it on my account:
    JSONClasses(items=[JSONClass(name=CSC-300-03-2188, longName=Professional Responsibilities, times=[JSONSchedule(days=TR, startTime=12:10 PM, endTime=01:30 PM, building=002, room=0213)]), JSONClass(name=MU-120-02-2188, longName=Music Appreciation, times=[]), JSONClass(name=CSC-349-07-2188, longName=Design and  Analysis of Algorithms, times=[JSONSchedule(days=TR, startTime=08:10 AM, endTime=09:30 AM, building=014, room=0252)]), JSONClass(name=MU-120-01-2188, longName=Music Appreciation, times=[JSONSchedule(days=MW, startTime=09:10 AM, endTime=10:30 AM, building=045, room=0218)]), JSONClass(name=CSC-349-08-2188, longName=Design and  Analysis of Algorithms, times=[JSONSchedule(days=TR, startTime=09:40 AM, endTime=11:00 AM, building=014, room=0301)]), JSONClass(name=CSC-436-03-2188, longName=Mobile Application Development, times=[JSONSchedule(days=MWF, startTime=02:10 PM, endTime=03:00 PM, building=014, room=0232B)]), JSONClass(name=CSC-436-04-2188, longName=Mobile Application Development, times=[JSONSchedule(days=MWF, startTime=03:10 PM, endTime=04:00 PM, building=014, room=0309)]), JSONClass(name=CSC-300-04-2188, longName=Professional Responsibilities, times=[JSONSchedule(days=TR, startTime=01:40 PM, endTime=03:00 PM, building=014, room=0303)])], term=Term(code=2188))
    https://polylearn.calpoly.edu/AY_2018-2019/course/view.php?id=5055
    [Category(title=General, items=[PolylearnItem(title=Announcements, type=Forum, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/forum/view.php?id=5200)]), Category(title=Introductory Materials, items=[PolylearnItem(title=CSC 436 Syllabus, type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=105977), PolylearnItem(title=Android Developer Site, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=106096), PolylearnItem(title=Kotlin and Android, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=106100), PolylearnItem(title=Kotlin Home Site, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=106103), PolylearnItem(title=Download Android Studio, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=106105), PolylearnItem(title=Setup Android Studio (Lab/CSL only), type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=106116), PolylearnItem(title=Course Intro (slides), type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=120559), PolylearnItem(title=Android (Smyth) book source code, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=134138), PolylearnItem(title=Kotlin (Jemerov) book source code, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=134139)]), Category(title=Intro To Android, items=[PolylearnItem(title=Build Your First Android App In Kotlin, type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=120371), PolylearnItem(title=Intro to Android (slides), type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=120412)]), Category(title=Project - Milestone 1, items=[PolylearnItem(title=Quarter Project - Milestone 1, type=Assignment, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/assign/view.php?id=120352)]), Category(title=Event Handling, items=[PolylearnItem(title=Android Click Events (slides), type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=126001), PolylearnItem(title=Lab 1 Specification, type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=126280)]), Category(title=Intro to Kotlin, items=[PolylearnItem(title=Kotlin Basics, type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=134945)]), Category(title=Input Views and Activities, items=[PolylearnItem(title=Lab 2, type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=137456), PolylearnItem(title=Activities (slides), type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=137611)]), Category(title=Kotlin Functions, items=[PolylearnItem(title=Kotlin - Functions, type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=139353)]), Category(title=Projects - Milestone 2, items=[PolylearnItem(title=Project - Milestone 2, type=Assignment, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/assign/view.php?id=139355)]), Category(title=Spinners and Pickers, items=[PolylearnItem(title=Spinners (Android documentation), type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=139381), PolylearnItem(title=Pickers (Android documentation), type=URL, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/url/view.php?id=139382), PolylearnItem(title=Lab 3, type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=141891)]), Category(title=Kotlin Classes, items=[PolylearnItem(title=Kotlin Classes (slides), type=File, description=, url=https://polylearn.calpoly.edu/AY_2018-2019/mod/resource/view.php?id=143809)]), Category(title=Intent

    The UI is currently populated entirely with dummy data.
     */

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

        // Gathers class data from Poly Portal and prints it to the console
        if(DO_DATA_COLLECTION) {
            val provider = PolyDataProvider(this)
            provider.polyLogin()
        }

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
