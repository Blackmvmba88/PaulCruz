package com.blackmamba.paulcruz;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.blackmamba.paulcruz.audio.AudioLoop;
import java.util.Locale;
public final class MainActivity extends Activity {
 private AudioLoop audio;
 private TextView status;
 @Override public void onCreate(Bundle savedInstanceState){
  super.onCreate(savedInstanceState);
  audio=new AudioLoop(this);
  LinearLayout root=new LinearLayout(this);root.setPadding(36,48,36,24);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);
  TextView title=new TextView(this);title.setText("PaulCruz v0.1");title.setTextSize(28);root.addView(title);
  TextView warning=new TextView(this);warning.setText("PROTOTIPO NO MÉDICO. Audífonos cableados/USB obligatorios. Empieza con volumen físico mínimo.");warning.setPadding(0,20,0,20);root.addView(warning);
  TextView gainLabel=new TextView(this);gainLabel.setText("Ganancia DSP 0.5x");root.addView(gainLabel);
  SeekBar gain=new SeekBar(this);gain.setMax(20);gain.setProgress(5);audio.setGain(.5f);
  gain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
   public void onProgressChanged(SeekBar b,int value,boolean user){float v=value/10f;audio.setGain(v);gainLabel.setText(String.format(Locale.US,"Ganancia DSP %.1fx",v));}
   public void onStartTrackingTouch(SeekBar b){}
   public void onStopTrackingTouch(SeekBar b){}
  });root.addView(gain);
  status=new TextView(this);status.setText("Detenido");status.setPadding(0,20,0,20);root.addView(status);
  Button start=new Button(this);start.setText("Iniciar escucha");root.addView(start);
  Button stop=new Button(this);stop.setText("Detener");root.addView(stop);
  start.setOnClickListener(v->{if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},101);else{audio.start();status.setText(audio.getStatus());}});
  stop.setOnClickListener(v->{audio.stop();status.setText(audio.getStatus());});
  setContentView(root);
  status.postDelayed(new Runnable(){public void run(){if(!isFinishing()){status.setText(audio.getStatus());status.postDelayed(this,400);}}},400);
 }
 @Override public void onRequestPermissionsResult(int req,String[] permissions,int[] results){
  super.onRequestPermissionsResult(req,permissions,results);
  if(req==101&&results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED){audio.start();status.setText(audio.getStatus());}
  else status.setText("Permiso de micrófono denegado");
 }
 @Override protected void onStop(){super.onStop();audio.stop();}
 @Override protected void onDestroy(){audio.stop();super.onDestroy();}
}
