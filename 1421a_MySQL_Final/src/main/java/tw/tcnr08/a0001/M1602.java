package tw.tcnr08.a0001;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.tcnr08.a0001.R;

public class M1602 extends AppCompatActivity
{

    private ListView list001;
    private TableRow tab01;
    private String check_t="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1602);
        setupViewComponent();
        //gradle加上    implementation 'org.jbundle.util.osgi.wrapped:org.jbundle.util.osgi.wrapped.org.apache.http.client:4.1.2'
        //manifests加上<uses-permission android:name="android.permission.INTERNET"></uses-permission>
    }

    private void setupViewComponent()
    {
        // 動態調整高度 抓取使用裝置尺寸
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int newscrollheight = displayMetrics.heightPixels * 90 / 100; // 設定ScrollView使用尺寸的4/5

        //--設定listview 抬頭--
//        t_title = (TextView)findViewById(R.id.t_title);
//        t_title.setText(getString(R.string.t001)+" "
//                +getString(R.string.t002)+"  "
//                +getString(R.string.t003)+"  "
//                +getString(R.string.t004 )+"                  "
//                +getString(R.string.t005 )     );

        //---

        list001 = (ListView) findViewById(R.id.listView1);
        list001.getLayoutParams().height = newscrollheight;
        list001.setLayoutParams(list001.getLayoutParams()); // 重定ScrollView大小
//        tab01 = (TableRow)findViewById(R.id.tab01);

        try
        {
            String Task_opendata = new TransTask().execute("http://opendata.epa.gov.tw/ws/Data/AQI/?$format=json").get();
            //===== 設定 opendata 網址===============
//                        [{"AQI":"19",
//                        "CO":"0.17",
//                        "CO_8hr":"0.7",
//                        "County":"基隆市",
//                        "NO":"2.3",
//                        "NO2":"3.4",
//                        "NOx":"5.6",
//                        "O3":"43",
//                        "O3_8hr":"20",
//                        "PM10":"13",
//                        "PM10_AVG":"13",
//                        "PM2.5":"5",
//                        "PM2.5_AVG":"6",
//                        "Pollutant":"",
//                        "PublishTime":"2018-01-15 13:00",
//                        "SiteName":"基隆",
//                        "SO2":"1.7",
//                        "Status":"良好",
//                        "WindDirec":"78",
//                        "WindSpeed":"3.5"},.....
            List<Map<String, Object>> mList;
            mList = new ArrayList<Map<String, Object>>();

            JSONArray jsonArray = new JSONArray(Task_opendata);
            jsonArray = sortJsonArray( jsonArray);  //新增排序方法

            //----+表頭---------
//            tab01.setVisibility(View.GONE);
//            Map<String, Object> item_title = new HashMap<String, Object>();
//            item_title.put("County", "縣市");
//            item_title.put("SiteName", "觀測點");
//            item_title.put("Pm25", "PM25");
//            item_title.put("Status", "狀態");
//            item_title.put("PublishTime", "更新時間");
//            mList.add(item_title);

            for(int i =0 ; i<jsonArray.length() ; i++){
                JSONObject jsonData = jsonArray.getJSONObject(i);
                Map<String, Object> item = new HashMap<String, Object>();
                String County = jsonData.getString("County");
                String SiteName = jsonData.getString("SiteName");
                String Pm25 = jsonData.getString("PM2.5_AVG");
                String Status = jsonData.getString("Status");
                String PublishTime = jsonData.getString("PublishTime");
                //check_t 初始值為空白,將重複縣市名稱改為 .. 顯示
                if (County.equals(check_t)) {
                    County = "..";
                } else {
                    check_t = County;
                }

                item.put("County", County);
                item.put("SiteName", SiteName);
                item.put("Pm25", Pm25);
                item.put("Status", Status);
                item.put("PublishTime", PublishTime);
                mList.add(item);
            }

 //=========設定listview========
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    mList,
                    R.layout.list,
                    new String[]{"County", "SiteName", "Pm25", "Status", "PublishTime"},
                    new int[]{R.id.t001, R.id.t002, R.id.t003, R.id.t004, R.id.t005}
            );
            list001.setAdapter(adapter);
//------------------------

            //設定list內的PM2.5的文字顏色和背景顏色,listview內容細部修改
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation)
                {
                    if (view.getId() == R.id.t003) {
                        //PM2.5
                        TextView PM25 = (TextView) view;
                        PM25.setText(data.toString());
                        if (checkIfDataIsInt(data.toString())) {
                            int PM25num = Integer.parseInt(data.toString());
                            //-判斷細懸浮微粒(PM2.5)指標對照
                            if (PM25num < 11) {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Green));  //lightgreen
                            } else if(PM25num >= 11 &&PM25num < 23) {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.mediumaquamarine));
                            } else if (PM25num >= 23 &&PM25num < 35) {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Teal));  //olivedrab
                            } else if (PM25num >= 36 && PM25num <= 41) {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Yellow));
                            } else if (PM25num >= 42 && PM25num <= 47) {
                                PM25.setTextColor(getResources().getColor(R.color.Blue));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Gold));
                            } else if (PM25num >= 48 && PM25num <= 53) {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Red));
                            } else if (PM25num >= 54 && PM25num <= 70) {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Firebrick));
                            } else if (PM25num > 70) {
                                PM25.setTextColor(getResources().getColor(R.color.Red));
                                PM25.setBackgroundColor(getResources().getColor(R.color.Mediumorchid));
                            } else {
                                PM25.setTextColor(getResources().getColor(R.color.Black));
                                PM25.setBackgroundColor(getResources().getColor(R.color.White));
                            }
                        }
////--Title------------------------
//                        if (view.getId() == R.id.t001) {
//                            TextView County_t = (TextView) view;
//                            County_t.setText(data.toString());
//                            if (County_t.getText().toString().equals("縣市")) {
//                                County_t.setBackgroundColor(getResources().getColor(R.color.Silver));
//                            }
//                        }
//                        if (view.getId() == R.id.t002) {
//                            TextView SiteName_t = (TextView) view;
//                            SiteName_t.setText(data.toString());
//                            if (SiteName_t.getText().toString().equals("觀測點")) {
//                                SiteName_t.setBackgroundColor(getResources().getColor(R.color.Silver));
//                            }
//                        }
//                        if (view.getId() == R.id.t003) {
//                            TextView Pm25_t = (TextView) view;
//                            Pm25_t.setText(data.toString());
//                            if (data.toString().equals("PM25")) {
//                                Pm25_t.setBackgroundColor(getResources().getColor(R.color.Silver));
//                            }
//                        }
//                        if (view.getId() == R.id.t004) {
//                            TextView Status_t = (TextView) view;
//                            Status_t.setText(data.toString());
//                            if (data.toString().equals("狀態")) {
//                                Status_t.setBackgroundColor(getResources().getColor(R.color.Silver));
//                            }
//                        }
//                        if (view.getId() == R.id.t005) {
//                            TextView PublishTime_t = (TextView) view;
//                            PublishTime_t.setText(data.toString());
//                            if (data.toString().equals("更新時間")) {
//                                PublishTime_t.setBackgroundColor(getResources().getColor(R.color.Silver));
//                            }
//                        }

////--------------------------
                    }
                    return false;
                }
            });


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkIfDataIsInt(String data) {
        if (data.equals("") || data.isEmpty()) {
            return false;
        } else {
            try {
                int i = Integer.parseInt(data);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private JSONArray sortJsonArray(JSONArray array)
    {
        ArrayList<JSONObject> jsons = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                jsons.add(array.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(jsons, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject t1, JSONObject t2) {
                //可多重排序
                String lid = "";  //左邊代表尚未用縣市排序
                String rid = "";  //右邊代表完成用縣市排序
                try {
                    lid = t1.getString("County");
                    rid = t2.getString("County");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return lid.compareTo(rid);
            }
        });

        return new JSONArray(jsons);
    }


    //===inner class===========================================
    //====================================================
    private class TransTask extends AsyncTask<String, Void, String> {
        String ans;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while (line != null) {
                    Log.d("HTTP", line);
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ans = sb.toString();
            //------------
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("s", "s:" + s);
            parseJson(s);
        }

        private void parseJson(String s) {

        }
    }


    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.toast_back),Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.mainsub,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.m_return:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}
