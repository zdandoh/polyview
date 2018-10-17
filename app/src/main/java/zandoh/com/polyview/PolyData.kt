package zandoh.com.polyview;

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.net.CookieHandler
import java.net.CookieManager

enum class Urls(val url: String) {
    LOGIN1("https://idp.calpoly.edu/idp/profile/cas/login?service=https://myportal.calpoly.edu/Loginhttps://idp.calpoly.edu/idp/profile/cas/login?service=https://myportal.calpoly.edu/Login"),
    LOGIN2("")
}

class PolyData {
    fun polyLogin(email: String, password: String, queue: RequestQueue) {
        val cookieManager = CookieManager()
        CookieHandler.setDefault(cookieManager)

//        val req = StringRequest(Request.Method.GET, Urls.LOGIN1,
//            Response.Listener<String> {
//        })
//
//        queue.add(req)
    }
}

data class PolyClass(val short_name: String, val full_name: String, val location: String)