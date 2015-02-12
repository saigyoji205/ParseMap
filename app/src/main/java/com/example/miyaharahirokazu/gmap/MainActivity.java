package com.example.miyaharahirokazu.gmap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class MainActivity extends ActionBarActivity {
    private static GoogleMap mMap = null;

    private static Location mMyLocation = null;

    private static boolean mMyLocationCentering = false;

    private static MarkerOptions mMyMarkerOptions = null;

    private static String REVERSE_GEOCODE = "hoge";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();

        if(mMap != null)
        {
            // 現在地マーカーを表示
            mMap.setMyLocationEnabled(true);

            //現在地が取得できたらマップ中央に表示する
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
            {

                public void onMyLocationChange(Location location)
                {
                    mMyLocation = location;

                    if(mMyLocation != null && mMyLocationCentering == false)
                    {
                        //　一度だけ現在地を画面中央にする
                        mMyLocationCentering = true;
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLocation.getLatitude(),mMyLocation.getLongitude()),14.0f);
                        mMap.animateCamera(cameraUpdate);

                        //　逆ジオコーディングで現在地の住所を取得する
                        requestReverseGeocode(location.getLatitude(),location.getLongitude());
                    }

                }

            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
            {
                @Override
                public void onMapClick(LatLng latLng)
                {
                    mMyMarkerOptions = new MarkerOptions();
                    mMyMarkerOptions.position(latLng);

                    // 古いピンを消去する
                    mMap.clear();
                    // タップした位置にピンを立てる
                    mMap.addMarker(mMyMarkerOptions);

                    // 逆ジオコーディングでピンを立てた位置の住所を取得する
                    requestReverseGeocode(latLng.latitude, latLng.longitude);
                }
            });
        }
    }

    protected void onResume()
    {
        super.onResume();

        // 逆ジオコーディングのレシーバ登録
        registerReceiver(reverseGeoCodeReceiver,new IntentFilter(REVERSE_GEOCODE));
    }

    protected void onPause()
    {
        super.onPause();

        // 逆ジオコーディングのレシーバ解除
        unregisterReceiver(reverseGeoCodeReceiver);
    }

    /*　逆ジオコーディング */
    private void requestReverseGeocode(double latitude, double longitude) {
        // Googleの逆ジオコーディング用URL(結果はJSONで受け取る)
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.encodedAuthority("maps.googleapis.com");
        builder.path("/maps/api/geocode/json");
        builder.appendQueryParameter("latlng",latitude+","+longitude);
        builder.appendQueryParameter("sensor","true");
        builder.appendQueryParameter("language", Locale.getDefault().getLanguage());
        HttpGet request = new HttpGet(builder.build().toString());

        try
        {
            // REST API非同期アクセスをリクエスト
            //　リクエスト結果はreverseGeoCodeReceiverで受け取る
            RestTask task = new RestTask(this,REVERSE_GEOCODE);
            task.execute(request);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /*　逆ジオコーディングレシーバ　*/

    private BroadcastReceiver reverseGeoCodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String responseJson = intent.getStringExtra(RestTask.HTTP_RESPONSE);

            //取得結果を解析
            parseReverseGeoCodeJSON(responseJson);
        }
    };

    private void parseReverseGeoCodeJSON(String str) {

        try
        {
            String address = "";
            JSONObject rootObject = new JSONObject(str);
            JSONArray eventArray = rootObject.getJSONArray("results");

            for(int i = 0; i < eventArray.length();i++)
            {
                JSONObject jsonObject = eventArray.getJSONObject(i);
                address = jsonObject.getString("formatted_address");

                if(!address.equals(""))
                {
                    Toast.makeText(this,address,Toast.LENGTH_LONG).show();
                    break;
                }
            }

        } catch (JSONException e)
        {
            e.printStackTrace();
        }


    }


}
