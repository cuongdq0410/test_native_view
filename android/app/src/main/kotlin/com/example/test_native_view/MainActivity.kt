package com.example.test_native_view

import android.annotation.SuppressLint
import io.flutter.embedding.android.FlutterActivity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class MainActivity : FlutterActivity() {
    private val buttonClickEventChannel = "button_click_event_channel"
    private var buttonClickEventSink: EventChannel.EventSink? = null

    private lateinit var localConstraintLayout: ConstraintLayout


    @SuppressLint("SetJavaScriptEnabled")
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        localConstraintLayout = LayoutInflater.from(context).inflate(
            R.layout.native_view, null
        ) as ConstraintLayout

        EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            buttonClickEventChannel
        ).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(p0: Any?, eventSink: EventChannel.EventSink) {
                    buttonClickEventSink = eventSink
                }

                override fun onCancel(p0: Any) {
                    buttonClickEventSink = null
                }
            },
        )
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory(
                "<platform-view-type>",
                NativeViewFactory(localConstraintLayout)
            )
        val button: Button = localConstraintLayout.findViewById(R.id.btnClick)
        button.setOnClickListener {
            buttonClickEventSink?.success("buttonClick")
        }


        val myWebView: WebView = localConstraintLayout.findViewById(R.id.webview)
        myWebView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }

        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true // Enable JavaScript if needed

        myWebView.loadUrl("http://www.google.com")
    }


}

internal class NativeView(
    context: Context,
    id: Int,
    creationParams: Map<String?, Any?>?,
    private val renderer: ConstraintLayout,
) :
    PlatformView, MethodChannel.MethodCallHandler {
    private var parentView: ConstraintLayout? = renderer

    override fun getView(): View? {
        return parentView
    }

    override fun dispose() {
        parentView?.removeAllViews()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        TODO("Not yet implemented")
    }
}

class NativeViewFactory(private val renderer: ConstraintLayout) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        return NativeView(context, viewId, creationParams, renderer)
    }
}
