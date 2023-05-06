package com.ownapp.core.view.web

import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.ownapp.core.R
import com.ownapp.core.binding.BindingActivity
import com.ownapp.core.binding.viewBinding
import com.ownapp.core.databinding.ActivityWebViewBinding
import com.ownapp.core.extensions.*
import com.ownapp.core.extensions.resource.getAttrColor
import com.ownapp.core.extensions.resource.isValidUrl
import com.ownapp.core.extensions.utility.*
import com.ownapp.core.extensions.view.enableRefresh
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Keep
open class WebViewActivity: BindingActivity()
    , SwipeRefreshLayout.OnRefreshListener, OnScrollChangedListener
{
    //**--------------------------------------------------------------------------------------------------
    //*     Variable
    //---------------------------------------------------------------------------------------------------*/
    @Keep
    open class ProgressWebChromeClient(private val progressListener: (Int) -> Unit = {}): WebChromeClient()
    {
        override fun onProgressChanged(view: WebView, newProgress: Int)
        {
            progressListener(newProgress)
            super.onProgressChanged(view, newProgress)
        }
    }

    @Keep
    open class BaseWebChromeClient(
        private val activity: WebViewActivity
        , progressListener: (Int) -> Unit = { activity.updateProgressBar(it) }
    ): ProgressWebChromeClient(progressListener)

    @Keep
    open class BaseWebViewClient(private val activity: WebViewActivity): WebViewClient()
    {
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String)
        {
            description.toast(activity)
            "Error in WebView: $errorCode => $description".logError()
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?)
        {
            super.onPageStarted(view, url, favicon)
            activity.progressBar.isVisible = activity.isProgressEnabled
        }

        override fun onPageFinished(view: WebView, url: String)
        {
            super.onPageFinished(view, url)
            activity.swipeRefreshContainer.isRefreshing = false
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Variable
    //---------------------------------------------------------------------------------------------------*/
    // Constant
    companion object
    {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_PARAMETERS = "extra_parameters"
        const val EXTRA_JAVASCRIPT = "extra_javascript"
    }

    val binding by viewBinding<ActivityWebViewBinding>(R.layout.activity_web_view)

    // View
    val webView: WebView
        get() = binding.webView

    val swipeRefreshContainer: SwipeRefreshLayout
        get() = binding.swipeRefreshContainer

    val toolbar: MaterialToolbar
        get() = binding.toolbar

    val progressBar: ProgressBar
        get() = binding.progressBar

    // Class
    open var headerMap: HashMap<String, Any?>? = null
    open var parameterMap: HashMap<String, Any?>? = null

    // Value
    open var url: String? = null
    open var title: String? = null

    open var isJavascriptEnabled = false
    open var isProgressEnabled = true
    open var isSwipeRefreshEnabled: Boolean = true
        set(isEnabled)
        {
            field = isEnabled
            swipeRefreshContainer.enableRefresh(isEnabled)
        }


    //**--------------------------------------------------------------------------------------------------
    //*     Initialize
    //---------------------------------------------------------------------------------------------------*/
    @Suppress("UNCHECKED_CAST")
    override fun onExtras(bundle: Bundle?)
    {
        bundle?.run {
            if (!containsKey(EXTRA_URL))
                "WebView URL cannot be null".log()

            url = getString(EXTRA_URL, "")
            title = getString(EXTRA_TITLE, "")
            isJavascriptEnabled = getBoolean(EXTRA_JAVASCRIPT, true)

            parameterMap = getSerializable(EXTRA_PARAMETERS) as? HashMap<String, Any?>?
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?)
    {
        super.onPostCreate(savedInstanceState)

        if(url?.isValidUrl == true)
        {
            initView()
            initWebView()
            onInitialize()
            loadUrl()
        }
        else
            toast(R.string.error_invalid_url)
    }

    private fun initView()
    {
        swipeRefreshContainer.apply {
            setColorSchemeColors(getAttrColor(R.attr.colorPrimary))
            setOnRefreshListener(this@WebViewActivity)
        }

        toolbar.apply {
            title = this@WebViewActivity.title.orEmpty()
            setNavigationOnClickListener { onBackPressed() }
        }

        progressBar.isVisible = isProgressEnabled
    }

    private fun initWebView()
    {
        webView.apply {
            webChromeClient = BaseWebChromeClient(this@WebViewActivity)
            webViewClient = BaseWebViewClient(this@WebViewActivity)

            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = true
            //webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            settings.apply {
                setSupportZoom(true)
                setSupportMultipleWindows(true)

                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

                javaScriptEnabled = isJavascriptEnabled
            }
        }
    }

    override fun onInitialize()
    {
        if(!parameterMap.isNullOrEmpty())
        {
            isSwipeRefreshEnabled = false
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Public
    //---------------------------------------------------------------------------------------------------*/
    protected fun loadUrl()
    {
        if (!parameterMap.isNullOrEmpty())
        {
            lifecycleScope.executeAsyncTask(
                onPreExecute = {}
                , doInBackground = {
                    try
                    {
                        if(!URL(url).toString().isValidUrl)
                            throw Exception("Invalid Url")

                        val urlConnection = (URL(url).openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            connectTimeout = 30000

                            setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8")
                            headerMap?.forEach { setRequestProperty(it.key, it.value.toString()) }

                            doOutput = true
                        }

                        try
                        {
                            urlConnection.outputStream?.let {
                                BufferedWriter(OutputStreamWriter(it, Charsets.UTF_8)).run {
                                    write(parameterMap.toEncodedParameters())
                                    close()
                                }
                                it.close()
                            }
                        }
                        catch(e: Exception)
                        {
                            e.logException()
                            lifecycleScope.launch { toast(R.string.error_connection_timeout) }
                        }

                        if(urlConnection.responseCode != HttpsURLConnection.HTTP_OK)
                        {
                            urlConnection.disconnect()
                            throw Exception("Http response => ${urlConnection.responseCode} ${urlConnection.responseMessage}")
                        }

                        try
                        {
                            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                            val stringBuilder = StringBuilder()
                            var line: String?
                            while(bufferedReader.readLine().also { line = it } != null)
                            {
                                stringBuilder.append(line).append("\n")
                            }
                            bufferedReader.close()
                            stringBuilder.toString()
                        }
                        catch(e: FileNotFoundException)
                        {
                            e.logException()
                            null
                        }
                        finally
                        {
                            urlConnection.disconnect()
                        }
                    }
                    catch(e: Exception)
                    {
                        e.logException()
                        null
                    }
                }, onPostExecute = {
                    webView.loadDataWithBaseURL(url, it.orEmpty(), "text/html", Charsets.UTF_8.toString(), null)
                }
            )
        }
        else
            webView.loadUrl(url.orEmpty())
    }

    fun updateProgressBar(newProgress: Int)
    {
        progressBar.apply {
            progress = newProgress
            isVisible = progress < 100
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Override
    //---------------------------------------------------------------------------------------------------*/
    override fun onStart()
    {
        super.onStart()
        swipeRefreshContainer.viewTreeObserver?.addOnScrollChangedListener(this)
    }

    override fun onStop()
    {
        super.onStop()
        swipeRefreshContainer.viewTreeObserver?.removeOnScrollChangedListener(this)
    }

    override fun onBackPressed()
    {
        if (webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Implement
    //---------------------------------------------------------------------------------------------------*/
    override fun onRefresh()
    {
        webView.loadUrl(webView.url.orEmpty())
    }

    override fun onScrollChanged()
    {
        swipeRefreshContainer.isEnabled = webView.scrollY == 0
    }
}