package lock.demo.jiajialockdemoapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.yunkelock.yunsdk.JJLockClient;
import com.yunkelock.yunsdk.JJUtil;
import com.yunkelock.yunsdk.beans.InitLockInfoBean;
import com.yunkelock.yunsdk.callback.InitLockCallback;
import com.yunkelock.yunsdk.callback.OpenLockCallback;
import com.yunkelock.yunsdk.callback.ResetLockCallback;
import com.yunkelock.yunsdk.operation.InitLockOp;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView info_text;
    private Button search;
    private Button unbound;
    private Button openLock;
    private ListView device_list;

    MyAdapter adapter = new MyAdapter();


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

        initInfoText();

        initEvent();//初始化事件


    }

    private void initInfoText() {

        info_text.setText("当前连接: " + getMacCache());
    }

    private void initEvent() {

        //设置适配器
        device_list.setAdapter(adapter);


        //监听搜索按钮的回调
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JJLockClient.getDefault().scan(new BleScanCallback() {
                    @Override
                    public void onScanFinished(List<BleDevice> list) {
                        toast("扫描结束");
                    }

                    @Override
                    public void onScanStarted(boolean b) {
                        adapter.clear();
                        toast("开始扫描");
                    }

                    @Override
                    public void onScanning(BleDevice bleDevice) {
                        mlog(bleDevice.getMac());
                        if (JJUtil.isUnboundLock(bleDevice.getScanRecord())){
                            //如果是未绑定的设备则显示
                            adapter.add(bleDevice);
                        }
                    }
                });

            }
        });


        //监听列表的item点击事件
        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                if (!getMacCache().equals("")){
                    //已绑定设备，不予响应
                    toast("已有绑定设备");
                    return;
                }

                new AlertDialog.Builder(MainActivity.this).setMessage("确定要绑定该设备吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取到BleDevice对象
                        final BleDevice bleDevice = (BleDevice) parent.getAdapter().getItem(position);
                        //连接
                        JJLockClient.getDefault().initLock(bleDevice, new InitLockCallback() {
                            @Override
                            public void onInitLockSuccess() {
                                toast("初始化锁成功");
                                //此处为了演示效果，实际上你不需要这么做，因为锁的信息全部被存到服务器了
                                setMacCache(bleDevice.getMac().toUpperCase());
                                initInfoText();
                            }

                            @Override
                            public void onReceivedData(InitLockInfoBean initLockInfoBean, InitLockOp initLockOp) {
                                //上传锁记录，此处省略
                                //因demo需要,此处记录一下code
                                setCode(initLockInfoBean.getAnyCode());
                                setIts(initLockInfoBean.getInitialTimeString());
                                //通知锁绑定成功
                                initLockOp.informInitLockOk();
                            }

                            @Override
                            public void onFail(final int i, String s) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        toast("初始化锁失败,code:" + i);
                                    }
                                });
                            }
                        });
                    }
                }).show();
            }
        });


        //监听开锁按钮点击事件
        openLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMacCache().equals("")){
                    toast("还没有绑定锁,请先去绑定锁");
                    return;
                }

                JJLockClient.getDefault().openLock(getMacCache(), Integer.parseInt(getCode()), new OpenLockCallback() {
                    @Override
                    public void onOpenLockSuccess() {
                        toast("开锁成功");
                        //你也应该在此时上传开锁记录
                    }

                    @Override
                    public void onGetBatteryPercentage(byte b) {
                        mlog("当前锁的电量：" + b);
                    }

                    @Override
                    public void onFail(int i, String s) {
                        toast("开锁失败,code: " + i);
                    }
                });
            }
        });


        //监听解绑的点击事件
        unbound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMacCache().equals("")){
                    toast("还没有绑定锁,请先去绑定锁");
                    return;
                }

                JJLockClient.getDefault().resetLock(getMacCache(), Integer.parseInt(getCode()), getIts(), new ResetLockCallback() {
                    @Override
                    public void onResetLockOk() {
                        toast("解绑锁成功");
                        setMacCache("");
                        initInfoText();
                        //你应该在此时调api接口删除服务器的锁记录
                    }

                    @Override
                    public void onFail(int i, String s) {
                        toast("解绑锁失败,code:" + i);
                    }
                });
            }
        });
    }



    //动态请求必须的权限
    private void reqPermissions() {

        /**
         * 请求必备权限，此处我省略用户不同意授权情况下的处理，但正式的产品应该做处理
         */
        JJLockClient.getDefault().enableBluetooth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, 666);
        }


    }


    private void initView() {
        info_text = (TextView) findViewById(R.id.info_text);
        search = (Button) findViewById(R.id.search);
        unbound = (Button) findViewById(R.id.unbound);
        openLock = (Button) findViewById(R.id.openLock);
        device_list = (ListView) findViewById(R.id.device_list);

    }


    /***************************************************************************
     ****************** 下面的代码不重要，只是demoAPP的特有逻辑*********************
     *****************************************************************************/



    private void mlog(String msg) {
        Log.e("-----", msg);
    }




    private void setCode(String anyCode) {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        sp.edit().putString("code", anyCode).commit();
    }


    private String getCode() {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        return sp.getString("code", "");
    }


    private void setIts(String initialTimeString) {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        sp.edit().putString("its", initialTimeString).commit();
    }


    private String getIts() {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        return sp.getString("its", "");
    }



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

    /**
     * 适配器
     */
    class MyAdapter extends BaseAdapter{
        private List<BleDevice> list = new ArrayList<>();


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            TextView tv = view.findViewById(R.id.mac_text);
            tv.setText(list.get(position).getMac());
            return view;
        }


        public void clear(){
            list.clear();
            notifyDataSetChanged();
        }

        public void add(BleDevice bleDevice) {
            list.add(bleDevice);
            notifyDataSetChanged();
        }
    }
}
