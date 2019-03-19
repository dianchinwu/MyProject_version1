package tw.tcnr08.a0001;

import android.content.Intent;
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
    private Button b01,b02,b03;

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
        b01.setOnClickListener(this);
        b02.setOnClickListener(this);
        b03.setOnClickListener(this);
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
                Intent it_b = new Intent(Index.this,Main.class);
                startActivity(it_b);
                break;
            case R.id.a0001_b03:
                Intent it_c = new Intent(Index.this, M1602.class);
                startActivity(it_c);
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
