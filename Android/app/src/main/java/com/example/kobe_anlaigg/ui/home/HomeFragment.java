package com.example.kobe_anlaigg.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.kobe_anlaigg.databinding.FragmentHomeBinding;
import com.example.kobe_anlaigg.utils.MyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private JSONArray statusesOther = null;
    private JSONArray statusesMe = null;
    private int role = 1;
    private String name;
    private TextView textView1;
    private TextView textView2;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if(role==1)
        {
            name = "小埋";
        }
        else
        {
            name = "坤坤";
        }

        textView1 = binding.identity;
        textView1.setText("我是："+name);

        textView2 = binding.state;
        try {
            updateStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Spinner spinner = binding.chooseState;
        try {
            String recentState=statusesMe.getJSONObject(statusesMe.length()-1).optString("status");
            for(int i=0;i<spinner.getCount();i++)
            {
                if(recentState.equals(spinner.getItemAtPosition(i)))
                {
                    spinner.setSelection(i,false);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)adapterView.getItemAtPosition(i);
                JSONObject newStatus = new JSONObject();
                Calendar calendar = Calendar.getInstance();
                int minutes = calendar.get(Calendar.MINUTE)+calendar.get(Calendar.HOUR_OF_DAY)*60;
                MyRequest request = new MyRequest();
                try {
                    newStatus.put("id",String.valueOf(role));
                    newStatus.put("status",item);
                    newStatus.put("time",String.valueOf(minutes));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    request.post("http://47.108.160.172:7002/setStatus",newStatus.toString(),"");
                    updateStatus();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void updateStatus() throws Exception {
        if(role==1)
        {
            MyRequest request = new MyRequest();
            String responseData=new String(request.get("http://47.108.160.172:7002/getStatus1"), StandardCharsets.UTF_8);
            statusesMe=new JSONArray(responseData);
            responseData=new String(request.get("http://47.108.160.172:7002/getStatus2"), StandardCharsets.UTF_8);
            statusesOther=new JSONArray(responseData);
        }
        else
        {
            MyRequest request = new MyRequest();
            String responseData=new String(request.get("http://47.108.160.172:7002/getStatus1"), StandardCharsets.UTF_8);
            statusesOther=new JSONArray(responseData);
            responseData=new String(request.get("http://47.108.160.172:7002/getStatus2"), StandardCharsets.UTF_8);
            statusesMe=new JSONArray(responseData);
        }
        String status = statusesOther.getJSONObject(statusesOther.length() - 1).optString("status");
        int time = statusesOther.getJSONObject(statusesOther.length() - 1).optInt("time");
        String hour=String.valueOf(time/60);
        if(hour.length()==1) hour="0"+hour;
        String minute=String.valueOf(time%60);
        if(minute.length()==1) minute="0"+minute;
        String dayTime=String.valueOf(hour)+":"+String.valueOf(minute);
        if(role==1) textView2.setText("坤坤在" + dayTime + "开始" + status + "了！");
        else textView2.setText("小埋在" + dayTime + "开始" + status + "了！");
    }
}
