package tw.tcnr08.a0001;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tw.tcnr08.a0001.providers.FriendsContentProvider;

public class M1917 extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMarkerClickListener,LocationListener
{

    private GoogleMap map;
    static LatLng VGPS = new LatLng(24.172127, 120.610313);  //消防栓
    int currentZoom = 12;  //mapzoom

//    private static String[][] locations = {
//            {"中區職訓", "24.172127,120.610313"},
//            {"東海大學路思義教堂", "24.179051,120.600610"},
//            {"台中公園湖心亭", "24.144671,120.683981"},
//            {"秋紅谷", "24.1674900,120.6398902"},
//            {"台中火車站", "24.136829,120.685011"},
//            {"國立科學博物館", "24.1579361,120.6659828"}};

    private String[] MYCOLUMN = new String[]{"id", "name", "grp", "address"};

    private static String[] mapType = {
            "街道圖",
            "衛星圖",
            "地形圖",
            "混合圖",
            "開啟路況",
            "關閉路況"};

    private Spinner mSpnLocation, mSpnMapType;
    double dLat, dLon;
    private BitmapDescriptor image_des;// 圖標顯示
    private int icosel = 1; //圖示旗標
    //----------------
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private LocationManager locationManager;
    private Location currentLocation;
    private String provider; // 提供資料
    long minTime = 5000;// ms
    float minDist = 5.0f;// meter


    String TAG = "tcnr08=>";


    private static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,  //定位group-高精確度定位
            Manifest.permission.ACCESS_COARSE_LOCATION};  //定位group-僅限裝置

    private List<String> permissionsList = new ArrayList<String>();

    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private TextView txtOutput,tmsg;
    private Marker markerMe;

    //---記錄軌跡-----------------------
    private ArrayList<LatLng> mytrace; // 追蹤我的位置
    //---------------
    private ScrollView controlScroll;
    private CheckBox checkBox;
    private UiSettings mUiSettings;
    //--------------
    //-----自訂義-------
    int infowindow_ico;
    int showMarkerMeON = 0;

    private int resID = 0;
    private int resID1 = 0;


    float Anchor_x;
    float Anchor_y;

    float infoAnchor_x =3.9f;//水滴水平錨點 0.5f
    float infoAnchor_y = 1.1f; //1.0f

    // ========= Thread Hander =============
    private Handler mHandler = new Handler();
    private long timer = 20; // thread每幾秒run 多久更新一次資料
    private long timerang = 20; // 設定幾秒刷新Mysql
    private Long startTime = System.currentTimeMillis(); // 上回執行thread time
    private Long spentTime;
    //=============SQL Database================================
    private static ContentResolver mContRes;
    //----------------------------
    int DBConnectorError = 0;
    int MyspinnerNo = 0;
    int Spinnersel = 0;

    private String Myid = "0";

    private String Myname = "8號吳典芹";
    private String Myaddress = "24.172127,120.610313";
    private String Mygroup = "3"; //群組
    /***********************************************/
//    private String Selname = "我的位置";
//    private String Seladdress = "24.172127,120.610313";
    private String Selname;
    private String Seladdress;


    private int SQL_count =0;
    private boolean view_open=false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1917);
        checkRequiredPermission(this);
        //-----------------------------------------------
        StrictMode.setThreadPolicy(
                new StrictMode
                        .ThreadPolicy
                        .Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog().build());
        StrictMode.setVmPolicy(
                new StrictMode
                        .VmPolicy
                        .Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build());
        //--------設定MapFragment--------------------------------------------------------------------


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        u_checkgps();  //檢查GPS是否開啟

        // 設定class標題
        Intent intent01 = this.getIntent();
        Myname = intent01.getStringExtra("googlename");

        setupViewComponent();

    }



    private void setupViewComponent()
    {
        mSpnLocation = (Spinner) this.findViewById(R.id.spnLocation);
        mSpnMapType = (Spinner) this.findViewById(R.id.spnMapType);

        txtOutput = (TextView) findViewById(R.id.txtOutput);
        tmsg = (TextView) findViewById(R.id.msg);

        // ----設定control 控制鈕----------
        checkBox = (CheckBox) this.findViewById(R.id.checkcontrol);
        controlScroll = (ScrollView) this.findViewById(R.id.Scroll01);
        checkBox.setOnCheckedChangeListener(chklistener);
        controlScroll.setVisibility(View.INVISIBLE);
        // Parameters: 對應的三個常量值：VISIBLE=0 INVISIBLE=4 GONE=8

        // ---------------
        icosel = 0;  //設定圖示初始值0或1
        // ---------------

        //-------檢查使用者是否存在--------------
        SelectMysql(Myname);
        //-------------------------------------
        // 設定Delay的時間
        mHandler.postDelayed(updateTimer, timer * 1000);
        // -------------------------
        Showspinner(); // 刷新spinner


//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                this, android.R.layout.simple_spinner_item);
//        for (int i = 0; i < locations.length; i++)
//            adapter.add(locations[i][0]);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSpnLocation.setAdapter(adapter);
//
//        // 指定事件處理物件
//        mSpnLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                map.clear();
//                mytrace = null; //清除軌跡圖
//                showloc();
//                setMapLocation();
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//        // ---------------
//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
//        for (int i = 0; i < mapType.length; i++)
//            adapter.add(mapType[i]);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSpnMapType.setAdapter(adapter);
//        //-----------設定ARGB 透明度----
////        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0x00FFFFFF)); //全透明
//        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0xF2FFFFFF)); //50%透明
//        mSpnLocation.setPopupBackgroundDrawable(new ColorDrawable(0xF2FFFFFF)); //50%透明
////        # ARGB依次代表透明度（alpha）、紅色(red)、綠色(green)、藍色(blue)
////        100% — FF       95% — F2        90% — E6        85% — D9
////        80% — CC        75% — BF        70% — B3        65% — A6
////        60% — 99        55% — 8C        50% — 80        45% — 73
////        40% — 66        35% — 59        30% — 4D        25% — 40
////        20% — 33        15% — 26        10% — 1A         5% — 0D         0% — 00
//        //---------------
//        mSpnMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                switch (position) {
//                    case 0:
//                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 道路地圖。
//                        break;
//                    case 1:
//                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星空照圖
//                        break;
//                    case 2:
//                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); // 地形圖
//                        break;
//                    case 3:
//                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID); // 道路地圖混合空照圖
//                        break;
//                    case 4:
//                        map.setTrafficEnabled(true); //開啟路況
//                        break;
//                    case 5:
//                        map.setTrafficEnabled(false); //關閉路況
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });


    }

    private void Showspinner()
    {
        /***************************************
         * 讀取SQLite => Spinner
         *****************************************/
        mContRes = getContentResolver();
        Cursor cur_Spinner =
                mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur_Spinner.moveToFirst();//一定要寫，不然會出錯

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for (int i = 0; i < cur_Spinner.getCount(); i++) {
            cur_Spinner.moveToPosition(i);
            adapter.add(cur_Spinner.getString(1));
        }
        cur_Spinner.close();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnLocation.setAdapter(adapter);
        //指定事件處理物件
        mSpnLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                map.clear();
                mytrace = null;//清除軌跡圖
                showloc();
                setMapLocation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //---------------
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for (int i = 0; i < mapType.length; i++)
            adapter.add(mapType[i]);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnMapType.setAdapter(adapter);


        if (Spinnersel > 0) {
            mSpnLocation.setSelection(Spinnersel, true); //spinner 框架設定
        } else {
            mSpnLocation.setSelection(MyspinnerNo, true); //spinner 框架設定
        }



        //-----------設定ARGB透明度----
        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0xF2FFFFFF));
        mSpnMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//道路地圖。
                        break;
                    case 1:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);//衛星空照圖
                        break;
                    case 2:
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);//地形圖
                        break;
                    case 3:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);//道路地圖混合空照圖
                        break;
                    case 4://開啟路況
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//道路地圖。
                        map.setTrafficEnabled(true);//交通路況圖
                        break;
                    case 5://關閉路況
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//道路地圖。
                        map.setTrafficEnabled(false);//交通路況圖
                        break;
                    case 6://----多景點顯示----------------------
                        if (view_open) {
                            view_open = false;
                            mapType[6]=getString(R.string.mult_view);
                        } else {
                            view_open = true;
                            mapType[6]=getString(R.string.one_view);
                        }
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



    }



    /************************************************
     * SQL Database
     ***********************************************/
    private void SelectMysql(String myname) {
        String selectMYSQL = "";
        String result = "";
        try {
            selectMYSQL = "SELECT name FROM member WHERE name = '" + myname + "' ORDER BY id";
            result = DBConnector.executeQuery(selectMYSQL);

            if (result.length() <= 11) {  //注意長度要改!!!
                /*******************************
                 * 執行InsertMysql()新增個人資料
                 * 也可以直接呼叫DBConnector.executeInsert(a,b,c);
                 *******************************/
                InsertMysql(myname, Mygroup, Myaddress);
            }
            selectMYSQL = "SELECT * FROM member WHERE name = '" + myname + "' ORDER BY id";
            result = DBConnector.executeQuery(selectMYSQL);
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonData = jsonArray.getJSONObject(0);
            Myid = jsonData.getString("id").toString();
            Myname = jsonData.getString("name").toString();
            Mygroup = jsonData.getString("grp").toString();
            Myaddress = jsonData.getString("address").toString();
        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }

    }



    private void InsertMysql(String insmyname, String insmygroup, String insmyaddress) {
        /*********************************
         * 使用 DBConnector的新增函數
         *********************************/
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name",insmyname));
        nameValuePairs.add(new BasicNameValuePair("grp",insmygroup));
        nameValuePairs.add(new BasicNameValuePair("address",insmyaddress));
        String result = DBConnector.executeInsert("",nameValuePairs);

    }



    private void UpdateMysql(String upmyid, String upmyname, String upmygroup, String upmyaddress) {
        /*********************************
         * 使用 DBConnector的mysql_update函數
         *********************************/
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("id", upmyid));
        nameValuePairs.add(new BasicNameValuePair("name", upmyname));
        nameValuePairs.add(new BasicNameValuePair("grp","丙"));  //upmygroup
        nameValuePairs.add(new BasicNameValuePair("address", upmyaddress));
        String result = DBConnector.executeUpdate("", nameValuePairs);
        //--------------------------------------------
    }





    // -----顯示 我的位置座標圖示
    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(((CheckBox) v).isChecked());
        } else {
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
    }




    // -------- 地圖縮放 -------------------------------------------
    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom controls (+/- buttons in the bottom right of
        // the map).
        map.getUiSettings().setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    // ---------------設定指北針----------------------------------------------
    public void setCompassEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the compass (icon in the top left that indicates the
        // orientation of the
        // map).
        map.getUiSettings().setCompassEnabled(((CheckBox) v).isChecked());
    }



    //----可用捲動手勢操控,用手指平移或捲動來拖曳地圖
    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables scroll gestures (i.e. panning the map).
        map.getUiSettings().setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    //----縮放手勢按兩下按一下或兩指拉大拉小----
    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
        map.getUiSettings().setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }
    //----傾斜手勢改變地圖的傾斜角度兩指上下拖曳來增加/減少傾斜角度----
    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables tilt gestures.
        map.getUiSettings().setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }
    //----旋轉手勢兩指旋轉地圖----
    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables rotate gestures.
        map.getUiSettings().setRotateGesturesEnabled(((CheckBox) v).isChecked());

    }




    //-------------監聽改變控制鈕 ------------
    private CheckBox.OnCheckedChangeListener chklistener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (checkBox.isChecked()) {
                controlScroll.setVisibility(View.VISIBLE);
                // Parameters: 對應的三個常量值：VISIBLE=0 INVISIBLE=4 GONE=8
            } else {
                controlScroll.setVisibility(View.INVISIBLE);
            }
        }
    };

    // ---Control 控制項設定--------------------------------
    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();

    }



    /************************************************
     * Thread Hander 固定要執行的方法
     ***********************************************/
    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            spentTime = System.currentTimeMillis() - startTime;
            // startTime = System.currentTimeMillis();
            Long second = (spentTime / 1000);// 將運行時間後，轉換成秒數
            if (second >= timerang) {
                startTime = System.currentTimeMillis();
                dbmysql(); // 匯入database
                Showspinner(); // 刷新spinner
            }
            mHandler.postDelayed(this, timer * 1000);// time轉換成毫秒 updateTime
        }
    };





    private void dbmysql()
    {
        mContRes = getContentResolver();  //抓取SQLite
        // ------------- 先刪除 SQLite 資料------------

        Cursor cur_dbmysql = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur_dbmysql.moveToFirst(); // 一定要寫，不然會出錯

        // ------
        try {
            String result = DBConnector.executeQuery("SELECT * FROM member");
            if (result.length() <= 11) {//php找不到資料會回傳7,所以以7來判斷有沒有找到資料,沒有的話新增

                DBConnectorError++;//連線失敗次數
                if (DBConnectorError > 3)//連線失敗大於3次
                    Toast.makeText(M1917.this, "伺服器狀態異常,請檢查您的網路狀態!", Toast.LENGTH_LONG).show();
                else//連線失敗小於等於3次
                    Toast.makeText(M1917.this, "伺服器嘗試連線中,請稍候!", Toast.LENGTH_LONG).show();
            } else {//php找到資料,刪除SQLite資料
                DBConnectorError = 0;
                Uri uri = FriendsContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null); // 刪除所有資料
            }

            /** SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);  */
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                ContentValues newRow = new ContentValues();
                newRow.put("id", jsonData.getString("id").toString());
                newRow.put("name", jsonData.getString("name").toString());
                newRow.put("grp", jsonData.getString("grp").toString());
                newRow.put("address", jsonData.getString("address").toString());
                // ---------
//                    MyspinnerNo = i; // 儲存會員在spinner 的位置
                mContRes.insert(FriendsContentProvider.CONTENT_URI, newRow);
            }
        } catch (Exception e) {

        }
        cur_dbmysql.close();

    }




    private void setMapLocation()
    {
        if (view_open)         showloc(); //刷新所有景點
        int iSelect = mSpnLocation.getSelectedItemPosition();

        mContRes = getContentResolver();
        Cursor cur_setmap = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null,
                null);

        cur_setmap.moveToPosition(iSelect);
/**************************************
 * id: cur_setmap.getString(0) name: cur_setmap.getString(1) grp:
 * cur_setmap.getString(2) address:cur_setmap.getString(3)
 **************************************/
        Selname = cur_setmap.getString(1);// 地名
        Seladdress = cur_setmap.getString(3);// 緯經
        cur_setmap.close();
        String[] sLocation = Seladdress.split(",");

        double dLat = Double.parseDouble(sLocation[0]); // 南北緯
        double dLon = Double.parseDouble(sLocation[1]); // 東西經
        String vtitle = Selname;//選擇的名字
        //--- 設定所選位置之當地圖示---//
        image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); //使用系統水滴
        //水滴可選擇樣式顏色
        VGPS = new LatLng(dLat, dLon);
        //----設定自訂義infowindow----//
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());//產生一個副calss在本class裡面
        map.setOnMarkerClickListener(this);
        //map.setOnInfoWindowClickListener(this);
        //map.setOnMarkerDragListener(this);
        map.addMarker(new MarkerOptions()
                .position(VGPS)
                .title(vtitle)
                .snippet("座標:" + dLat + "," + dLon)
                .infoWindowAnchor(Anchor_x, Anchor_y)
                .icon(image_des));//顯示圖標文字
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));
        onCameraChange(map.getCameraPosition());//始終把圖放在中間


        map.setOnMyLocationButtonClickListener(this);
    }



    //-----------onCameraChange--------------------------
    private void onCameraChange(CameraPosition cameraPosition) {
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
    }



    private void u_checkgps()
    {
        // 取得系統服務的LocationManager物件
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 顯示對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("定位管理")
                    .setMessage("GPS目前狀態是尚未啟用.\n"
                            + "請問你是否現在就設定啟用GPS?")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 使用Intent物件啟動設定程式來更改GPS設定
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("不啟用", null).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    private void checkRequiredPermission(final Activity activity){
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if(permissionsList.size()!=0){
            ActivityCompat.requestPermissions(activity,permissionsList.toArray(new
                    String[permissionsList.size()]),REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for(int i=0;i<permissions.length;i++){
                    if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getApplicationContext(),permissions[i]+"權限申請成功!",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"權限被拒絕："+permissions[i],Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }





    // ---檢查 Google Map 是否正確開啟
    private boolean checkReady() {
        if (map == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }






    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
//        mUiSettings = map.getUiSettings();//

////        開啟 Google Map 拖曳功能
//        map.getUiSettings().setScrollGesturesEnabled(true);
//
////        右下角的導覽及開啟 Google Map功能
//        map.getUiSettings().setMapToolbarEnabled(true);
//
////        左上角顯示指北針，要兩指旋轉才會出現
//        map.getUiSettings().setCompassEnabled(true);
//
////        右下角顯示縮放按鈕的放大縮小功能
//        map.getUiSettings().setZoomControlsEnabled(true);

        // --------------------------------
        map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));

        //----------取得定位許可------------API 20-----------
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //----顯示我的位置ICO-------
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        } else {

            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(true);
            return;
        }
//-------------------------------------------------

    }





    //------------------------------------
    /**
     * 位置變更狀態監視
     */
    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
            Log.d(TAG, "locationListener->onLocationChanged:" + map.getCameraPosition().zoom + " currentZoom:"
                    + currentZoom);
            tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
            Log.d(TAG, "onProviderDisabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            tmsg.setText("onProviderEnabled");
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v(TAG, "Status Changed: Out of Service");
                    tmsg.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v(TAG, "Status Changed: Temporarily Unavailable");
                    tmsg.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.v(TAG, "Status Changed: Available");
                    tmsg.setText("Available");
                    break;
            }
        }

    };



    private void showloc()
    {
        Cursor cursholoc = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        // 將所有景點位置顯示
        for (int i = 0; i < cursholoc.getCount(); i++) {
            cursholoc.moveToPosition(i);
            String[] sLocation = cursholoc.getString(3).split(",");
            dLat = Double.parseDouble(sLocation[0]); // 南北緯
            dLon = Double.parseDouble(sLocation[1]); // 東西經
            String vtitle = cursholoc.getString(1);
            resID = 0;//從R裡面抓出來的機碼(配置碼)
            resID1 = 0;
            // ---設定所選位置之當地圖片---//
            //drawable目錄下存放q01.png ~ q06.png t01.png ~t07.png 超出範圍用t99.png & q99.png
            if (i >= 0 && i < 7) {
                String idName = "t" + String.format("%02d", i);
                String imgName = "q" + String.format("%02d", i);
                resID = getResources().getIdentifier(idName, "drawable",
                        getPackageName());
                resID1 = getResources().getIdentifier(imgName, "drawable",
                        getPackageName());
                image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
            } else {
                resID = getResources().getIdentifier("t99", "drawable",
                        getPackageName());//超出範圍 用t99.png
                resID1 = getResources().getIdentifier("q99", "drawable",
                        getPackageName());//超出範圍 用q99.png
            }
            // ---設定所選位置之當地圖片---//
            switch (icosel) {
                case 0:
                    image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); // 使用橘色系統水滴
                    Anchor_x = -1.6f;
                    Anchor_y = 1.0f;
                    break;
                case 1:
                    // 運用巨集
                    image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
                    Anchor_x = -0.3f;
                    Anchor_y = 1.0f;
                    break;
            }
            vtitle = vtitle + "#" + resID1;//存放圖片號碼
            VGPS = new LatLng(dLat, dLon);// 更新成欲顯示的地圖座標
            //---根據所選位置項目顯示地圖/標示文字與圖片---//
            map.addMarker(new MarkerOptions()
                            .position(VGPS)
                            .alpha(0.9f)
                            .title(i + "." + vtitle)
                            .snippet("緯度:" + String.valueOf(dLat) + "\n經度:" + String.valueOf(dLon))
                            .infoWindowAnchor(Anchor_x, Anchor_y)//設定圖標的基點位置
                            .icon(image_des)// 顯示圖標文字
//                    .draggable(true)//設定marker可移動
            );
            //--------使用自定義視窗-------------------------
            map.setInfoWindowAdapter(new CustomInfoWindowAdapter());//外圓內方
        }
        cursholoc.close();

    }



    private void nowaddress()  // 取得上次已知的位置
    {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Location location = locationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location);
            return;

        }

// 監聽 GPS Listener----------------------------------
// long minTime = 5000;// ms
// float minDist = 5.0f;// meter
//---網路和GPS來取得定位，因為GPS精準度比網路來的更好，所以先使用網路定位、
// 後續再用GPS定位，如果兩者皆無開啟，則跳無法定位的錯誤訊息
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            tmsg.setText("GPS 未開啟");
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                tmsg.setText("使用網路GPS");
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                tmsg.setText("使用精確GPS");
            }
        }

    }

    private void updateWithNewLocation(Location location)  //左下角
    {
        String where = "";
        if (location != null) {

            double lng = location.getLongitude();// 經度
            double lat = location.getLatitude();// 緯度
            float speed = location.getSpeed();// 速度
            long time = location.getTime();// 時間
            String timeString = getTimeString(time);

            where = "經度: " + lng + "\n緯度: " + lat + "\n速度: " + speed + "\n時間: " + timeString + "\nProvider: "
                    + provider;

            Myaddress = lat + "," + lng;

            /**********************************
             *變更mysql會員的座標
             **********************************/
            UpdateMysql(Myid, Myname, Mygroup, Myaddress);//Myid是MySQL的id不是SQLite的


            // 標記"我的位置"
            showMarkerMe(lat, lng);
            cameraFocusOnMe(lat, lng);
            trackMe(lat, lng);// 軌跡圖

        } else {
            where = "*位置訊號消失*";
        }

        // 位置改變顯示
        txtOutput.setText(where);

    }



    private void trackMe(double lat, double lng)
    {
        if (mytrace == null) {
            mytrace = new ArrayList<LatLng>();
        }
        mytrace.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : mytrace) {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(Color.RED); // 軌跡顏色
        Polyline line = map.addPolyline(polylineOpt);
        line.setWidth(15); // 軌跡寬度
        line.setPoints(mytrace);

    }


    private String getTimeString(long timeInMilliseconds)  //可寫在library
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);

    }



    private void cameraFocusOnMe(double lat, double lng)
    {
        CameraPosition camPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(map.getCameraPosition().zoom).build();
        /* 移動地圖鏡頭 */
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        Log.d(TAG, "cameraFocusOnMe");
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);

    }




    /*** 顯示目前位置 */
    private void showMarkerMe(double lat, double lng) {

        if (markerMe != null) {
            markerMe.remove();
        }
//------------------
        int resID = getResources().getIdentifier("q00", "drawable", getPackageName());
//-------------------------
        if (icosel != 0) {
            image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
        } else {
            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE); // 使用系統水滴

        }
//-------------------------
        dLat = lat; // 南北緯
        dLon = lng; // 東西經

        String vtitle = "GPS位置:" + "#" + resID;
        String vsnippet = "座標:" + String.valueOf(dLat) + "," + String.valueOf(dLon);

        VGPS = new LatLng(lat, lng);// 更新成欲顯示的地圖座標
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title(vtitle);
        markerOpt.snippet(vsnippet);
        markerOpt.infoWindowAnchor(0.5f, 0.9f);
        markerOpt.draggable(true);
        markerOpt.icon(image_des);
        markerMe = map.addMarker(markerOpt);
//----------------------------
//        locations[0][1] = lat + "," + lng;


    }



    @Override
    public boolean onMyLocationButtonClick()
    {
        Toast.makeText(getApplicationContext(), "返回GPS目前位置", Toast.LENGTH_LONG).show();
        return true;

    }


    /* 檢查GPS 是否開啟 */
    private boolean initLocationProvider()
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }
        return false;

    }



    // -----------------------------------------------------
    //*** 增加 Maker 監聽 使用動畫*/ 按水滴額外多做事情
    @Override
    public boolean onMarkerClick(final Marker marker_Animation) {
        Log.d(TAG, "onMarkerClick:" + marker_Animation.getTitle()); //
        if (!marker_Animation.getTitle().substring(0, 4).equals("Move")) {
            // 設定動畫
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;//連續時間

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker_Animation.setAnchor(0.5f, 1.0f + 2 * t); //設定標的位置

                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        } else {
            M1917.this.markerMe.hideInfoWindow();
        }
        return false;
    }


    //------------生命週期------------
    @Override
    protected void onStart()
    {
        super.onStart();
        if (initLocationProvider()) {
            nowaddress();
        } else {
            txtOutput.setText("GPS未開啟,請先開啟定位！");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }
    //-----------------



    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }



    //====================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m1917, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.item1:
                map.clear();  //歸零
                if (icosel < 1) {
                    icosel = 1; //用照片顯示
                    showloc();
                } else
                    icosel = 0; //用水滴顯示
                showloc();
                break;
            case R.id.item3:
                //----
//                LatLng VGPS = new LatLng(24.1423715,120.660715);//台中歌劇院
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(VGPS)//目標 抓spinner景點
                        .zoom(17)       //縮放 1：世界 5：地塊/大陸 10：城市 15：街道 20：建築物
                        .bearing(80)   //0:北 45:西北 90:西 135:西南 180:南 225:東南 270:東 315:東北
                        .tilt(5)       //傾斜度（檢視角度）0-90 0:正上方(0~15 最佳)
                        .build();       // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                map.setBuildingsEnabled(true);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 衛星空照圖
                //----
                break;

            case R.id.item4:
                //----
                LatLng VGPS_3D = new LatLng(35.6298079,139.8805474);//東京迪士尼 大阪城34.687404, 135.525763
                CameraPosition cameraPosition1 = new CameraPosition.Builder()
                        .target(VGPS_3D)//目標
                        .zoom(17)       //縮放 1：世界 5：地塊/大陸 10：城市 15：街道 20：建築物
                        .bearing(80)   //0:北 45:西北 90:西 135:西南 180:南 225:東南 270:東 315:東北
                        .tilt(5)       //傾斜度（檢視角度）0-90 0:正上方(0~15 最佳)
                        .build();       // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
                map.setBuildingsEnabled(true);
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星空照圖
                //----
                break;

            case R.id.action_settings:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);

    }



    //---------------------------------------------自訂義infowindow inner class---------------------------------------------------
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        @Override
        public View getInfoWindow(Marker marker)
        {
            // 依指定layout檔，建立地標訊息視窗View物件
            // --------------------------------------------------------------------------------------
            // 單一框
            // View infoWindow=
            // getLayoutInflater().inflate(R.layout.custom_info_window,
            // null);
            // 有指示的外框
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null);
            infoWindow.setAlpha(0.5f);
            // ----------------------------------------------
            // 顯示地標title
            TextView title = ((TextView) infoWindow.findViewById(R.id.title));
            String[] ss = marker.getTitle().split("#");
            title.setText(ss[0]);
            // 顯示地標snippet
            TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
            snippet.setText(marker.getSnippet());
            // 顯示圖片
            ImageView imageview = ((ImageView) infoWindow.findViewById(R.id.content_ico));
            imageview.setImageResource(Integer.parseInt(ss[1]));

            return infoWindow;

        }

        @Override
        public View getInfoContents(Marker marker)
        {
            Toast.makeText(getApplicationContext(), "getInfoContents", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.toast_back),Toast.LENGTH_SHORT).show();
    }



}
