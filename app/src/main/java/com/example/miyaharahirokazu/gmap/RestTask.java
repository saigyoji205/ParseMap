package com.example.miyaharahirokazu.gmap;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by miyaharahirokazu on 15/02/08.
 */
public class RestTask extends AsyncTask<HttpUriRequest,Void,String>
{
    public static final String HTTP_RESPONSE = "httpResponse";

    private Context mContext;

    private HttpClient mClient;

    private String mAction;

    public RestTask(Context context,String action)
    {
        mContext = context;
        mAction = action;
        mClient = new DefaultHttpClient();
    }

    @Override
    protected String doInBackground(HttpUriRequest... httpUriRequests) {

        try
        {
            HttpUriRequest request = httpUriRequests[0];
            HttpResponse serverResponse =  mClient.execute(request);

            BasicResponseHandler handler = new BasicResponseHandler();
            String response = handler.handleResponse(serverResponse);
            return response;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        Intent intent = new Intent(mAction);
        intent.putExtra(HTTP_RESPONSE,s);

        mContext.sendBroadcast(intent);
    }
}

