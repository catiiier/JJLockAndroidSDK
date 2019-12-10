# JJLock Android SDK Documentation

---

## author : 李向伟

创建日期：2019年11月19日

最后一次修改日期：

---

## 准备工作（重要）

- 本sdk需要以下权限，否则不能正常工作

```xml
<!--下面7个权限是本SDK正常工作需要的权限-->
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
```

需要注意的是，以上的7个权限除了要在xml中声明以外，除了网络权限之外的6个权限在Android6.0以上的系统均需要动态申请。如果用户拒绝了该权限的申请，则sdk无法正常工作！

## sdk的初始化

```java
JJLockClient.init(getApplication());
```

之后，您便可以使用JJLock Android SDK所提供的所有功能了。

注意，您必须在调用其它api之前调用此方法。

---

之后，sdk也为您提供了更多的自主权，您可以在初始化之后调用config方法（这一步并非必须的）：

```java
JJLockClient.Config config = new JJLockClient.Config() {
    @Override
    public void globalConfig(BleManager bleManagerIns) {
		
    }

    @Override
    public BleScanRuleConfig getScanRuleConfig() {
        return null;
    }
};
JJLockClient.getDefault().config(config);
```

您可以根据您的具体需求进行配置：

```java
JJLockClient.Config config = new JJLockClient.Config() {
    @Override
    public void globalConfig(BleManager bleManagerIns) {
		bleManagerIns
        .enableLog(true)  //默认打开库中的运行日志，如果不喜欢可以关闭
        .setReConnectCount(1, 5000) //设置连接时重连次数和重连间隔（毫秒）
        .setSplitWriteNum(20) //设置分包发送的时候，每一包的数据长度，默认20个字节
        .setConnectOverTime(10000) //设置连接超时时间（毫秒）
        .setOperateTimeout(5000); //配置操作超时
    }

    @Override
    public BleScanRuleConfig getScanRuleConfig() {
    	BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
            .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
            .setDeviceName(true, names)         // 只扫描指定广播名的设备，可选
            .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
            .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
            .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
            .build();
        return scanRuleConfig;
    }
};
```

您实现globalConfig用于全局配置，实现getScanRuleConfig则用于扫描配置

细心的您可能已经发现，globalConfig接口无须返回任何值，您应该直接在给出的bleManagerIns上进行配置；getScanRuleConfig则需要您返回一个BleScanRuleConfig的实例。

请您留意二者的差别。

---

## 判断手机是否支持ble

```java
boolean supportBle =  JJLockClient.getDefault().isSupportBle();
```

## 判断蓝牙是否为打开状态

```java
boolean blueEnable = JJLockClient.getDefault().isBlueEnable();
```

## 请求开启蓝牙

```java
JJLockClient.getDefault().enableBluetooth();
```

## 关闭蓝牙

```java
JJLockClient.getDefault().disableBluetooth();
```

## 扫描周围智能锁

注意，如果您没有在配置里返回自己的BleScanRuleConfig实例，则sdk在扫描智能锁的时候默认过滤名称为"Yunke Lock"。如果您返回了自己的BleScanRuleConfig实例，则需要在构建BleScanRuleConfig实例的时候通过setDeviceName(boolean, string)方法显式指定过滤名称。

做好这些准备工作，您就可以扫描智能锁了。

```java
JJLockClient.getDefault().scan(new BleScanCallback() {
    @Override
    public void onScanFinished(List<BleDevice> scanResultList) {
        // 开始扫描（主线程）
    }

    @Override
    public void onScanStarted(boolean success) {
		// 扫描到一个符合扫描规则的BLE设备（主线程）
    }

    @Override
    public void onScanning(BleDevice bleDevice) {
		// 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
    }
});
```

---

## 扫描智能锁后判断锁是否为绑定状态

一般情况下，您在扫描到设备后只想要显示未绑定的设备，但是或许您也有些时候显示已绑定的设备。所以，sdk为您提供了判断扫描到的设备是否是绑定设备的方法。

```java
@Override
public void onScanning(BleDevice bleDevice) {
    
    if (JJUtil.isUnboundLock(bleDevice.getScanRecord())){
        //如果是未绑定的锁
        //dosomething...
    }
}
```

正如您所看到的，若isUnboundLock方法返回true，则代表该锁处于未初始化（未绑定状态）。

一般来说，辅助性的方法都会封装在JJUtil工具类里。

---

## 初始化锁（绑定锁）

```java
JJLockClient.getDefault().initLock(BleDevice bleDevice, InitLockCallback callback);
```

bleDevice是您在扫描时得到的BleDevice的实例，一般在有需要时直接传入即可。而callback则是您需要传的回调，以便您在不同的状况下做出合适的操作。

```java
JJLockClient.getDefault().initLock(bleDevice, new InitLockCallback() {
    @Override
    public void onInitLockSuccess() {
        
    }

    @Override
    public void onReceivedData(InitLockInfoBean data, InitLockOp initLockOp) {

    }

    @Override
    public void onFail(int errorCode, String errMsg) {

    }
});
```

如您所见，其实您只需要实现InitLockCallback，重点也是InitLockCallback的实现，下面将介绍该如何实现。

```java
new InitLockCallback() {
    @Override
    public void onInitLockSuccess() {
		//当初始化锁（绑定锁）成功时将会调用，初始化流程已结束
        //您应该在此处调用cloudapi暴露出来的修改别名的接口
    }

    @Override
    public void onReceivedData(InitLockInfoBean data, InitLockOp initLockOp) {
		//当收到随机字节库时调用，注意，此时初始化锁的流程并未结束，您在接下来应该进行两步操作
        //第一步：通过cloudapi暴露出来的接口上传data里的数据
        ...省略代码...
        //第二步：如果上传data成功，一定要尽快调用（立刻）以下方法来确保初始化锁的流程继续进行
        initLockOp.informInitLockOk();
        //这里需要补充说明的是informInitLockOk（）的中文意思'通知初始化锁成功'指的是上传随机字节库成功后通知sdk进行后续操作，而并非指此时初始化锁流程已经成功（还有失败的可能性，比如您在上传随机字节库成功后很久之后才调用此方法）
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
		//当初始化锁失败的时候，这个方法将会被调用
         //值得一提的是，以后您在许多回调中都要实现这个方法，因为你所实现的所有接口全都继承自WhenError接口。是的，onFail(int errorCode, String errMsg)方法是在WhenError接口中定义。
        //您在实现onFail接口时，SDK为您提供了两个参数errorCode和errMsg。前者是错误码，后者是对错误信息的描述。最重要的是errorCode，您可以在文档中查看所有errorCode对应的含义。注意，不要太依赖于errMsg，因为SDK有时候会为您返回null或者""。
    }
}
```

WhenError接口的定义

```java
public interface WhenError {

    /**
     * 当失败的时候将会回调的方法
     * @param errorCode 错误码，可用于在文档中查找具体错误类型
     * @param errMsg 错误消息，有时候可能会提供空字符串或null
     */
    public void onFail(int errorCode, String errMsg);


}
```

不管errorCode的值是多少，您需要明白当onFail方法调用的时候您的操作已经失败了，您可以根据errorCode显示不同的提示信息，或者根据errorCode来定位您程序的问题所在。

---

## 重置锁（解绑锁）

重置锁意味着恢复出厂设置,如果你想要再次使用该锁,你需要重新初始化该锁。

注意，解绑前需要用户先拍亮门锁面板。

```java
//解绑（重置）锁
JJLockClient.getDefault().resetLock(lockMac, anyCode, initialTimeString, 
resetLockCallback);
```

重置锁有4个参数，其中前三个都在cloudapi返回的锁列表数据里，最后一个则需要你自己去实现。

```java
new ResetLockCallback(){
    @Override
    public void onFail(int errorCode, String errMsg) {
		//onFail的实现根据需求的不同而变化，本质上都大同小异，此处不再赘述
    }

    @Override
    public void onResetLockOk() {
        //重置锁成功时候的回调
        //注意，注意，注意，您应当在此回调里调用cloudapi响应的接口删除锁记录
    }
};
```

---

## 开锁

```java
JJLockClient.getDefault().openLock(lockMac, anyCode,openLockCallback);
```

开锁只需要三个参数，第一个参数是锁的mac地址，由服务器提供。第二个参数叫anyCode，同样由服务器提供给你。

因此只有第三个参数需要您去实现。

```java
JJLockClient.getDefault().openLock(lockMac, anyCode, new OpenLockCallback() {
    @Override
    public void onOpenLockSuccess() {
		//当开锁成功时回调。一般情况下您除了展示界面效果外，还需要根据cloudapi向服务器上传蓝牙开锁记录。
    }

    @Override
    public void onGetBatteryPercentage(byte datum) {
		//当得到锁电量的时候，如datum的值为67，则表示现在的电量是百分之67.
        //注意，获取电量成功并不代表开锁成功
        //您也可以选择对此方法做空实现
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
		//当操作失败的时候回调
    }
});
```

## 同步时间

注意，同步时间时锁需要在手机附近，代码如下：

```java
JJLockClient.getDefault().syncLockTime(lockMac, new SyncTimeCallback() {
    @Override
    public void syncTimeSuccess() {
        //同步时间成功时执行
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
        //同步时间失败时执行
    }
});
```

## 同步数据

同步数据指同步锁的数据到服务器。注意，同步数据时锁需要在手机附近，代码如下：

```java
JJLockClient.getDefault().syncLockData(lockMac, new SyncDataCallback() {
    @Override
    public void onNoData() {
        //与锁通信成功，但锁没有记录可以上传。
    }

    @Override
    public void onGetData(int recordCount, String data) {
		//获取数据成功，这种情况SDK接入者应该调用cloudapi将锁的记录上传到服务器
        //recordCount为记录条数
        //data为记录内容，可直接上传到服务器
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
		//操作失败的情况
    }
});
```

---

## 添加卡

```java
JJLockClient.getDefault().addIC(lockMac, new AddICCallback() {
    @Override
    public void onAddIcOk() {
        //当最终添加ic卡成功时回调，注意，您正确地实现onGetIcId方法后，此方法才可能执行。
    }

    @Override
    public void onGetIcId(String idStr, AddICOp addICOp) {
		//此方法在读取ic卡成功的时候调用
        //您正常情况下应该在此方法里调用cloudapi以换取添加ic流程后续需要的数据
        //注意，此处提供了两个参数，第一个参数是ic卡的id字符串表现形式，用于您上传到服务器
        //第二个参数addICOp用于您在拿到服务器的数据后在此实例上进行afterGetPwdFromServer(String)的调用以便完成添加ic卡的后续操作
    }

    @Override
    public void icCloseToLock() {
		//此方法在锁准备好读取ic卡的时候调用，您一般应当在此方法中更新ui界面，以提示用户将卡靠近锁的感应区
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
		//在失败的时候调用，您通过查询errcode可以知道失败的原因
        //注意，当errorCode为ERR_FINALLY_ADDIC_FAIL时您应调用cloudapi的接口以删除已生成的ic卡的记录
        //因此，您必须在此方法里判断当 errorCode==JJErrorCode.ERR_FINALLY_ADDIC_FAIL 的情况
    }
});
```

下面是添加ic卡除了通用错误外还可能有以下几种可能性（在JJErrorCode类中定义）

```java
public static final int ERR_CANNOT_READCARD = -31;//锁不能进入读卡模式
public static final int ERR_IC_COUNT_IS_MAX = -32;//ic卡的数量已经达到上限
public static final int ERR_IC_ALREADY_EXIST = -33;//ic卡已经添加过
public static final int ERR_FINALLY_ADDIC_FAIL = -34;//最终添加ic卡失败
```

---

## 修改卡

```java
JJLockClient.getDefault().alterIC(lockMac, idStr, password, new AlterICCallback() {
    @Override
    public void onAlterIcOk() {
		//修改IC卡成功的情况
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
		//修改IC卡失败的情况。需要注意的是，如果errcode是ERR_IC_NOT_EXIT或者ERR_UNKONOW的情况，您应该调用apicloud的接口回滚此次对ic卡的修改。
        
    }
});
```

另外需要说明的是，修改IC卡您需要传入4个参数。其中有三个参数都包含在服务器返回的IC卡列表信息里。lockMac是服务器返回的锁的lockMac地址，idStr代表IC卡id的字符串，您只需要原样填入；password也由服务器返回。AlterICCallback则需要您自己实现。

## 删除卡

```java
JJLockClient.getDefault().deleteIC(lockMac, idStr, new DeleteICCallback() {
    @Override
    public void deleteICOk() {
        //当锁删除IC卡成功的时候调用，注意，您应该在此回调里调用apicloud的删除IC卡的接口 以删除服务器记录的IC卡
    }

    @Override
    public void onFail(int errorCode, String errMsg) {
		//当删除IC卡失败的情况
    }
});
```

---

## 固件升级

```java
JJLockClient.getDefault().firmwareUpgrade(Context, "bab7c754-8954-4f62-ba85-88d64b52c3b8", "从服务器拿到的mac地址",firmwareUpgradeCallback ,otaCallback );
```



## 附录（errcode表）

