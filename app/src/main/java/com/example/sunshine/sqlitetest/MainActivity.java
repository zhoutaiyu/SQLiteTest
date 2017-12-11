package com.example.sunshine.sqlitetest;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText name, price1, id1, price2, id2, id3;
    Button b1, b2, b3, b4;
    ListView lv;
    SQLiteDatabase sdb = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //找控件
        name = (EditText) findViewById(R.id.name);
        price1 = (EditText) findViewById(R.id.price1);
        id1 = (EditText) findViewById(R.id.id1);
        price2 = (EditText) findViewById(R.id.price2);
        id2 = (EditText) findViewById(R.id.id2);
        id3 = (EditText) findViewById(R.id.id3);
        b1 = (Button) findViewById(R.id.b1);
        b2 = (Button) findViewById(R.id.b2);
        b3 = (Button) findViewById(R.id.b3);
        b4 = (Button) findViewById(R.id.b4);
        lv = (ListView) findViewById(R.id.listView1);
        //为点击设置监听
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //创建或打开数据库
        getDb("/database", "booksdb.db3");
        //创建表
        createTable("tb_books");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b1:
                insertData();
                break;
            case R.id.b2:
                updateData();
                break;
            case R.id.b3:
                deleteData();
                break;
            case R.id.b4:
        }
        showData();
    }

    //创建数据库
    public SQLiteDatabase getDb(String dbPath, String dbName) {
        File dbDir = new File(Environment.getExternalStorageDirectory(),
                dbPath);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        File dbFile = new File(dbDir, dbName);
        try {
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            sdb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdb;
    }

    public boolean tableIsExist(String tableName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from " + "sqlite_master" + " where type ='table' and name ='" + tableName.trim() + "' ";
            cursor = sdb.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    //创建数据库表
    public void createTable(String tbName) {
        if (tableIsExist(tbName)) {
            return;
        }
        //不存在则创建
        String sql = "create table " + tbName + "(" +
                "_id integer primary key autoincrement," +
                "bookname varchar,bookprice float)";
        if (sdb == null) {
            Log.i("tag", "sdf为空");
        }
        sdb.execSQL(sql);
    }

    //数据适配器向ListView填充数据
    private void showData() {
        Cursor c = queryData();
        CursorAdapter ca = new SimpleCursorAdapter(this,
                R.layout.listview_item, c,
                new String[]{"_id", "bookname", "bookprice"}
                , new int[]{R.id.listView_item_id, R.id.listView_item_name
                , R.id.listView_item_price}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv.setAdapter(ca);
    }

    //根据id查询数据，如果ID为null,则查询所有数据
    private Cursor queryData() {
        String id = id3.getText().toString();
        Cursor c = null;
        if (id.length() < 1) {
            c = sdb.query("tb_books",
                    new String[]{"_id", "bookname", "bookprice"}
                    , null, null,
                    null, null, null);
        } else {
            c = sdb.query("tb_books", new String[]{"_id", "bookname", "bookprice"},
                    "_id=?", new String[]{id}, null, null, null);
        }
        return c;
    }

    //根据id删除
    private void deleteData() {
        String id = id2.getText().toString();
        if (id.length() < 1) {
            return;
        }
        int r = sdb.delete("tb_books", "_id=?",
                new String[]{id});
        Log.i("tag", "删除数据成功 id=" + r);
    }

    //根据id更新
    private void updateData() {
        String id = id1.getText().toString();
        String bprice = price2.getText().toString();
        if (id.length() < 1) return;
        ContentValues cv = new ContentValues();
        cv.put("bookprice", bprice);
        long r = sdb.update("tb_books",
                cv, "_id=?"
                , new String[]{id});
        Log.i("tag", "更新数据成功 ID=" + r);
    }

    //插入
    private void insertData() {
        String bName = name.getText().toString();
        String bprice = price1.getText().toString();
        //创建ContentValues，存放数据
        ContentValues cv = new ContentValues();
        cv.put("bookname", bName);
        cv.put("bookprice", bprice);
        long r = sdb.insert("tb_books", null, cv);
        Log.i("tag", "插入数据成功 ID = " + r);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sdb != null) {
            sdb.close();
            sdb = null;
        }
    }
}
