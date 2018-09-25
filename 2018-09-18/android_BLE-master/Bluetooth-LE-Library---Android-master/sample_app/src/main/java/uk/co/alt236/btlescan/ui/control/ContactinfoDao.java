package uk.co.alt236.btlescan.ui.control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Sky on 2016/10/7.
 */

public class ContactinfoDao {
   public MydbHelper mMydbHelper;

    /**
     * 在构造方法中实例化帮助类
     *
     * @param context
     */
    public ContactinfoDao(Context context) {

        mMydbHelper = new MydbHelper(context);
    }

    /**
     * 将数据库打开，帮帮助类实例化，然后利用这个对象
     */

    public long addData(String time,String temper , String pressure) {
        /**
         * getWritableDatabase:
         * Create and/or open a database that will be used for reading and writing.
         *
         *
         * 增删改查，每一次操作都要得到数据库，操作完成后都要记得关闭
         * getWritableDatabase得到后数据库才会被创建
         * 数据库文件利用DDMS可以查看，在 data/data/包名/databases 目录下即可查看
         */
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        /**
         * ContentValues:This class is used to store a set of values
         * 存储值得集合
         */
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("temper", temper);
        values.put("pressure", pressure);
        //Log.e("timetrue", time);
        /**
         * 参数1：表名
         * 参数2：可选择的项。可空、
         * 参数3：要添加的值
         * 返回类型：添加数据所在行数，如果返回-1，则表示添加失败
         */
        long row = writableDatabase.insert(mMydbHelper.get_table_time(), null, values);

        return row;
    }

    public float[] getData_pressure()
    {
        //得到该表中时间最后的20个压力数据
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        int id;
        float[]  data_pressure=new float[20];
        for (int i=0;i<20;i++){data_pressure[i]=0;}
        Cursor cursor= writableDatabase.query(mMydbHelper.get_table_time(),null,null,null,null,null,null);
        if(cursor.moveToFirst())
        {
            if(cursor.getCount()>20)
                for(int i=0;i<20;i++)
                {
                    cursor.moveToPosition(cursor.getCount()-20+i);
                    String temper=cursor.getString(3);
                    data_pressure[i]=Float.parseFloat(temper);
                }
        }
        cursor.close();
        writableDatabase.close();
        return data_pressure;

    }
    public float[]  getData_temper()
    {
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        int id;
        float[]  data_temper=new float[20];
        /**
         * 参数1：查询的表
         * 参数2：查询的列
         * 参数3：selection
         * 参数4：selectionArgs
         * 参数5：groupBy
         * 参数6：having
         * 参数7：orderBy
         */
        for (int i=0;i<20;i++){data_temper[i]=0;}
        data_temper[0]=20;
        Cursor cursor= writableDatabase.query(mMydbHelper.get_table_time(),null,null,null,null,null,null);
        if(cursor.moveToFirst())
        {
            if(cursor.getCount()>20)
            for(int i=0;i<20;i++)
            {
                cursor.moveToPosition(cursor.getCount()-20+i);
                String temper=cursor.getString(2);
                data_temper[i]=Float.parseFloat(temper);
            }
        }
        cursor.close();
        writableDatabase.close();
        return data_temper;

    }
    /**
     *查询
     * @param
     * @return
     */
    public void dayinData() {
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        int id;
        /**
         * 参数1：查询的表
         * 参数2：查询的列
         * 参数3：selection
         * 参数4：selectionArgs
         * 参数5：groupBy
         * 参数6：having
         * 参数7：orderBy
         */
        Cursor cursor= writableDatabase.query(mMydbHelper.get_table_time(),null,null,null,null,null,null);
        if(cursor.moveToFirst())
        {
            for(int i=0;i<cursor.getCount();i++)
            {
                cursor.moveToPosition(i);
                id = cursor.getInt(0);
                String id1=String.valueOf(id);
                String time = cursor.getString(1);
                String temper = cursor.getString(2);
                String pressure = cursor.getString(3);
                Log.e("cursor value:","id is"+id1+"time is:" + time + "temper is:" + temper + "pressure is:" + pressure);
            }
        }
        cursor.close();
        writableDatabase.close();

    }

    public String upversion() {

        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        mMydbHelper.onUpgrade(writableDatabase,getversion(),getversion()+1);
        return  mMydbHelper.get_table_time();

    }
    public  void rename_table_name(String table_name_new)
    {
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        mMydbHelper.rename_tablename(writableDatabase,table_name_new);
    }
    /**
     * 获取当前数据库版本号
     * @param
     * @param
     * @return
     */
    public int getversion() {

        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();

        int version = writableDatabase.getVersion();


         //int row = writableDatabase.update("contactinfo", values, "name=?", new String[]{name});

       return version;
    }
    public String[] get_table_name_all()
    {
        int ii=0;
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
                Cursor cursor = writableDatabase.rawQuery("select name from sqlite_master where type='table' order by name", null);
        String[] table_name_all=new String[cursor.getCount()];
//        String da=String.valueOf(cursor.getCount());
//        Log.e("ggoodd",da);
        while(cursor.moveToNext()){
            table_name_all[ii]=cursor.getString(0);
            ii++;
            //遍历出表名
//            String name = cursor.getString(0);
//            Log.e("ggoodd", name);
        }
        //for(int i=0;i<table_name_all.length;i++)Log.e("table_name",table_name_all[i]);
        cursor.close();
        return table_name_all;
    }
    public String get_lasttime()
    {
        SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
        String time_get="";
        /**
         * 参数1：查询的表
         * 参数2：查询的列
         * 参数3：selection
         * 参数4：selectionArgs
         * 参数5：groupBy
         * 参数6：having
         * 参数7：orderBy
         */
        Cursor cursor= writableDatabase.query(mMydbHelper.get_table_time(),null,null,null,null,null,null);
        if(cursor.moveToFirst())
        {
                cursor.moveToPosition(cursor.getCount()-1);
                time_get=cursor.getString(1);

        }
        cursor.close();
        writableDatabase.close();
        return time_get;


    }
    public void  verify_tablename()
    {
           //table_name true name example:time_start2018_09_19_20_22_17time_end2018_09_19_20_22_23
            SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
            String time_change;
//            String[] strings=get_table_name_all();
            String[] strings = Arrays.copyOfRange(get_table_name_all(),3,get_table_name_all().length);
            /**
             * 参数1：查询的表
             * 参数2：查询的列
             * 参数3：selection
             * 参数4：selectionArgs
             * 参数5：groupBy
             * 参数6：having
             * 参数7：orderBy
             */
            for(int i=0;i<strings.length;i++) {
                mMydbHelper.set_table_time(strings[i]);
                Cursor cursor = writableDatabase.query(strings[i], null, null, null, null, null, null);
                cursor.moveToFirst();
                while ((cursor.getString(1).equals("0"))) cursor.moveToNext();
                time_change = cursor.getString(1).replace("-", "_").replace(":", "_").replace(" ", "_");
                Log.e("timechange",time_change);
                cursor.moveToLast();
                time_change = "time_start" + time_change + "time_end" + cursor.getString(1).replace("-", "_").replace(":", "_").replace(" ", "_");
                cursor.close();
                Log.e("timechange1",time_change);
                if(!time_change.equals(strings[i])) {
                    Log.e("timechange1",time_change);
                    rename_table_name(time_change);
                }
            }
            //time_change的格式yyyy-MM-dd HH:mm:ss;
        writableDatabase.close();
    }
    public String[] scan_singletime_data(String single_time)
    {
        verify_tablename();
        String[] table_name_all = Arrays.copyOfRange(get_table_name_all(),3,get_table_name_all().length);
        String[] string_return=new String[2];
        Date dateTime1=null,dateTime2=null,dateTime3=null;
        //表名为（例）：time_start2018_09_19_19_50_40time_end2018_09_19_19_50_45
        //截去android_在metadata, sqlite_sequence, time_start.这三个默认的数据表;
        System.out.print("substring1"+Arrays.toString(get_table_name_all()));
        System.out.print("substring"+Arrays.toString(table_name_all));
        for(int i=0;i<table_name_all.length;i++)
        {
//            String[] string_starttime_endtime= String_tablename_getnormaltime(table_name_all[i]);
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            try {
//                 dateTime1 = dateFormat.parse(single_time);
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            try {
//                    dateTime2 = dateFormat.parse(string_starttime_endtime[0]);
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            try {
//                dateTime3 = dateFormat.parse(string_starttime_endtime[1]);
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            int i1=dateTime2.compareTo(dateTime1);//-1
//            int i2=dateTime3.compareTo(dateTime1);//1 //两个Date类型的变量可以通过compareTo方法来比较。此方法的描述是这样的：如果参数 Date 等于此 Date，则返回值 0；如果此 Date 在 Date 参数之前，则返回小于 0 的值；如果此 Date 在 Date 参数之后，则返回大于 0 的值。
//            Log.e("???7",String.valueOf(i1));
//            Log.e("???8",String.valueOf(i2));
//            //a.compareto(b)   b>a---return -1；
//            //                 b<a---return 1;
            if(get_flag_timeString_compareto(String_tablename_getnormaltime(table_name_all[i]),single_time))
            {
                Log.e("gogogo","gogog");
                SQLiteDatabase writableDatabase = mMydbHelper.getWritableDatabase();
                Cursor cursor= writableDatabase.query(mMydbHelper.get_table_time(),null,null,null,null,null,null);
                cursor.moveToFirst();
                            for(int ii=1;i<cursor.getCount();i++)
                            {
                                cursor.moveToPosition(i);
                                cursor.getString(1);
                                if(cursor.getString(1).equals(single_time))
                                 break;
                            }
//                while (!(cursor.getString(1).equals(single_time)))
//                    cursor.moveToNext();
                Log.e("truetimeis",cursor.getString(1));
                string_return[0]=cursor.getString(2);
                string_return[1]=cursor.getString(3);
                Log.e("stringreturn",string_return[0]);
                //                cursor.getString(2);//temper;
//                cursor.getString(3);//pressure;
                cursor.close();
                writableDatabase.close();
            }
        }
        return string_return;
    }
    private  Boolean get_flag_timeString_compareto(String[] string_starttime_endtime,String single_time )
    {
        //返回flag，若为真 则说明查询的single_time在这个表中;反之则不在;
        Date dateTime1=null,dateTime2=null,dateTime3=null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            dateTime1 = dateFormat.parse(single_time);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        try {
            dateTime2 = dateFormat.parse(string_starttime_endtime[0]);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        try {
            dateTime3 = dateFormat.parse(string_starttime_endtime[1]);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        try {
            if(dateTime2.compareTo(dateTime1)==-1)
                if(dateTime3.compareTo(dateTime1)==1)
                return true;
            else return false;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
//        int i1=dateTime2.compareTo(dateTime1);//-1
//        int i2=dateTime3.compareTo(dateTime1);//1
        //a.compareto(b)   b>a---return -1；
        //                 b<a---return 1;
        return false;
    }
    private String[] String_tablename_getnormaltime(String tablename) //分离出一个表的开始时间和终止时间
    {
        String[] string_starttime_endtime= new String[2];
        //if(tablename.length()>55) {
            string_starttime_endtime[0] = tablename.substring(10, 29);
            string_starttime_endtime[1] = tablename.substring(37, 56);
        //}
        string_starttime_endtime[0]=string_starttime_endtime[0].replace("_","-");
        StringBuffer buffer = new StringBuffer(string_starttime_endtime[0]);
        buffer.replace(10,11," ")
                .replace(13,14,":")
                .replace(16,17,":");  //将time_start2018_09_19_19_50_40time_end2018_09_19_19_50_45 分离成2018-09-20 21:33:53 和2018-09-19 19:50:45
        string_starttime_endtime[0]=buffer.toString();

        string_starttime_endtime[1]=string_starttime_endtime[1].replace("_","-");
        buffer = new StringBuffer(string_starttime_endtime[1]);
        buffer.replace(10,11," ")
                .replace(13,14,":")
                .replace(16,17,":");  //同上
        string_starttime_endtime[1]=buffer.toString();
        return string_starttime_endtime;
    }
    public String get_tablename()
    {
        return  mMydbHelper.get_table_time();

    }
}