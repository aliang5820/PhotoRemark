package com.example.photoremark.poi;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.photoremark.FileUtil;
import com.example.photoremark.R;
import com.example.photoremark.SystemInfo;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Edison on 2016/4/8.
 */
public class VisitActivity extends FragmentActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ProgressDialog processDialog;
    private Context mContext;
    private SystemInfo systemInfo;
    private WebView webView;
    private ListView listView;
    private ArrayAdapter adapter;
    private List<String> list = new ArrayList<>();
    private int extra;
    private boolean isEdit = false;
    private String editFileName = "";
    private static final int LOAD_FINISH = 0;
    private static final int LOAD_FINISH_EMPTY = 1;
    private static final int SAVE_FINISH = 2;
    private static final int EDIT_FINISH = 3;
    public static final String EXTRA = "extra_key";
    public static final int TO_HOME = 0;
    public static final int TO_VILLAGE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (isEdit) {
            getMenuInflater().inflate(R.menu.save_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.create_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                showProgressDialog();
                saveValues();
                break;
            case R.id.menu_create:
                isEdit = true;
                switch (extra) {
                    case TO_HOME:
                        webView.loadUrl("file:///android_asset/to_home.htm");
                        break;
                    case TO_VILLAGE:
                        webView.loadUrl("file:///android_asset/to_village.htm");
                        break;
                }
                listView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_web);
        systemInfo = (SystemInfo) getApplication();
        initView();
        initData();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        webView = (WebView) findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "stub");

        adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        extra = intent.getIntExtra(EXTRA, TO_HOME);

        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mContext, "SD卡不可用", Toast.LENGTH_SHORT).show();
        } else {
            showProgressDialog();
            getAllFiles();
        }
    }

    /**
     * 获取所有文件
     */
    private void getAllFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String startStr = "";
                switch (extra) {
                    case TO_HOME:
                        //获取所有到户的文件
                        startStr = "home";
                        break;
                    case TO_VILLAGE:
                        //获取所有到村的文件
                        startStr = "village";
                        break;
                }
                File filePath = new File(systemInfo.PATH_HTML_VALUE);
                File[] files = filePath.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith(startStr)) {
                            list.add(file.getName());
                        }
                    }
                    if (list.isEmpty()) {
                        Message.obtain(handler, LOAD_FINISH_EMPTY).sendToTarget();
                    } else {
                        Message.obtain(handler, LOAD_FINISH).sendToTarget();
                    }
                } else {
                    Message.obtain(handler, LOAD_FINISH_EMPTY).sendToTarget();
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_FINISH:
                    processDialog.dismiss();
                    //加载文件完成
                    adapter.notifyDataSetChanged();
                    break;
                case LOAD_FINISH_EMPTY:
                    processDialog.dismiss();
                    //空数据
                    Toast.makeText(mContext, "没有对应的文件", Toast.LENGTH_SHORT).show();
                    break;
                case SAVE_FINISH:
                    processDialog.dismiss();
                    isEdit = false;
                    //新建完成
                    list.add(0, msg.obj.toString());
                    adapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    break;
                case EDIT_FINISH:
                    processDialog.dismiss();
                    //编辑完成
                    isEdit = false;
                    listView.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //进入查看或者编辑
        isEdit = true;
        //重新设置menu菜单
        if (Build.VERSION.SDK_INT >= 11) {
            invalidateOptionsMenu();
        }
        //保存被选中的值
        String fileName = list.get(position);
        editFileName = fileName;
        String values = FileUtil.readFile(systemInfo.PATH_HTML_VALUE + fileName).toString();
        String htmlSource = "";
        switch (extra) {
            case TO_HOME:
                //获取到户的html源文件
                htmlSource = FileUtil.getStringFromAssets(mContext, "to_home.htm");
                break;
            case TO_VILLAGE:
                //获取到村的html源文件
                htmlSource = FileUtil.getStringFromAssets(mContext, "to_village.htm");
                break;
        }
        if (!TextUtils.isEmpty(values)) {
            String[] arrays = values.split("\\$_\\$");
            String inputArrays = arrays[0];
            if(arrays.length == 2) {
                String checkBoxArrays = arrays[1];
                //选择框
                String[] checkValuesArray = checkBoxArrays.split(",");
                for (String va : checkValuesArray) {
                    htmlSource = htmlSource.replace("value=\"" + va + "\"", "value=\"" + va + "\" checked");
                }
            }
            //输入框
            String[] inputValuesArray = inputArrays.split(",");
            for (int i = 0; i < inputValuesArray.length; i++) {
                String va = inputValuesArray[i];
                htmlSource = htmlSource.replaceAll("id=\"" + (i + 1) + "\"", "id=\"" + (i + 1) + "\" value=\"" + va + "\"");
            }
        }
        //显示到webView上
        listView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadDataWithBaseURL(null, htmlSource, "text/html", Charset.defaultCharset().toString(), null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isEdit) {
            listView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            isEdit = false;
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
        new AlertDialog.Builder(mContext)
                .setTitle("是否删除该文件")
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //删除该文件
                        String fileName = list.get(position);
                        FileUtil.deleteFile(systemInfo.PATH_HTML_VALUE + fileName);
                        FileUtil.deleteFile(systemInfo.PATH_WORD_DOC + fileName);
                        list.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
        return true;
    }

    //执行JS，获取表单选中的对象值
    private void saveValues() {
        webView.loadUrl("javascript:getHtmlValues()");
    }

    //获取网页源码JS回调
    private final class InJavaScriptLocalObj {

        @JavascriptInterface
        public void callBackAndroid(final String htmlValues) {

            try {
                Log.e("edison", Thread.currentThread().getName() + ":" + htmlValues);
                //htmlSource = htmlSource.replace("value=\"2\"", "value=\"2\" checked");
                String htmlSource;//html源文件
                String fileName = "";
                /**
                 * 保存htm的value值
                 */
                if (isEdit && !TextUtils.isEmpty(editFileName)) {
                    FileUtil.writeFile(systemInfo.PATH_HTML_VALUE + editFileName, htmlValues);
                } else {
                    Date date = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd[HH-mm-ss]");
                    if (extra == TO_HOME) {
                        fileName = "home-" + dateFormat.format(date) + ".doc";
                    } else {
                        fileName = "village-" + dateFormat.format(date) + ".doc";
                    }
                    FileUtil.writeFile(systemInfo.PATH_HTML_VALUE + fileName, htmlValues);
                }

                /**
                 * 获取html源文件
                 */
                if (extra == TO_HOME) {
                    //获取到户的html源文件
                    htmlSource = FileUtil.getStringFromAssets(mContext, "to_home.htm");
                } else {
                    //获取到村的html源文件
                    htmlSource = FileUtil.getStringFromAssets(mContext, "to_village.htm");
                }

                /**
                 * 保存word文档
                 */
                if (!TextUtils.isEmpty(htmlValues)) {
                    String[] arrays = htmlValues.split("\\$_\\$");

                    String inputArrays = arrays[0];
                    if(arrays.length == 2) {
                        String checkBoxArrays = arrays[1];
                        //选择框
                        String[] checkValuesArray = checkBoxArrays.split(",");
                        for (String va : checkValuesArray) {
                            htmlSource = htmlSource.replace("value=\"" + va + "\"", "value=\"" + va + "\" checked");
                        }
                    }
                    //输入框
                    String[] inputValuesArray = inputArrays.split(",");
                    for (int i = 0; i < inputValuesArray.length; i++) {
                        String va = "<u>&nbsp;&nbsp;" + inputValuesArray[i] + "&nbsp;&nbsp;</u>";
                        htmlSource = htmlSource.replaceAll("<input type=\"text\" id=\"" + (i + 1) + "\"/>", va);
                        if (htmlSource.contains("\" class=\"w250\"/>")) {
                            htmlSource = htmlSource.replaceAll("<input type=\"text\" id=\"" + (i + 1) + "\" class=\"w250\"/>", va);
                        }
                        if (htmlSource.contains("\" class=\"w200\"/>")) {
                            htmlSource = htmlSource.replaceAll("<input type=\"text\" id=\"" + (i + 1) + "\" class=\"w200\"/>", va);
                        }
                        if (htmlSource.contains("\" class=\"w50\"/>")) {
                            htmlSource = htmlSource.replaceAll("<input type=\"text\" id=\"" + (i + 1) + "\" class=\"w50\"/>", va);
                        }
                    }
                }
                if (isEdit && !TextUtils.isEmpty(editFileName)) {
                    FileUtil.deleteFile(systemInfo.PATH_WORD_DOC + editFileName);
                    PoiUtil.saveAsWord(systemInfo.PATH_WORD_DOC + editFileName, htmlSource);
                } else {
                    PoiUtil.saveAsWord(systemInfo.PATH_WORD_DOC + fileName, htmlSource);
                }

                //操作完毕，通知页面
                if (isEdit && !TextUtils.isEmpty(editFileName)) {
                    Message.obtain(handler, EDIT_FINISH).sendToTarget();
                } else {
                    Message.obtain(handler, SAVE_FINISH, fileName).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showProgressDialog() {
        if (processDialog == null) {
            processDialog = new ProgressDialog(mContext);
            processDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // processDialog.setTitle("");
            processDialog.setMessage("正在处理...请稍后");
            // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
            processDialog.setIndeterminate(false);
            // 设置ProgressDialog 是否可以按退回键取消
            processDialog.setCancelable(false);
        }
        processDialog.show();
    }
}
