package edu.doggy228.antoxawallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity extends AppCompatActivity  {
    final public static int REQUEST_CODE_ASK_PERMISSIONS = 123;
    public static final String TAG = "CardReaderFragment";
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).

    private TextView tfPaymentBill;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageViewLogo = new ImageView(this);
        imageViewLogo.setImageResource(R.drawable.logoantoxawallet);
        linearLayout.addView(imageViewLogo);
        TextView textViewTitleServer = new TextView(this);
        textViewTitleServer.setText("Вкажіть сервер систем лояльності:");
        linearLayout.addView(textViewTitleServer);
        EditText editTextServerName = new EditText(this);
        editTextServerName.setText(AttrStorage.serverNameGet(this));
        linearLayout.addView(editTextServerName);
        Button btnStart = new Button(this);
        btnStart.setText("Запуск");
        linearLayout.addView(btnStart);
        TextView textViewError = new TextView(this);
        textViewError.setText("");
        linearLayout.addView(textViewError);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttrStorage.serverNameSet(MainActivity.this,editTextServerName.getText().toString());
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://" + AttrStorage.serverNameGet(MainActivity.this) + ":8083/wms/api/v1/wms/wallet/")
                        .addConverterFactory(JacksonConverterFactory.create())
                        .build();
                Log.i("test","Url = "+retrofit.baseUrl());
                WalletService service = retrofit.create(WalletService.class);
                try {
                    TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.READ_SMS,
                                        Manifest.permission.READ_PHONE_NUMBERS,
                                        Manifest.permission.READ_PHONE_STATE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                    String tel = tMgr.getLine1Number();
                    if (tel.startsWith("+")) tel = tel.substring(1);
                    while(tel.length()<12) tel = "0"+tel;
                    //tel = "380684976787";
                    Log.i("test","tel="+tel);
                    Response<JsonNode> rsp = service.walletUser("Bearer wu:"+tel).execute();
                    //Response<JsonNode> rsp = service.walletUser().execute();
                    if(rsp.code()!=200) throw new Exception("Помилка мережі:"+rsp.code()+" "+rsp.errorBody());
                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    tfPaymentBill = new TextView(MainActivity.this);
                    tfPaymentBill.setText("NFC вимкнуто");
                    layout.addView(tfPaymentBill);
                    Iterator<JsonNode> it = rsp.body().path("listLoyaltyUser").elements();
                    while(it.hasNext()){
                        JsonNode loyaltySystem = it.next();
                        Button btn = new Button(MainActivity.this);
                        btn.setText(loyaltySystem.path("loyaltySystemName").asText());
                        layout.addView(btn);
                    }
                    setContentView(layout);
                } catch (Exception e) {
                    Log.e("test", "Помилка мережі", e);
                    textViewError.setText("Error");
                }
            }
        });
        setContentView(linearLayout);
    }



    public void onClickMainStartButton(View view) {

    }
}