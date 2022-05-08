package edu.doggy228.antoxawallet;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;

public class PayCheckUserPayInfo {
    public PayCheckActivity activity;
    public JsonNode jsonNode;
    public TableRow tableRow;
    public CheckBox cUserPayInfo;
    public TextView cBonusAmountIn;
    public PayCheckUserPayInfo(PayCheckActivity activity, JsonNode jsonNode){
        this.activity = activity;
        this.jsonNode = jsonNode;
        this.tableRow = new TableRow(activity);
        cUserPayInfo = new CheckBox(activity);
        cUserPayInfo.setText(jsonNode.path("loyaltySystemName").asText());
        cUserPayInfo.setChecked(jsonNode.path("localTrans").asBoolean());
        tableRow.addView(cUserPayInfo);
        TextView cBalanceAmount = new TextView(activity);
        cBalanceAmount.setText(jsonNode.path("balanceAmount").asText());
        tableRow.addView(cBalanceAmount);
        cBonusAmountIn = new TextView(activity);
        cBonusAmountIn.setText(jsonNode.path("bonusAmountInMax").asText());
        tableRow.addView(cBonusAmountIn);
    }
}
