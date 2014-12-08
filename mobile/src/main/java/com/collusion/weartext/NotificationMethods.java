package com.collusion.weartext;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Antonio on 12/8/2014.
 */
public class NotificationMethods {

    public void buildNotif(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("FROM ALARM");
        builder.setContentText("HEY!");
        NotificationManager nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        nm.notify(1001111, builder.build());
    }

    public void getTextandNotify(final Context contextOne){
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
                    String message = article.text();
                    final String dat = date.text();
                    String scriptureStr = scripture.text();
                    buildNotification(message, dat, contextOne);

                }
            }
        });
        thread.start();
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

    public void buildNotification(String data, String Title, Context context){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.bookic512);
        int imageHeight = options.outHeight;
        int imageWeight = options.outWidth;

        Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(context, 0, viewIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setLargeIcon(bp);
        builder.setSmallIcon(R.drawable.bookiconsmall);
        builder.setContentTitle(Title);
        builder.setContentText(data);
        Intent result = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(result);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(11110, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(data));

        builder.extend(new NotificationCompat.WearableExtender().setBackground(bp));
        //builder.build();
        NotificationManager nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        nm.notify(1001, builder.build());
    }

}
