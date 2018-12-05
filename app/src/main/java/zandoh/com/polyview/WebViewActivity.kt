package zandoh.com.polyview

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_webview.*
import okhttp3.HttpUrl
import android.widget.Toast
import android.support.v4.content.ContextCompat.getSystemService
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.webkit.*


class WebViewActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_webview, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model = ViewModelProviders.of(activity as MainActivity).get(PolylearnModel::class.java)

        val provider = (activity as MainActivity).getDataProvider()
        val cookies = provider.client.cookieJar().loadForRequest(HttpUrl.get(model.webViewUrl!!))

        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        for(cookie in cookies) {
            manager.setCookie(cookie.domain(), cookie.toString())
        }

        browser_view.webViewClient = WebViewClient()
        browser_view.webChromeClient = WebChromeClient()
        browser_view.settings.javaScriptEnabled = true

        browser_view.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))

            request.addRequestHeader("Cookie", manager.getCookie(url))

            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)

            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setTitle(fileName)
            request.setMimeType(mimetype)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            val dm = getSystemService(activity as MainActivity, DownloadManager::class.java)
            dm!!.enqueue(request)
            Toast.makeText(context, "Downloading $fileName", Toast.LENGTH_LONG).show()
        }

        browser_view.loadUrl(model.webViewUrl)
    }
}