package com.beta.autobookkeeping.activity.settings.items;

import static Util.ConstVariable.IP;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;

import com.beta.autobookkeeping.R;
import com.beta.autobookkeeping.activity.settings.SettingsActivity;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Util.ProjectUtil;
import Util.SpUtils;
import Util.StringUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BankNumbers {
    private String bankNumbers;
    private List<String> bankNumbersList;
    private Context context;

    public BankNumbers(String bankNumbers,Context context) {
        this.bankNumbers = bankNumbers;
        this.context = context;
        this.bankNumbersList = StringUtil.string2List(bankNumbers);
    }

    public List<LinearLayout> getBankNumbersViews(){
        List<LinearLayout> bankNumbersViews = new ArrayList<>();
        for (int i = 0;i < bankNumbersList.size();i++){
            bankNumbersViews.add(getBankNumberView(bankNumbersList.get(i)));
        }
        return bankNumbersViews;
    }

    //设置一个view
    public LinearLayout getBankNumberView(String bankNumber){
        LinearLayout bankNumberView = (LinearLayout) View.inflate(context, R.layout.item_btn,null);
        QMUIRoundButton btn = bankNumberView.findViewById(R.id.btn_bank_number);

        btn.setText(bankNumber);

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                List<String> bankNumbers = StringUtil.string2List((String) SpUtils.get(context,"bankNumbers",""));
                bankNumbers.remove(bankNumber);
                StyledDialog.buildIosAlert("删除号码", "是否确定删除该银行号码?", new MyDialogListener() {
                    @Override
                    public void onFirst() {
                        StyledDialog.buildLoading().show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String url = IP+"/user/modifyBankNumber";
                                OkHttpClient client = new OkHttpClient();
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("phoneNum", SpUtils.get(context,"phoneNum",""));
                                    jsonObject.put("bankNumbers",StringUtil.list2String(bankNumbers));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
                                Request request = new Request.Builder().url(url).put(body).build();
                                try{
                                    Response response = client.newCall(request).execute();
                                    if(response.code()==200){
                                        JSONObject jsonResponse = new JSONObject(response.body().string());
                                        if(jsonResponse.getBoolean("success")){
                                            afterModifyBankNumber(StringUtil.list2String(bankNumbers));
                                        }
                                        else{
                                            Looper.prepare();
                                            StyledDialog.dismissLoading((Activity) context);
                                            ProjectUtil.toastMsg(context,jsonResponse.getString("message"));
                                            Looper.loop();
                                        }
                                    }
                                    else{
                                        Looper.prepare();
                                        StyledDialog.dismissLoading((Activity) context);
                                        ProjectUtil.toastMsg(context,"服务器出错");
                                        Looper.loop();
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    @Override
                    public void onSecond() {}
                }).show();
                return false;
            }
        });
        return bankNumberView;
    }

    private void afterModifyBankNumber(String newBankNumber){
        SettingsActivity activity = (SettingsActivity) context;
        activity.afterModifyBankNumber(newBankNumber);
    }

    public String getBankNumbers() {
        return bankNumbers;
    }

    public void setBankNumbers(String bankNumbers) {
        this.bankNumbers = bankNumbers;
        this.bankNumbersList = StringUtil.string2List(bankNumbers);
    }
}
