package com.collusion.weartext;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleApiClient;

    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH) + 1;
        final int dayofWeek = cal.get(Calendar.DAY_OF_WEEK);
        Log.i("INFO", String.valueOf(dayofWeek));

        final String target = ("http://wol.jw.org/en/wol/dt/r1/lp-e/" + String.valueOf(year) + "/" + String.valueOf(month) + "/" + String.valueOf(day));
        Log.i("INFO", target);
        Log.i("INFO", "GettingW doc");

        String Year = String.valueOf(year);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Document doc = getDoc(target);
                if (doc != null)
                {
                    String title = doc.title();

                    Log.i("INFO", title);
                    Elements articles = doc.getElementsByClass("siteParentLink");
                    Element article = doc.select("p.sb").first();
                    Element date = doc.select("p.ss").first();
                    //Log.i("INFO", articles.get(0).text());
                    Log.i("INFO", article.text());
                    message = article.text();
                    String dat = date.text();
                    buildNotification(message, dat);
                    if (googleApiClient.isConnected()) {
                        //Collection<String> nodes = getNodes();
                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                        for (Node node : nodes.getNodes()) {
                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/start/MainActivity", "Message".getBytes()).await();
                            if (!result.getStatus().isSuccess()){
                                Log.e("INFO", "ERROR");
                            } else {
                                Log.i("INFO", "Success sent to: " + node.getDisplayName());
                            }

                        }
                    }
                }
            }
        });
        thread.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Document getDoc(String url)
    {
        try
        {
            Log.i("INFO", "Getting doc");
            Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
            Log.i("INFO", "Got doc");
            return doc;

        }
        catch(IOException e)
        {
            return null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        String message = "#33423423124#";

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onSendMessage(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (googleApiClient.isConnected()) {
                    //Collection<String> nodes = getNodes();
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/startactivity", "Hey0".getBytes()).await();
                        if (!result.getStatus().isSuccess()){
                            Log.e("INFO", "ERROR");
                        } else {
                            Log.i("INFO", "Success sent to: " + node.getDisplayName());
                        }

                    }
                } else {
                    Log.i("INFO", "Not connected!");
                }
            }
        }).start();

    }

    private Collection<String> getNodes()
    {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()){
            results.add(node.getId());
        }
        return results;
    }

    public void buildNotification(String data, String Title){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bp = BitmapFactory.decodeResource(getResources(), R.drawable.b);
        int imageHeight = options.outHeight;
        int imageWeight = options.outWidth;

        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(Title);
        builder.setContentText(data);

        builder.extend(new NotificationCompat.WearableExtender().setBackground(bp));
        //builder.build();
        NotificationManager nm = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1001, builder.build());
    }


}
