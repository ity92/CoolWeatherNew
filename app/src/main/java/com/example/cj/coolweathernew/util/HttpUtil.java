package com.example.cj.coolweathernew.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by cj on 2017/3/4.
 */

public class HttpUtil {
      public  static  void sendOkHttpRequst(String address,okhttp3.Callback  callback){
          OkHttpClient client =new OkHttpClient();
          Request request =new Request.Builder().url(address).build();
          client.newCall(request).enqueue(callback);
      }

}
