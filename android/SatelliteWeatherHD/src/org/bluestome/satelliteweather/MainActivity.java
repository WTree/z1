
package org.bluestome.satelliteweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bluestome.android.utils.DateUtils;
import com.bluestome.android.utils.FileUtils;

import org.bluestome.satelliteweather.biz.SatelliteWeatherSimpleBiz;
import org.bluestome.satelliteweather.common.Constants;
import org.bluestome.satelliteweather.services.UpdateService;
import org.bluestome.satelliteweather.utils.HttpClientUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements OnClickListener {

    static String TAG = MainActivity.class.getSimpleName();
    TextView showLog = null;
    Button btnStart = null;
    Button btnPlay = null;
    Button btnClearConsole = null;
    Button btnStop = null;
    Button btnForward = null;
    ScrollView scrollView = null;
    LinearLayout mLayout;
    LinearLayout mLayout2;
    LinearLayout mLayout3;
    ImageView imgView = null;
    List<String> mList = null;
    Spinner spinner = null;
    ArrayAdapter<String> adapter = null;
    String date = null;
    SatelliteWeatherSimpleBiz biz = null;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (null != msg) {
                switch (msg.what) {
                    case 0x0101:
                        String url = (String) msg.obj;
                        String s2 = showLog.getText().toString();
                        showLog.setText(s2 + "\r\n" + url);
                        break;
                    case 0x0102:
                        String ex = (String) msg.obj;
                        String old = showLog.getText().toString();
                        showLog.setText(old + "\r\n" + ex);
                        break;
                    case 0x0104:
                        if (!btnPlay.isEnabled()) {
                            btnPlay.setEnabled(true);
                        }
                        break;
                    case 0x0105:
                        Drawable drawable = (Drawable) msg.obj;
                        // BitmapDrawable bd = (BitmapDrawable) drawable;
                        // Bitmap bm = bd.getBitmap();
                        // imgView.setImageBitmap(bm);
                        imgView.setImageDrawable(drawable);
                        break;
                    case 0x0106:
                        String t = (String) msg.obj;
                        Toast.makeText(getContext(), t, Toast.LENGTH_SHORT).show();
                        break;
                    case 0x0107:
                        break;
                    case 0x0200:
                        // 将下拉空间设置为可见
                        mLayout3.setVisibility(View.VISIBLE);
                        if (!btnPlay.isEnabled()) {
                            btnPlay.setEnabled(true);
                        }
                        if (!btnStart.isEnabled()) {
                            btnStart.setEnabled(true);
                        }
                        break;
                    case 0x0201:
                        // 将图片空间设置为可见
                        if (imgView.getVisibility() == View.INVISIBLE
                                || imgView.getVisibility() == View.GONE) {
                            imgView.setVisibility(View.VISIBLE);
                        }
                        play();
                        break;
                    case 0x0202:
                        init();
                        adapter.notifyDataSetChanged();
                        break;
                }
                if (showLog.getText().toString().length() > 0) {
                    btnClearConsole.setEnabled(true);
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            // 横屏
                            int off = mLayout.getMeasuredHeight()
                                    - scrollView.getHeight();
                            if (off > 0) {
                                scrollView.scrollTo(0, off);
                            }
                        } else if (getContext().getResources()
                                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            // 竖屏
                            int off = mLayout.getMeasuredHeight()
                                    - scrollView.getHeight();
                            if (off > 0) {
                                scrollView.scrollTo(0, off);
                            }
                        }
                    }
                });
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horizontal);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initHUI();
        init();
        if (null == biz) {
            biz = new SatelliteWeatherSimpleBiz(null);
        }
        startService(new Intent(MainActivity.this, UpdateService.class));
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 当前为横屏， 在此处添加额外的处理代码
        setContentView(R.layout.horizontal);
        initHUI();
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO 保存当前活动中的参数
        super.onSaveInstanceState(outState);
    }

    /**
     * 横屏初始化UI
     */
    private void initHUI() {
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setVisibility(View.VISIBLE);

        mLayout = (LinearLayout) findViewById(R.id.linearlayout);

        mLayout3 = (LinearLayout) findViewById(R.id.linearlayout_v1);

        showLog = (TextView) findViewById(R.id.text_show_log);
        showLog.setText("");

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        btnPlay = (Button) findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);

        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(this);
        btnStop.setEnabled(false);

        btnForward = (Button) findViewById(R.id.btn_forward);
        btnForward.setOnClickListener(this);
        btnForward.setEnabled(false);

        btnClearConsole = (Button) findViewById(R.id.btn_clear_console);
        btnClearConsole.setOnClickListener(this);
        btnClearConsole.setEnabled(false);

        imgView = (ImageView) findViewById(R.id.imageView1);
        imgView.setVisibility(View.GONE);

        spinner = (Spinner) findViewById(R.id.spin_date);
        spinner.setVisibility(View.VISIBLE);

    }

    /**
     * 初始化文件目录
     */
    private void init() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            // 创建目录
            File path = new File(Constants.APP_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            // 创建图片目录
            path = new File(Constants.SATELINE_CLOUD_IMAGE_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            if (path.isDirectory()) {
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (File f : path.listFiles()) {
                    // TODO 如果文件名不是日期格式，则跳过
                    if (dataCheck(f.getName())) {
                        adapter.add(f.getName());
                    }
                }
                if (adapter.getCount() > 0) {
                    adapter.sort(new Comparator<String>() {
                        @Override
                        public int compare(String lhs, String rhs) {
                            if (lhs.compareTo(rhs) > 0) {
                                return -1;
                            } else if (lhs.compareTo(rhs) < 0) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    spinner.setEnabled(true);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                View view, int position, long id) {
                            String value = (String) parent.getAdapter()
                                    .getItem(position);
                            date = value;
                            if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                // 横屏
                            } else if (getContext().getResources()
                                    .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                // 竖屏
                                imgView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

            }
        }

    }

    Runnable rParserHtml = new Runnable() {
        @Override
        public void run() {
            mList = null;
            Message msg = null;
            try {
                msg = new Message();
                msg.what = 0x0102;
                msg.obj = "正在比较本地和服务器版本号";
                mHandler.sendMessage(msg);
                final String lastModifyTime = HttpClientUtils
                        .getLastModifiedByUrl(Constants.SATELINE_CLOUD_URL);
                if (null != lastModifyTime
                        && !lastModifyTime.equals(MainApp.i()
                                .getLastModifyTime())) {
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "本地和服务器版本号不一致，执行从服务器下载最新数据";
                    mHandler.sendMessage(msg);
                    long s1 = System.currentTimeMillis();
                    new Thread(new Runnable() {
                        boolean ok = false;

                        @Override
                        public void run() {
                            while (!ok) {
                                try {
                                    Message msg = new Message();
                                    msg.what = 0x0102;
                                    msg.obj = "执行下载请求";
                                    mHandler.sendMessage(msg);
                                    mList = biz.catalog(lastModifyTime);
                                    ok = true;
                                } catch (Exception e) {
                                    Message msg = new Message();
                                    msg.what = 0x0102;
                                    msg.obj = e.getMessage();
                                    mHandler.sendMessage(msg);
                                    ok = false;
                                }
                            }
                        }
                    }).start();
                    int i = 1;
                    long ss = System.currentTimeMillis();
                    do {
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "正在连接到服务器...500ms " + i;
                        mHandler.sendMessage(msg);
                        i++;
                        SystemClock.sleep(500L);
                    } while (mList == null
                            && (System.currentTimeMillis() <= (ss + 5000L)));
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "从网页解析耗时:" + (System.currentTimeMillis() - s1)
                            + " ms";
                    mHandler.sendMessage(msg);
                    if (null != mList && mList.size() > 0) {
                        MainApp.i().setLastModifyTime(lastModifyTime);
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "从站点获取图片地址成功，数量为:" + mList.size();
                        mHandler.sendMessage(msg);
                    } else {
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "从站点获取图片地址失败，数量为:" + mList.size();
                        mHandler.sendMessage(msg);
                    }
                } else {
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "当前数据已经是最新数据不需要再处理\r\n";
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                msg = new Message();
                msg.what = 0x0102;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);
            }
            mHandler.sendEmptyMessageDelayed(0x0200, 1000L);
        }
    };

    private synchronized void play() {
        Message msg = null;
        // 先从本地文件开始入手
        File dir = new File(Constants.SATELINE_CLOUD_IMAGE_PATH
                + File.separator + date);
        File[] files = dir.listFiles();
        FileUtils.sortFilesByFileName(files);
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File s = files[i];
                Drawable drawable = null;
                try {
                    drawable = Drawable.createFromStream(new FileInputStream(
                            new File(s.getAbsolutePath())), "image.png");
                } catch (FileNotFoundException e) {
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = s.getName() + "找不到\r\n";
                    mHandler.sendMessage(msg);
                }
                if (null != drawable) {
                    msg = new Message();
                    msg.what = 0x0105;
                    msg.obj = drawable;
                    mHandler.sendMessage(msg);
                    SystemClock.sleep(250L);
                }
            }
        } else {
            if (null != mList && mList.size() > 0) {
                for (String tmp : mList) {
                    Drawable drawable = loadImageFromUrl(Constants.PREFIX_SATELINE_CLOUD_IMG_URL
                            + tmp);
                    if (null != drawable) {
                        msg = new Message();
                        msg = new Message();
                        msg.what = 0x0105;
                        msg.obj = drawable;
                        mHandler.sendMessage(msg);
                        SystemClock.sleep(500L);
                    }
                }
            } else {
                msg = new Message();
                msg.what = 0x0102;
                msg.obj = "没有可用图片\r\n";
                mHandler.sendMessage(msg);
            }
        }
        mHandler.sendEmptyMessageDelayed(0x0200, 1000L);
    }

    Runnable rDownloadImg = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                play();
            }
        }
    };

    Runnable rPlayImg = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                play();
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (null != v) {
            Message msg = null;
            switch (v.getId()) {
                case R.id.btn_start:
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "正在启动....";
                    mHandler.sendMessage(msg);
                    scrollView.setVisibility(View.VISIBLE);
                    if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mLayout2.setVisibility(View.GONE);
                    } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mLayout3.setVisibility(View.GONE);
                        imgView.setVisibility(View.GONE);
                    }
                    if (null != mList) {
                        mList.clear();
                    }
                    btnPlay.setEnabled(false);
                    showLog.setText("");
                    // new Thread(rParserHtml).start();
                    mHandler.post(rParserHtml);
                    break;
                case R.id.btn_play:
                    if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mLayout2.setVisibility(View.VISIBLE);
                    } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mLayout3.setVisibility(View.GONE);
                    }
                    if (null == date || date.length() == 0 || date.equals("")) {
                        msg = new Message();
                        msg.what = 0x0106;
                        msg.obj = "请选择目录";
                        mHandler.sendMessage(msg);
                        mHandler.sendEmptyMessage(0x0200);
                        imgView.setVisibility(View.GONE);
                        return;
                    }
                    btnStart.setEnabled(false);
                    btnPlay.setEnabled(false);
                    scrollView.setVisibility(View.GONE);
                    imgView.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new Thread(rDownloadImg).start();
                        }
                    }, 500L);
                    break;
                case R.id.btn_clear_console:
                    showLog.setText("");
                    btnClearConsole.setEnabled(false);
                    break;
                case R.id.btn_stop:
                case R.id.btn_forward:
                    MainApp.i().getImageCache().clear();
                    msg = new Message();
                    msg.what = 0x0106;
                    msg.obj = "等待开发完成.....";
                    mHandler.sendMessage(msg);
                    break;
            }
        }
    }

    // 从网络上取数据方法
    protected Drawable loadImageFromUrl(String imageUrl) {
        Drawable drawable = null;
        try {
            if (!MainApp.i().getImageCache().containsKey(imageUrl)) {
                Log.d(TAG, "图片缓存中不存在，从服务器中下载");
                String name = downloadImage(imageUrl);
                if (null != name && name.length() > 0 && !name.equals("")) {
                    drawable = loadDrawableFromLocal(name);
                    if (null != drawable) {
                        MainApp.i().getImageCache().put(imageUrl, name);
                    }
                }
            } else {
                Log.d(TAG, "图片缓存中存在，从本地直接获取");
                String name = MainApp.i().getImageCache().get(imageUrl);
                if (null != name && name.length() > 0 && !name.equals("")) {
                    drawable = loadDrawableFromLocal(name);
                    if (null != drawable) {
                        MainApp.i().getImageCache().put(imageUrl, name);
                    }
                }
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = 0x0102;
            msg.obj = e.getMessage();
            mHandler.sendMessage(msg);
        }
        return drawable;
    }

    private Context getContext() {
        return this;
    }

    /**
     * 下载图片
     * 
     * @param url
     * @return
     */
    private synchronized String downloadImage(String url) {
        URL cURL = null;
        HttpURLConnection connection = null;
        InputStream in = null;
        Message msg = null;
        File file = null;
        try {
            cURL = new URL(url);
            connection = (HttpURLConnection) cURL.openConnection();
            // 获取输出流
            connection.setConnectTimeout(5 * 1000);
            connection.setReadTimeout(15 * 1000);
            connection.connect();
            int code = connection.getResponseCode();
            switch (code) {
                case 200:
                    String name = analysisURL(url);
                    String date = analysisURL2(name);
                    file = new File(Constants.SATELINE_CLOUD_IMAGE_PATH
                            + File.separator + date + File.separator + name);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        if (file.isDirectory()) {
                            msg = new Message();
                            msg.what = 0x0202;
                            msg.obj = file.getName();
                            mHandler.sendMessage(msg);
                        }
                    }
                    if (file.exists()) {
                        msg = new Message();
                        msg.what = 0x0102;
                        msg.obj = "已经存在文件:" + name;
                        mHandler.sendMessage(msg);
                        return name;
                    }
                    in = connection.getInputStream();
                    byte[] buffer = new byte[2 * 1024];
                    OutputStream byteBuffer = new FileOutputStream(file, false);
                    int ch;
                    int length = 0;
                    // 临时变量
                    int tlength = 0;
                    long start = System.currentTimeMillis();
                    while ((ch = in.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, ch);
                        byteBuffer.flush();
                        length += ch;
                        if (tlength != length) {
                            tlength = length;
                            start = System.currentTimeMillis();
                        } else {
                            long end = System.currentTimeMillis() - start;
                            if (end < Constants.TIMEOUT) {
                                msg = new Message();
                                msg.what = 0x0102;
                                msg.obj = "下载[" + name + "]超时!";
                                mHandler.sendMessage(msg);
                                break;
                            }
                        }
                    }
                    byteBuffer.close();
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "下载文件[" + file.getName() + "]成功!";
                    mHandler.sendMessage(msg);
                    return name;
                default:
                    msg = new Message();
                    msg.what = 0x0102;
                    msg.obj = "请求到服务端失败,错误码:" + code;
                    mHandler.sendMessage(msg);
                    break;
            }
        } catch (IOException e) {
            msg = new Message();
            msg.what = 0x0102;
            msg.obj = e.getMessage();
            mHandler.sendMessage(msg);
            e.printStackTrace();
        } catch (Exception e) {
            msg = new Message();
            msg.what = 0x0102;
            msg.obj = e.getMessage();
            mHandler.sendMessage(msg);
            e.printStackTrace();
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * 分析URL,获取图片文件名
     * 
     * @param url
     * @return
     */
    private String analysisURL(String url) {
        int s = url.lastIndexOf("/");
        String name = url.substring(s + 1, url.length());
        if (null == name || name.equals("")) {
            name = String.valueOf(System.currentTimeMillis());
        }
        if (name.indexOf("?") != -1) {
            int pos = name.indexOf("?");
            name = name.substring(0, pos);
        }
        return name;
    }

    /**
     * 从文件名中分析出时间信息
     */
    private static String analysisURL2(String name) {
        String date = DateUtils.formatDate(new Date(),
                DateUtils.DEFAULT_PATTERN);
        if (null != name && name.length() > 0 && !name.equals("")) {
            String[] tmps = name.substring(0, name.lastIndexOf(".")).split("_");
            if (tmps.length > 8) {
                date = tmps[8];
            }
        }
        if (null != date && date.length() > 8 && !date.equals("")) {
            date = date.substring(0, 8);
        }
        return date;
    }

    /**
     * 根据文件名从本地读取图片
     * 
     * @param name
     * @return
     */
    private Drawable loadDrawableFromLocal(String name) {
        Drawable drawable = null;
        FileInputStream fis = null;
        try {
            String date = analysisURL2(name);
            String dir = Constants.SATELINE_CLOUD_IMAGE_PATH + File.separator
                    + date + File.separator + name;
            fis = new FileInputStream(new File(dir));
            drawable = Drawable.createFromStream(fis, "image.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    private boolean dataCheck(String date) {
        String eL = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
        Pattern p = Pattern.compile(eL);
        Matcher m = p.matcher(date);
        boolean flag = m.matches();
        return flag;
    }

}
