import com.blackmamba.paulcruz.dsp.SafeSpeechProcessor;
public final class DspSmoke {
 static double rms(short[] a){double sum=0;for(int i=48000;i<a.length;i++)sum+=(double)a[i]*a[i];return Math.sqrt(sum/48000)/32768;}
 static double tone(int hz){
  SafeSpeechProcessor p=new SafeSpeechProcessor(48000);p.setGain(1);
  short[] a=new short[96000];for(int i=0;i<a.length;i++)a[i]=(short)Math.round(Math.sin(2*Math.PI*hz*i/48000.0)*.08*32767);
  p.process(a,a.length);return rms(a);
 }
 public static void main(String[] args){
  double voice=tone(1000),bass=tone(60),treble=tone(12000);
  if(!(voice>2*bass&&voice>2*treble))throw new AssertionError("speech band regression: "+voice+" "+bass+" "+treble);
  SafeSpeechProcessor p=new SafeSpeechProcessor(48000);p.setGain(2);short[] a=new short[48000];
  java.util.Arrays.fill(a,Short.MAX_VALUE);p.process(a,a.length);
  for(short s:a)if(Math.abs((int)s)>11469)throw new AssertionError("digital peak");
  p.setGain(0);java.util.Arrays.fill(a,Short.MAX_VALUE);p.process(a,a.length);
  for(short s:a)if(s!=0)throw new AssertionError("mute");
  System.out.printf("PASS speech band + peak + mute; voice=%.6f bass=%.6f treble=%.6f%n",voice,bass,treble);
 }
}
