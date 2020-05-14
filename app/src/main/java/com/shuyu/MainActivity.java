package com.shuyu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.shuyu.action.web.ActionSelectListener;
import com.shuyu.action.web.CustomActionWebView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    View mLadingView;
    CustomActionWebView mCustomActionWebView;
    MenuItem.OnMenuItemClickListener onMenuItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLadingView = findViewById(R.id.loadingView);
        mCustomActionWebView = (CustomActionWebView) findViewById(R.id.customActionWebView);
        onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                String title = (String) menuItem.getTitle();
                if ("获取文字".equals(title) || "复制".equals(title)) {
                    mCustomActionWebView.getSelectedData(title);
                    mCustomActionWebView.releaseAction();
                }
                return true;
            }
        };

        List<String> list = new ArrayList<>();
        list.add("获取文字");
        list.add("复制");
        list.add("APIWeb");

        mCustomActionWebView.setWebViewClient(new CustomWebViewClient());

        //设置item
        mCustomActionWebView.setActionList(list);

        //链接js注入接口，使能选中返回数据
        mCustomActionWebView.linkJSInterface();

        mCustomActionWebView.getSettings().setBuiltInZoomControls(true);
        mCustomActionWebView.getSettings().setDisplayZoomControls(false);
        //使用javascript
        mCustomActionWebView.getSettings().setJavaScriptEnabled(true);
        mCustomActionWebView.getSettings().setDomStorageEnabled(true);
        mCustomActionWebView.setOnMenuItemClickListener(onMenuItemClickListener);

        //增加点击回调
        mCustomActionWebView.setActionSelectListener(new ActionSelectListener() {
            @Override
            public void onClick(String title, String selectText) {
                if (title.equals("APIWeb")) {
                    Intent intent = new Intent(MainActivity.this, APIWebViewActivity.class);
                    startActivity(intent);
                    return;
                } else if ("复制".equals(title)) {
                    if (!TextUtils.isEmpty(selectText)) {
                        // 得到剪贴板管理器
                        ClipboardManager cmb = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
                        ClipData clipData = ClipData.newPlainText(null, selectText);
                        // 把数据集设置（复制）到剪贴板
                        cmb.setPrimaryClip(clipData);
                        Toast.makeText(MainActivity.this, "复制成功！", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Click Item: " + title + "。\n\nValue: " + selectText, Toast.LENGTH_LONG).show();
                }
            }
        });

        //加载url
        mCustomActionWebView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCustomActionWebView.loadUrl("https://www.jianshu.com/p/18c1f9e534e2");
            }
        }, 1000);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mCustomActionWebView != null) {
            mCustomActionWebView.dismissAction();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private class CustomWebViewClient extends WebViewClient {

        private boolean mLastLoadFailed = false;

        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            if (!mLastLoadFailed) {
                CustomActionWebView customActionWebView = (CustomActionWebView) webView;
                customActionWebView.linkJSInterface();
                mLadingView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            mLastLoadFailed = true;
            mLadingView.setVisibility(View.GONE);
        }
    }
}
