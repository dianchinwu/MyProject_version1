package tw.tcnr08.a0001;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Index extends AppCompatActivity implements View.OnClickListener
{
    private Button b01,b02,b03,b04,b05,b06;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        setupViewComponent();
    }

    private void setupViewComponent()
    {
        b01 = (Button)findViewById(R.id.a0001_b01);
        b02 = (Button)findViewById(R.id.a0001_b02);
        b03 = (Button)findViewById(R.id.a0001_b03);
        b04 = (Button)findViewById(R.id.a0001_b04);
        b05 = (Button)findViewById(R.id.a0001_b05);
        b06 = (Button)findViewById(R.id.a0001_b06);
        b01.setOnClickListener(this);
        b02.setOnClickListener(this);
        b03.setOnClickListener(this);
        b04.setOnClickListener(this);
        b05.setOnClickListener(this);
        b06.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()){
            case R.id.a0001_b01:
                Intent it_a = new Intent(Index.this,M0703.class);
                startActivity(it_a);
                break;
            case R.id.a0001_b02:
                Intent it_b = new Intent(Index.this,M1421.class);
                startActivity(it_b);
                break;
            case R.id.a0001_b03:
                Intent it_c = new Intent(Index.this, M1602.class);
                startActivity(it_c);
                break;
            case R.id.a0001_b04:
                Intent it_d = new Intent(Index.this, M1901.class);
                startActivity(it_d);
                break;
            case R.id.a0001_b05:
                Uri uri_e = Uri.parse("https://tcnr1608.000webhostapp.com/allschoolthings/jqm/index.php");
                Intent it_e = new Intent(Intent.ACTION_VIEW,uri_e);
                startActivity(it_e);
                break;
            case R.id.a0001_b06:
                Uri uri_f = Uri.parse("https://github.com/dianchinwu/MyProject_version1/tree/master/1421a_MySQL_Final/src/main/java/tw/tcnr08/a0001");
                Intent it_f = new Intent(Intent.ACTION_VIEW,uri_f);
                startActivity(it_f);
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.toast_close),Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.action_settings:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}
