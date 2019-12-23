package lock.demo.jiajialockdemoapp;

import android.Manifest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yunkelock.yunsdk.JJLockClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();

    }

    private void init() {

        JJLockClient.init(getApplication());

        reqPermissions(); //android 6.0以上，一些权限需要动态申请，这一步几乎是必须的

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
            },666);
        }


    }


}
