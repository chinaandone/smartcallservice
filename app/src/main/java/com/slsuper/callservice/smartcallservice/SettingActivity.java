package com.slsuper.callservice.smartcallservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.slsuper.callservice.utils.ConstValue;
import com.slsuper.callservice.utils.SettingConfig;

public class SettingActivity extends Activity {

    private Button btnConfirm,btnCancel;
    private TextView tvServerHost,tvWatchID;
    private SettingConfig settingConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        btnConfirm = (Button)findViewById(R.id.settingconfirm);
        btnCancel = (Button)findViewById(R.id.settingcancel);
        tvServerHost = (EditText)findViewById(R.id.serverhost);
        tvWatchID = (EditText)findViewById(R.id.watchid);

        initSettingInfo();
        initListener();
    }

    private void initSettingInfo(){
        settingConfig = new SettingConfig(this);
        String serverHost = settingConfig.getServerHost();
        String watchID = settingConfig.getWatchID();
        if(serverHost!=null && watchID!=null){
            tvServerHost.setText(serverHost);
            tvWatchID.setText(watchID);
        }
    }
    private void initListener(){
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSettings()){
                    settingConfig.SetServerHost(tvServerHost.getText().toString());
                    settingConfig.SetWatchID(tvWatchID.getText().toString());
                    Intent intent = getIntent();
                    intent.putExtra(ConstValue.SERVERHOSTKEY,tvServerHost.getText().toString());
                    intent.putExtra(ConstValue.WATCHIDKEY,tvWatchID.getText().toString());
                    SettingActivity.this.setResult(0x02,intent);
                    SettingActivity.this.finish();
                }else{
                    Toast.makeText(SettingActivity.this,"设置不正确",Toast.LENGTH_LONG);
                    return;
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity.this.setResult(0x03);
                SettingActivity.this.finish();
            }
        });
    }

    private boolean checkSettings(){
        String serverHost = tvServerHost.getText().toString();
        String watchID = tvWatchID.getText().toString();
        if(serverHost==null || watchID==null){
            return false;
        }else{
            String[] ip = serverHost.split("\\.");
            if(ip.length!=4){
                return false;
            }else{
                for(int i=0;i<ip.length;i++){
                    int ipi = Integer.parseInt(ip[i]);
                    if (ipi < 0 || ipi > 255) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
