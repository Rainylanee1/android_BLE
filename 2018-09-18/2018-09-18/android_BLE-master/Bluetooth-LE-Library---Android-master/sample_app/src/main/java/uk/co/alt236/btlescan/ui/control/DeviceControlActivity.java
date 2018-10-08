/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.alt236.btlescan.ui.control;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import tech.linjiang.suitlines.SuitLines;
import tech.linjiang.suitlines.Unit;

import java.sql.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.resolvers.GattAttributeResolver;
import uk.co.alt236.bluetoothlelib.util.ByteUtils;
import uk.co.alt236.btlescan.R;
import uk.co.alt236.btlescan.services.BluetoothLeService;
import uk.co.alt236.btlescan.ui.date.DateTimepickerDialog;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity {
    private static final String EXTRA_DEVICE = DeviceControlActivity.class.getName() + ".EXTRA_DEVICE";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    @Bind(R.id.gatt_services_list)
    protected ExpandableListView mGattServicesList;
    @Bind(R.id.connection_state)
    protected TextView mConnectionState;
    @Bind(R.id.uuid)
    protected TextView mGattUUID;

    @Bind(R.id.data_as_string)
    protected TextView mDatapressure;
    @Bind(R.id.data_as_array)
    protected TextView mDatatemper;
    private Exporter mExporter;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private  Button button;
    private Button button_chaxun;
    private SuitLines suitLines_temper,suitLines_pressure;
    //GattDataAdapterFactory gattDataAdapterFactory;
    private GattDataAdapterFactory.GattDataAdapter adapter_1;
    private BluetoothGattCharacteristic characteristic_1;
    int charaProp_1;
    Boolean flag_connect=false;
    ContactinfoDao mContactinfoDao;
    public  String table_name;
    public String[] String_single_data;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private BluetoothLeDevice mDevice;
    private State mCurrentState = State.DISCONNECTED;
    private String mExportString;
    private Boolean falg_start=true;
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.
    //					      this can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(State.CONNECTED);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                clearUI();
                updateConnectionState(State.DISCONNECTED);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)) {
                clearUI();
                updateConnectionState(State.CONNECTING);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());//具体的蓝牙设备以及通道，调试时可展开.
                //已连接后的操作
                        final BluetoothGattCharacteristic characteristic =
                        adapter_1.getBluetoothGattCharacteristic(); //groupPosition: 4  childPosition: 0
                        characteristic_1=characteristic;//循环读所用
                        final int charaProp = characteristic.getProperties();
                        charaProp_1=charaProp;//charaProp: 18
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                    flag_connect=true;
                    mGattServicesList.setVisibility(View.INVISIBLE);
                    suitLines_pressure.setVisibility(View.VISIBLE);
                    suitLines_temper.setVisibility(View.VISIBLE);
                    if(sqlTableIsExist("time_start"))
                    {
                        table_name=mContactinfoDao.upversion(); //并不是第一次使用，更新版本（新建待时间的表）
                        Log.e("table_name",table_name);
                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String noData = getString(R.string.no_data);
                final String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID_CHAR);
                final byte[] dataArr = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);
                mGattUUID.setText(tryString(uuid, noData));
                judge_Is_firstconnest();//如果之前没有数据，为了防止空指针错误，将表中前20个数据置0;
                data_manage(dataArr);//得到数据，刷新UI显示数据，将读到的数据添加到数据表中；
                setSuitLines();//将处理好的数据，即温度、压力,分别用图显示出来;
            }
        }
    };
    private void setSuitLines()
    {
        List<Unit> lines=new ArrayList<>();
        for(int i=0;i<20;i++)
        {
            lines.add(new Unit(mContactinfoDao.getData_temper()[i],"0"));//temper数据显示
        }
        suitLines_temper.value_1=80;
        suitLines_temper.feed(lines);
        List<Unit> lines_pressure=new ArrayList<>();
        for(int i=0;i<20;i++)
        {
            lines_pressure.add(new Unit(mContactinfoDao.getData_pressure()[i],"0"));//pressure护具显示
        }
        suitLines_pressure.value_1=150;
        suitLines_pressure.feed(lines_pressure);
    }
    private void judge_Is_firstconnest()
    {
        if(falg_start) {
            for (int j = 0; j < 20; j++)
                mContactinfoDao.addData("0", "0", "0");
            falg_start=false;
        }
    }
    private void data_manage(byte[] dataArr)
    {
        //刷新UI显示数据，将读到的数据添加到数据表中；
        String string_temper_pressure[]=new  String[2];
        string_temper_pressure=temper_pressure_get(dataArr);//将读到的dataArr转换成String;
        mDatatemper.setText(string_temper_pressure[0]);//set_temper
        mDatapressure.setText(string_temper_pressure[1]);//set_pressure
        mContactinfoDao.addData(get_localtime(),string_temper_pressure[0],string_temper_pressure[1]);//添加至数据中
        Log.e("tablenameis",mContactinfoDao.get_tablename());
        }
    private String[] temper_pressure_get(byte dataArr[])
    {
        int[] byte_int=new int[6];//储存从byte转换的int的数据
        int num;
        String string_return[]=new String[2];
        for (int i=0;i<6;i++)  //int[0-1]为温度，int[2-5]为压力；
        {
            num=dataArr[i]&0xFF;
            byte_int[i]=num;
        }
        num=(byte_int[1]<<8)+byte_int[0];
        string_return[0]=int_to_string(num,100);
        num=(byte_int[5]<<24)+(byte_int[4]<<16)+(byte_int[3]<<8)+byte_int[2];
        string_return[1]=int_to_string(num,10000);
        return string_return;
    }
    private String int_to_string(int number,int times)
    {
        float value=number;
        value=value/(times);
        return  String.valueOf(value);
    }
    private String get_localtime()
    {
        SimpleDateFormat sDateFormat    =   new    SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sDateFormat.format(new    java.util.Date());
    }
    private void clearUI() {
        mExportString = null;
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mGattUUID.setText(R.string.no_data);
        mDatatemper.setText(R.string.no_data);
        mDatapressure.setText(R.string.no_data);
    }
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
    public boolean sqlTableIsExist(String tableName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            //search.db数据库的名字
            db = openOrCreateDatabase("Sky.db", Context.MODE_PRIVATE, null);
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
            cursor.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }//获取数据库中表是否存在
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        mExportString = mExporter.generateExportString(
                mDevice.getName(),
                mDevice.getAddress(),
                gattServices);
       final GattDataAdapterFactory.GattDataAdapter adapter = GattDataAdapterFactory.createAdapter(this,gattServices);
       adapter_1 = adapter;
        // final GattDataAdapterFactory.GattDataAdapter adapter = GattDataAdapterFactory.createAdapter(this,gattServices);
        //final GattDataAdapterFactory.GattDataAdapter adapter = GattDataAdapterFactory.createAdapter(this, gattServices);
        mGattServicesList.setAdapter(adapter);
        invalidateOptionsMenu();
    }
    private void showNormalDialog_selet_scankind(final String[] table_name_all)
    {
        //选择单次时间搜索或者范围时间搜索（多次数据）
        /*
 @setIcon 设置对话框图标
         *
 @setTitle 设置对话框标题
         *
 @setMessage 设置对话框消息提示
         *
 setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder
            normalDialog = 
            new AlertDialog.Builder(DeviceControlActivity.this);
        //normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("请选择查询模式")
                    .setMessage("搜索单次数据或某一时间段的数据")
                    .setPositiveButton("多次数据",
                        new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface
                dialog, int which)
            {
                mContactinfoDao.verify_tablename();
                Log.e("tableisExist",mContactinfoDao.get_tablename());
                for (int a = 0; a < table_name_all.length; a++) {
                    if(table_name_all[a].contains("time_start")) {
                        table_name_all[a] = table_name_all[a].substring(10).replaceAll("time_end", "至").replace("_",":");
                    }
                    //time_start2018_09_19_19_50_40time_end2018_09_19_19_50_45转换为2018:09:19:19:50:40至2018:09:19:19:50:45
                }                                                                                       //至为19
                AlertDialog.Builder builder=new AlertDialog.Builder(DeviceControlActivity.this);
                builder.setTitle("选择时间");
                builder.setItems(table_name_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,final int i) {
                        dialogInterface.dismiss();
                        Toast.makeText(DeviceControlActivity.this, table_name_all[i],
                                Toast.LENGTH_SHORT).show();

                        final Calendar calendar=Calendar.getInstance();
                        calendar.set(Integer.parseInt(table_name_all[i].substring(0,4)),
                                Integer.parseInt(table_name_all[i].substring(5,7))-1,
                                Integer.parseInt(table_name_all[i].substring(8,10)),
                                Integer.parseInt(table_name_all[i].substring(11,13)),
                                Integer.parseInt(table_name_all[i].substring(14,16)),
                                Integer.parseInt(table_name_all[i].substring(17,19)));
                        final Date date=calendar.getTime();
                        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time=df.format(date);//得到点击区间的起始时间
                        System.out.println("abcdis"+time);

                        final Calendar calendar1=Calendar.getInstance();
                        calendar1.set(Integer.parseInt(table_name_all[i].substring(20,24)),
                                Integer.parseInt(table_name_all[i].substring(25,27))-1,
                                Integer.parseInt(table_name_all[i].substring(28,30)),
                                Integer.parseInt(table_name_all[i].substring(31,33)),
                                Integer.parseInt(table_name_all[i].substring(34,36)),
                                Integer.parseInt(table_name_all[i].substring(37,39)));
                        Date date1=calendar1.getTime();
                        SimpleDateFormat df1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time1=df1.format(date1);//得到点击区间的终止时间

                        final TimePickerView pvtime1; //单次或起始时间选择
                        pvtime1 = new TimePickerBuilder(DeviceControlActivity.this, new OnTimeSelectListener() {
                            @Override
                            public void onTimeSelect(final Date date_start, View v) {
                                TimePickerView pvTime_end = new TimePickerBuilder(DeviceControlActivity.this, new OnTimeSelectListener() {
                                    @Override //终止时间选择
                                    public void onTimeSelect(Date date_end, View v) {
                                        String[] String_mutiple_data=null;
                                        ProgressDialog progressDialog_mutiple = ProgressDialog.show(DeviceControlActivity.this,"提示","正在搜索中",false);
                                        String_mutiple_data=mContactinfoDao.scan_mutiple_data(getTime(date_start),getTime(date_end));
                                        progressDialog_mutiple.dismiss();

                                        AlertDialog.Builder builder_data_mutiple=new AlertDialog.Builder(DeviceControlActivity.this);
                                        builder_data_mutiple.setTitle("选择时间");
                                        builder_data_mutiple.setItems(String_mutiple_data, new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i)
                                            {

                                            }
                                        });
                                        builder_data_mutiple.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                Toast.makeText(DeviceControlActivity.this, "确定", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });
                                        builder_data_mutiple.create().show();
                                    }
                                })
                                        .setType(new boolean[]{true, true, true, true, true, true})
                                        .setTitleText("选择终止时间 "+table_name_all[i])
                                        .setRangDate(calendar,calendar1)
                                        .build();
                                Dialog mDialog = pvTime_end.getDialog();
                                if (mDialog != null) {

                                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            Gravity.CENTER);

                                    params.leftMargin = 0;
                                    params.rightMargin = 0;
                                    pvTime_end.getDialogContainerLayout().setLayoutParams(params);

                                    Window dialogWindow = mDialog.getWindow();
                                    if (dialogWindow != null) {
                                        dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                                        dialogWindow.setGravity(Gravity.CENTER);//改成center,中间显示
                                    }
                                }

                                pvTime_end.show();

                            }
                        })
                                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                                    @Override
                                    public void onTimeSelectChanged(Date date) {
                                        Log.i("pvTime", "onTimeSelectChanged");
                                    }
                                })
                                .setType(new boolean[]{true, true, true, true, true, true})
                                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                                .setRangDate(calendar,calendar1)
                                .setTitleText("选择选择起始时间 "+table_name_all[i])
                                //.isCyclic(true)
                                .build();

                        Dialog mDialog = pvtime1.getDialog();
                        if (mDialog != null) {

                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    Gravity.CENTER);

                            params.leftMargin = 0;
                            params.rightMargin = 0;
                            pvtime1.getDialogContainerLayout().setLayoutParams(params);

                            Window dialogWindow = mDialog.getWindow();
                            if (dialogWindow != null) {
                                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                                dialogWindow.setGravity(Gravity.CENTER);//改成center,中间显示
                            }
                        }

                        pvtime1.show();

                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(DeviceControlActivity.this, "确定", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                builder.create().show();
                //pvTime.show();
                //...To-do

            }
        })
                    .setNegativeButton("获取单次数据",
                        new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface
                dialog, int which)
            {
//                mContactinfoDao.get_first_lasttime(); 验证每个表名和是否实际开始、结束时间一致;
                    mContactinfoDao.verify_tablename();
                                Log.e("tableisExist",mContactinfoDao.get_tablename());
                    for (int a = 0; a < table_name_all.length; a++) {
                        if(table_name_all[a].contains("time_start")) {
                            table_name_all[a] = table_name_all[a].substring(10).replaceAll("time_end", "至").replace("_",":");
                        }
                        //time_start2018_09_19_19_50_40time_end2018_09_19_19_50_45转换为2018:09:19:19:50:40至2018:09:19:19:50:45
                    }                                                                                       //至为19
                    AlertDialog.Builder builder=new AlertDialog.Builder(DeviceControlActivity.this);
                    builder.setTitle("选择时间");
                    builder.setItems(table_name_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(DeviceControlActivity.this, table_name_all[i],
                                    Toast.LENGTH_SHORT).show();

                            Calendar calendar=Calendar.getInstance();
                            calendar.set(Integer.parseInt(table_name_all[i].substring(0,4)),
                                    Integer.parseInt(table_name_all[i].substring(5,7))-1,
                                    Integer.parseInt(table_name_all[i].substring(8,10)),
                                    Integer.parseInt(table_name_all[i].substring(11,13)),
                                    Integer.parseInt(table_name_all[i].substring(14,16)),
                                    Integer.parseInt(table_name_all[i].substring(17,19)));
                            Date date=calendar.getTime();
                            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time=df.format(date);//得到点击区间的起始时间
                            System.out.println("abcdis"+time);

                            Calendar calendar1=Calendar.getInstance();
                            calendar1.set(Integer.parseInt(table_name_all[i].substring(20,24)),
                                    Integer.parseInt(table_name_all[i].substring(25,27))-1,
                                    Integer.parseInt(table_name_all[i].substring(28,30)),
                                    Integer.parseInt(table_name_all[i].substring(31,33)),
                                    Integer.parseInt(table_name_all[i].substring(34,36)),
                                    Integer.parseInt(table_name_all[i].substring(37,39)));
                            Date date1=calendar1.getTime();
                            SimpleDateFormat df1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time1=df1.format(date1);//得到点击区间的终止时间

                            System.out.println("abcdis"+time1);
                            TimePickerView pvtime1;
                            pvtime1 = new TimePickerBuilder(DeviceControlActivity.this, new OnTimeSelectListener() {
                                @Override
                                public void onTimeSelect(Date date, View v) {
                                    //点击了单次时间后，将搜索到的数据用list dialog显示出来;
                                    ProgressDialog progressDialog = ProgressDialog.show(DeviceControlActivity.this,"提示","正在搜索中",false);
                                    //不能取消的progressdialog
                                    String_single_data=mContactinfoDao.scan_singletime_data(getTime(date));
                                    progressDialog.dismiss();
                                    Toast.makeText(DeviceControlActivity.this,"temper is:"+String_single_data[0]+" pressure is"+String_single_data[1], Toast.LENGTH_SHORT).show();
                                    AlertDialog.Builder builder_data=new AlertDialog.Builder(DeviceControlActivity.this);
                                    builder_data.setTitle("选择时间");
                                    builder_data.setItems(String_single_data, new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {

                                        }
                                    });
                                    builder_data.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Toast.makeText(DeviceControlActivity.this, "确定", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                                    builder_data.create().show();
                                }
                            })
                                    .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                                        @Override
                                        public void onTimeSelectChanged(Date date) {
                                            Log.i("pvTime", "onTimeSelectChanged");

                                        }
                                    })
                                    .setType(new boolean[]{true, true, true, true, true, true})
                                    .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                                    .setRangDate(calendar,calendar1)
                                    .setTitleText(" "+table_name_all[i])
                                    //.isCyclic(true)
                                    .build();

                            Dialog mDialog = pvtime1.getDialog();
                            if (mDialog != null) {

                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        Gravity.CENTER);

                                params.leftMargin = 0;
                                params.rightMargin = 0;
                                pvtime1.getDialogContainerLayout().setLayoutParams(params);

                                Window dialogWindow = mDialog.getWindow();
                                if (dialogWindow != null) {
                                    dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                                    dialogWindow.setGravity(Gravity.CENTER);//改成center,中间显示
                                }
                            }

                            pvtime1.show();

                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Toast.makeText(DeviceControlActivity.this, "确定", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    builder.create().show();
                //pvTime.show();
                //...To-do
            }
        });
        normalDialog.show();
    }
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_services);
        final Intent intent = getIntent();
        mDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        mContactinfoDao = new ContactinfoDao(this);
        button = (Button) findViewById(R.id.dayin);
        suitLines_temper=(SuitLines)findViewById(R.id.suitlines_temper);
        suitLines_pressure=(SuitLines)findViewById(R.id.suitlines_pressure);
        button_chaxun=(Button)findViewById(R.id.chaxun);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  Log.e("Logging","database");
                mContactinfoDao.dayinData();
               // mContactinfoDao.get_table_name_all();
            }
        });
       // Log.e("notablename",mContactinfoDao.get_tablename()); 在这打印的话，get_tablename()为空
        button_chaxun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContactinfoDao.verify_tablename();//确定每个表名都包含了正确的数据起始时间和数据终止时间，如果不是则获取表中正确数据并更改表名;
                final String[] table_name_all = Arrays.copyOfRange(mContactinfoDao.get_table_name_all(),3,mContactinfoDao.get_table_name_all().length);
                //截去android_metadata, sqlite_sequence, time_start.这三个默认的数据表;
//                System.out.println("nametrue"+Arrays.toString(table_name_all));
                System.out.println("nametrue1"+Arrays.toString(mContactinfoDao.get_table_name_all()));
                if(table_name_all.length>0)
            {
                showNormalDialog_selet_scankind(table_name_all); //列出已保存的时间，选择查询时间;
            }
                else
                {
                    Toast.makeText(getApplicationContext(), "未存储任何数据！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        ButterKnife.bind(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        while (flag_connect) {
                            try {
                                // If there is an active notification on a characteristic, clear
                                // it first so it doesn't update the data field on the user interface.
                                if (mNotifyCharacteristic != null) {
                                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                                    mNotifyCharacteristic = null;
                                }
                                mBluetoothLeService.readCharacteristic(characteristic_1); //读取数据
                                Thread.sleep(500);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        })     .start();
        ((TextView) findViewById(R.id.device_address)).setText(mDevice.getAddress());
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);

        getSupportActionBar().setTitle(mDevice.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mExporter = new Exporter(this);

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);

        switch (mCurrentState) {

            case DISCONNECTED:
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(null);
                break;
            case CONNECTING:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress_indeterminate);
                break;
            case CONNECTED:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
                break;
            default:
                throw new IllegalStateException("Don't know how to handle: " + mCurrentState);
        }

        if (mExportString == null) {
            menu.findItem(R.id.menu_share).setVisible(false);
        } else {
            menu.findItem(R.id.menu_share).setVisible(true);
        }

        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        mContactinfoDao.verify_tablename();
        //Pause_Destory_change_sqltablename();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDevice.getAddress());
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_share:
                final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                final String subject = getString(
                        R.string.exporter_email_device_services_subject,
                        mDevice.getName(),
                        mDevice.getAddress());

                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, mExportString);

                startActivity(Intent.createChooser(
                        intent,
                        getString(R.string.exporter_email_device_list_picker_text)));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        //Pause_Destory_change_sqltablename();
        mContactinfoDao.verify_tablename();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDevice.getAddress());
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    private void updateConnectionState(final State state) {
        mCurrentState = state;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int colourId;
                final int resId;

                switch (state) {
                    case CONNECTED:
                        colourId = android.R.color.holo_green_dark;
                        resId = R.string.connected;
                        break;
                    case DISCONNECTED:
                        colourId = android.R.color.holo_red_dark;
                        resId = R.string.disconnected;
                        break;
                    case CONNECTING:
                        colourId = android.R.color.black;
                        resId = R.string.connecting;
                        break;
                    default:
                        colourId = android.R.color.black;
                        resId = 0;
                        break;
                }

                mConnectionState.setText(resId);
                mConnectionState.setTextColor(ContextCompat.getColor(DeviceControlActivity.this, colourId));
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        return intentFilter;
    }

    private static String tryString(final String string, final String fallback) {
        if (string == null) {
            return fallback;
        } else {
            return string;
        }
    }

    public static Intent createIntent(final Context context, final BluetoothLeDevice device) {
        final Intent intent = new Intent(context, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRA_DEVICE, device);
        return intent;
    }
//    private void Pause_Destory_change_sqltablename() {
//        //将数据库名字设为开始连接时间至Device结束时间;
//        if (flag_connect) {
//            if (sqlTableIsExist("time_start")) {
//                String time_last = mContactinfoDao.get_lasttime();
//                time_last = time_last.replace("-", "_");
//                time_last = time_last.replace(" ", "_");
//                time_last = time_last.replace(":", "_");
//                mContactinfoDao.rename_table_name(mContactinfoDao.get_tablename() + "time_end" + time_last);
//                Log.e("time_last", time_last);
//            }
//        }
//    }
    private enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }
}
