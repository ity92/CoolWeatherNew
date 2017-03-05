package com.example.cj.coolweathernew;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cj.coolweathernew.db.City;
import com.example.cj.coolweathernew.db.County;
import com.example.cj.coolweathernew.db.Province;
import com.example.cj.coolweathernew.util.HttpUtil;
import com.example.cj.coolweathernew.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by cj on 2017/3/4.
 */

public class ChooseAreaFragment extends Fragment {
    public static final  int LEVEL_PROVINCE=0;
    public static final  int LEVEL_CITY=1;
    public static final  int LEVEL_COUNTY=2;
    private Button backButton;
    private TextView titleText;
    private ListView listView;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<String>();
    private List<Province>provcinceList;
    private List<City> cityList;
    private List<County>countyList;
    private Province selectedprovince;
    private City selectedcity;
    private  int currentlevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton= (Button) view.findViewById(R.id.back_button);
        listView= (ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                if(currentlevel==LEVEL_PROVINCE){
                    selectedprovince=provcinceList.get(postion);
                    queryCities();
                }else if(currentlevel==LEVEL_CITY){
                    selectedcity=cityList.get(postion);
                    queryCounties();
                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentlevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentlevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    //查询所有省份信息，先数据库，后服务器
     private void queryProvinces(){
         titleText.setText("中国");
         backButton.setVisibility(View.GONE);
         provcinceList= DataSupport.findAll(Province.class);
         if(provcinceList.size()>0){
             datalist.clear();
             for (Province province :provcinceList){
                 datalist.add(province.getProvinceName());
             }
              adapter.notifyDataSetChanged();
              listView.setSelection(0);
             currentlevel=LEVEL_PROVINCE;
         }else{
               String address="http://guolin.tech/api/china";
             queryFromServer(address,"province");
         }

     }

    private  void queryCities(){
        titleText.setText(selectedprovince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId=?",String.valueOf(selectedprovince.getId())).find(City.class);
          if(cityList.size()>0){
              datalist.clear();
              for(City city :cityList){
                    datalist.add(city.getCityName());
              }
              adapter.notifyDataSetChanged();
              listView.setSelection(0);
              currentlevel=LEVEL_CITY;

          }else{
              int provinceCode=selectedprovince.getProvinceCode();
              String address="http://guolin.tech/api/china"+provinceCode;
              queryFromServer(address,"city");

          }
    }
    private  void queryCounties(){
        titleText.setText(selectedcity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=?",String.valueOf(selectedcity.getId())).find(County.class);
        if(countyList.size()>0){
            datalist.clear();
            for(County county :countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentlevel=LEVEL_COUNTY;
        }else{
            int provinceCode=selectedprovince.getProvinceCode();
            int cityCode=selectedcity.getCityCode();
            String address="http://guolin.tech/api/china"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");

        }

    }

    private  void queryFromServer(String address,final String type){
        HttpUtil.sendOkHttpRequst(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
               getActivity().runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       closeProgressDialog();
                       Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                   }
               });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                showProgressDialog();
               String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedprovince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedcity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }
    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
