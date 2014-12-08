package com.collusion.weartext;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private TextView mTextView;

    GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);


            }
        });
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

    }


    public static Document getDoc(String url)
    {
        try
        {
            Document doc = Jsoup.connect(url).get();
            return doc;
        }
        catch(IOException e)
        {
            return null;
        }
    }

    public void oops(View view){
        TextView tv = (TextView)findViewById(R.id.result);
        tv.setText("OOPS!");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("INFO", messageEvent.getData().toString());
        final TextView tv = (TextView)findViewById(R.id.result);
        final String message = new String(messageEvent.getData());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(message);
                tv.setMovementMethod(new ScrollingMovementMethod());
            }
        });

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(googleApiClient, this);
        Log.i("INFO", "Registered");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
