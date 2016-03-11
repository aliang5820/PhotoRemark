package com.example.photoremark.poi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.photoremark.R;

import java.nio.charset.Charset;

/**
 * Created by Edison on 2016/3/3.
 */
public class ToHomeActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initView();
        initData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_save:
                saveSource();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void initData() {
        String content = HtmlToWord.getContent();
        webView.loadDataWithBaseURL(null, content, "text/html", Charset.defaultCharset().toString(), null);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
    }

    //执行JS，获取网页源码
    private void saveSource() {
        webView.loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
    }

    //获取网页源码JS回调
    private final class InJavaScriptLocalObj {

        @JavascriptInterface
        public void showSource(String htmlSource) {
            Log.i("edison", htmlSource);
            HtmlToWord.saveAsWord(htmlSource);
        }
    }
}
