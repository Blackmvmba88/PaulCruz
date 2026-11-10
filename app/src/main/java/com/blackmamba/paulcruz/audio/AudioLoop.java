package com.blackmamba.paulcruz.audio;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import com.blackmamba.paulcruz.dsp.SafeSpeechProcessor;
/** Foreground-only monitoring; refuses to start without wired/USB headset output. */
public final class AudioLoop {
 private static final int RATE=48000, FRAME=480;
 private final Context context;
 private final SafeSpeechProcessor dsp=new SafeSpeechProcessor(RATE);
 private volatile boolean running=false;
 private volatile String status="Detenido";
 private Thread worker;
 public AudioLoop(Context context){this.context=context.getApplicationContext();}
 public String getStatus(){return status;}
 public void setGain(float gain){dsp.setGain(gain);}
 private AudioDeviceInfo headset() {
  AudioManager m=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
  if(m==null)return null;
  for(AudioDeviceInfo device:m.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
   int type=device.getType();
   if(type==AudioDeviceInfo.TYPE_WIRED_HEADPHONES||type==AudioDeviceInfo.TYPE_WIRED_HEADSET||type==AudioDeviceInfo.TYPE_USB_HEADSET) return device;
  }
  return null;
 }
 public synchronized boolean start(){
  if(running)return true;
  if(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){status="Permiso de micrófono requerido";return false;}
  if(headset()==null){status="Conecta audífonos cableados o USB";return false;}
  dsp.reset();running=true;status="Iniciando...";
  worker=new Thread(this::run,"PaulCruz-Audio");worker.start();return true;
 }
 public synchronized void stop(){running=false;status="Detenido";}
 private void run(){
  AudioRecord input=null;
  AudioTrack output=null;
  try {
   Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
   int inMin=AudioRecord.getMinBufferSize(RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
   int outMin=AudioTrack.getMinBufferSize(RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
   if(inMin<=0||outMin<=0)throw new IllegalStateException("Unsupported format");
   input=new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(inMin,FRAME*4));
   output=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
    .setAudioFormat(new AudioFormat.Builder().setSampleRate(RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
    .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(Math.max(outMin,FRAME*4))
    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY).build();
   AudioDeviceInfo device=headset();
   if(device==null||!output.setPreferredDevice(device))throw new IllegalStateException("Headset routing unavailable");
   if(input.getState()!=AudioRecord.STATE_INITIALIZED||output.getState()!=AudioTrack.STATE_INITIALIZED)throw new IllegalStateException("Audio init");
   input.startRecording();output.play();status="Escuchando";
   short[] frame=new short[FRAME];
   while(running){
    AudioDeviceInfo available=headset();
    AudioDeviceInfo routed=output.getRoutedDevice();
    if(available==null||routed==null||routed.getId()!=device.getId()){status="Ruta de salida no segura; detenido";break;}
    int n=input.read(frame,0,FRAME,AudioRecord.READ_BLOCKING);
    if(n<=0)throw new IllegalStateException("Input error: "+n);
    dsp.process(frame,n);
    int offset=0;
    while(running&&offset<n){
     int written=output.write(frame,offset,n-offset,AudioTrack.WRITE_BLOCKING);
     if(written<=0)throw new IllegalStateException("Output error: "+written);
     offset+=written;
    }
   }
  }catch(Exception e){status="Error: "+e.getClass().getSimpleName();}
  finally{
   running=false;
   if(input!=null){try{input.stop();}catch(Exception ignored){}input.release();}
   if(output!=null){try{output.pause();output.flush();output.stop();}catch(Exception ignored){}output.release();}
  }
 }
}
