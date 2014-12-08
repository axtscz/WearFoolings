package com.collusion.weartext;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

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
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient googleApiClient;

    String message;

    static Integer setHour;

    static Integer setMinute;
    static AlarmReceiver alarmReceiver = new AlarmReceiver();

    TextView Title;
    TextView Scripture;
    TextView Reference;

    String scriptureStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //alarmReceiver.setOnce(this);

        Title = (TextView)findViewById(R.id.Title);
        Scripture = (TextView)findViewById(R.id.scripture);
        Reference = (TextView)findViewById(R.id.reference);

        //Reference.setMovementMethod(new ScrollingMovementMethod());

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int day = cal.get(Calendar.DAY_OF_MONTH);
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
                    Element scripture = doc.select("p.sa").first();
                    //Log.i("INFO", articles.get(0).text());
                    Log.i("INFO", article.text());
                    message = article.text();
                    final String dat = date.text();
                    scriptureStr = scripture.text();
                    buildNotification(message, dat);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Title.setText(dat);
                            Scripture.setText(scriptureStr);
                            Reference.setText(message);
                        }});
                    if (googleApiClient.isConnected()) {
                        //Collection<String> nodes = getNodes();
                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                        for (Node node : nodes.getNodes()) {
                            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/start/MainActivity", message.getBytes()).await();
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
        if (id == R.id.action_setReminder) {
            setTime();
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
        Bitmap bp = BitmapFactory.decodeResource(getResources(), R.drawable.bookic512);
        int imageHeight = options.outHeight;
        int imageWeight = options.outWidth;

        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setLargeIcon(bp);
        builder.setSmallIcon(R.drawable.bookiconsmall);
        builder.setContentTitle(Title);
        builder.setContentText(data);
        Intent result = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(result);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(11110, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(data));

        builder.extend(new NotificationCompat.WearableExtender().setBackground(bp));
        //builder.build();
        NotificationManager nm = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1001, builder.build());
    }


    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            Calendar cal = Calendar.getInstance();
            int hour = Calendar.HOUR_OF_DAY;
            int minute = Calendar.MINUTE;
            Log.i("INFO", String.valueOf(hour));
            Log.i("INFO", String.valueOf(minute));
            return new TimePickerDialog(getActivity(), this, hour, minute, false);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setMinute = view.getCurrentMinute();
            setHour = view.getCurrentHour();
            alarmReceiver.repeatingTimer(getActivity(), setMinute, setHour);
            Log.i("INFO", setMinute.toString());
            Log.i("INFO", setHour.toString());
        }
    }

    public void SetTime(View view){
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timepicker");
        Intent intent = new Intent(this, Notifier.class);
        Log.i("INFO", "Done");
    }

    public void setTime(){
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timepicker");
        Intent intent = new Intent(this, Notifier.class);
        Log.i("INFO", "Done");
    }

    public void writePrefs(){
        SharedPreferences sharedPreferences = getSharedPreferences("hourset", -1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("hourset", setHour).apply();
        sharedPreferences = getSharedPreferences("minuteset", -1);
        editor = sharedPreferences.edit();
        editor.putInt("minuteset", setMinute).apply();

    }

}
