package edu.doggy228.antoxawallet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class PayCheckActivity extends AppCompatActivity implements LoyaltyCardReader.PayCheckCallback {
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private Button btnPay;
    public LoyaltyCardReader mLoyaltyCardReader;
    private boolean readerMode;
    public TextView cOperAmountRest;
    public Button cButtonPay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mLoyaltyCardReader = new LoyaltyCardReader(this);
        disableReaderMode();
        LinearLayout layoutPayCheck = new LinearLayout(this);
        layoutPayCheck.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(this);
        textView.setText("Піднесіть телефон до терміналу.");
        layoutPayCheck.addView(textView);
        setContentView(layoutPayCheck);
    }
    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }
    private void enableReaderMode() {
        readerMode = false;
        Log.i("PayCheckActivity", "Enabling reader mode");
        Activity activity = this;
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.enableReaderMode(activity, mLoyaltyCardReader, READER_FLAGS, null);
            readerMode = true;
        }
    }

    private void disableReaderMode() {
        readerMode = false;
        Log.i("PayCheckActivity", "Disabling reader mode");
        Activity activity = this;
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.disableReaderMode(activity);
        }
    }

    @Override
    public void onPayCheckReceived(final String payload) {
        StringTokenizer st = new StringTokenizer(payload,":");
        if (st.countTokens() != 3 || !st.nextToken().equals("pb")){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorPayCheck("Термінал не підтримується.");
                }
            });
            return;
        }
        String loyaltySystemId = st.nextToken();
        String operAmount = st.nextToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + AttrStorage.serverNameGet(this) + ":8083/wms/api/v1/wms/wallet/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        WalletService service = retrofit.create(WalletService.class);
        ReqOperPayCheck req = new ReqOperPayCheck();
        req.loyaltySystemId = loyaltySystemId;
        req.bonusAmountInMax = "999999";
        try {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_SMS,
                                Manifest.permission.READ_PHONE_NUMBERS,
                                Manifest.permission.READ_PHONE_STATE},
                        MainActivity.REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            String tel = tMgr.getLine1Number();
            if (tel.startsWith("+")) tel = tel.substring(1);
            while(tel.length()<12) tel = "0"+tel;
            Response<JsonNode> rsp = service.operPayCheck("Bearer wu:"+tel,req).execute();
            if(rsp.code()!=200) throw new Exception("Помилка мережі:"+rsp.code()+" "+rsp.errorBody());
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showOperPayCheck(operAmount,rsp.body());
                }
            });

        } catch (Exception e) {
            Log.e("test", "Помилка мережі", e);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorNetwork("Помилка мережі");

                }
            });
        }
    }
    public void showErrorPayCheck(String msg){
        LinearLayout layoutPayCheck = new LinearLayout(this);
        layoutPayCheck.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(this);
        textView.setText(msg);
        layoutPayCheck.addView(textView);
        setContentView(layoutPayCheck);
    }
    public void showErrorNetwork(String msg){
        LinearLayout layoutPayCheck = new LinearLayout(this);
        layoutPayCheck.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(this);
        textView.setText(msg);
        layoutPayCheck.addView(textView);
        setContentView(layoutPayCheck);
    }
    public void showOperPayCheck(String operAmount,JsonNode rspRoot){
        String loyaltySystemId = rspRoot.path("loyaltySystemId").asText();
        LinearLayout layoutPayCheck = new LinearLayout(this);
        layoutPayCheck.setOrientation(LinearLayout.VERTICAL);
        ImageView imageViewLogo = new ImageView(this);
        int resId = getResources().getIdentifier("ls_"+loyaltySystemId, "drawable", "edu.doggy228.antoxawallet");
        if (resId <= 0) resId = getResources().getIdentifier("ls_def", "drawable", "edu.doggy228.antoxawallet");
        imageViewLogo.setImageResource(resId);
        layoutPayCheck.addView(imageViewLogo);
        TextView textViewOperAmount = new TextView(this);
        textViewOperAmount.setText("Сума чеку: "+operAmount+" грн");
        layoutPayCheck.addView(textViewOperAmount);
        TableLayout layoutList = new TableLayout(this);
        List<PayCheckUserPayInfo> payCheckUserPayInfos = new ArrayList<>();
        Iterator<JsonNode> it = rspRoot.path("listLoyaltyUserPayInfo").elements();
        while(it.hasNext()){
            JsonNode loyaltyUserPayInfo = it.next();
            PayCheckUserPayInfo payCheckUserPayInfo = new PayCheckUserPayInfo(this,loyaltyUserPayInfo);
            payCheckUserPayInfos.add(payCheckUserPayInfo);
            layoutList.addView(payCheckUserPayInfo.tableRow);
        }
        layoutPayCheck.addView(layoutList);
        cOperAmountRest = new TextView(this);
        cOperAmountRest.setText("Сума до сплати: "+operAmount+" грн");
        layoutPayCheck.addView(cOperAmountRest);
        cButtonPay = new Button(this);
        cButtonPay.setText("Сплатити");
        layoutPayCheck.addView(cButtonPay);
        setContentView(layoutPayCheck);
    }
}
