package lopez.sanchez.fitzonetfg;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    VideoView videoView;
    Button boton;
    private static final int JOB_ID = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Necesario porque en versiones anteriores a Lollipop no hay soporte para JobService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleJob();
        }

        videoView = (VideoView)findViewById(R.id.VideoPortada);
        Uri path = Uri.parse("android.resource://lopez.sanchez.fitzonetfg/"+R.raw.videoapp);
        videoView.setVideoURI(path);
        videoView.start();
        boton = (Button)findViewById(R.id.b_siguiente);
        boton.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Muestra el botón después de 3 segundos
                boton.setVisibility(View.VISIBLE);
            }
        }, 3000);

    }
    public void nueva (View v){
        Intent intent = new Intent(this,  InicioApp.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob() {
        //servicio que se va a ejecutar
        ComponentName componentName = new ComponentName(this, ReiniciarPlazasServicio.class);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startMillis = calendar.getTimeInMillis();
        long currentMillis = System.currentTimeMillis();
        long delayMillis = startMillis > currentMillis ? startMillis - currentMillis : startMillis + 24 * 60 * 60 * 1000 - currentMillis;

        @SuppressLint("MissingPermission") JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                .setMinimumLatency(delayMillis)
                .setOverrideDeadline(delayMillis + 1000)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}