package lock.demo.jiajialockdemoapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.yunkelock.yunsdk.JJLockClient;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView info_text;
    private Button search;
    private Button unbound;
    private Button openLock;
    private ListView device_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        init();

    }

    private void init() {

        JJLockClient.init(getApplication());

        reqPermissions(); //android 6.0以上，一些权限需要动态申请，这一步几乎是必须的

        initEvent();//初始化事件


    }

    private void initEvent() {
        View empty = LayoutInflater.from(this).inflate(R.layout.empty_view,  device_list, false);
        ((ViewGroup)device_list.getParent()).addView(empty);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.mac_text, new String[]{"3","4"});
        device_list.setAdapter(adapter);
        device_list.setEmptyView(empty);
    }

    //动态请求必须的权限
    private void reqPermissions() {

        /**
         * 请求必备权限，此处我省略用户不同意授权情况下的处理，但正式的产品应该做处理
         */
        JJLockClient.getDefault().enableBluetooth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{
                    Manifest.permission_group.LOCATION,
                    Manifest.permission_group.STORAGE
            }, 666);
        }


    }


    private void initView() {
        info_text = (TextView) findViewById(R.id.info_text);
        search = (Button) findViewById(R.id.search);
        unbound = (Button) findViewById(R.id.unbound);
        openLock = (Button) findViewById(R.id.openLock);
        device_list = (ListView) findViewById(R.id.device_list);

        search.setOnClickListener(this);
        unbound.setOnClickListener(this);
        openLock.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:{
                //搜索
                JJLockClient.getDefault().scan(new BleScanCallback() {
                    @Override
                    public void onScanFinished(List<BleDevice> list) {

                    }

                    @Override
                    public void onScanStarted(boolean b) {

                    }

                    @Override
                    public void onScanning(BleDevice bleDevice) {

                    }
                });
                break;

            }
            case R.id.unbound: {
                if (getMacCache().equals("")) {
                    toast("还未绑定设备");
                    return;
                }
                break;

            }
            case R.id.openLock: {
                if (getMacCache().equals("")) {
                    toast("还未绑定设备");
                    return;
                }
                break;

            }

        }
    }

    /***************************************************************************
     ****************** 下面的代码不重要，只是demoAPP的特有逻辑*********************
     *****************************************************************************/

    /**
     * 设置mac的缓存
     *
     * @param mac
     */
    private void setMacCache(String mac) {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        sp.edit().putString("mac", reverseMac(mac)).commit();
    }

    /**
     * 得到mac的缓存
     *
     * @return
     */
    private String getMacCache() {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        return sp.getString("mac", "");
    }


    private String reverseMac(String mac) {
        if (mac == null) {
            return "";
        }
        String[] b = mac.split(":");
        String str = "";
        for (int i = 1; i <= b.length; i++) {
            if (i != 1) {
                str += ":" + b[b.length - i];
            } else {
                str += b[b.length - i];
            }
        }
        return str;
    }


    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
