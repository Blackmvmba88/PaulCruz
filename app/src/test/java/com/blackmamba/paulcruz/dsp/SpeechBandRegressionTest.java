package com.blackmamba.paulcruz.dsp;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
public final class SpeechBandRegressionTest {
 private static short[] sine(int hz){
  short[] samples=new short[96000];
  for(int i=0;i<samples.length;i++)samples[i]=(short)Math.round(Math.sin(2*Math.PI*hz*i/48000.0)*.08*32767);
  return samples;
 }
 private static double rms(short[] a){
  double sum=0;for(int i=48000;i<a.length;i++)sum+=(double)a[i]*a[i];
  return Math.sqrt(sum/48000)/32768;
 }
 @Test public void speechBandSuppressesBassAndTreble(){
  SafeSpeechProcessor p=new SafeSpeechProcessor(48000);p.setGain(1f);
  short[] speech=sine(1000);p.process(speech,speech.length);double voice=rms(speech);
  p.reset();short[] bass=sine(60);p.process(bass,bass.length);double low=rms(bass);
  p.reset();short[] treble=sine(12000);p.process(treble,treble.length);double high=rms(treble);
  assertTrue("1 kHz must exceed 60 Hz",voice>2*low);
  assertTrue("1 kHz must exceed 12 kHz",voice>2*high);
 }
 @Test public void silenceAndImmediateMute(){
  SafeSpeechProcessor p=new SafeSpeechProcessor(48000);
  short[] samples=new short[480];p.process(samples,samples.length);
  for(short s:samples)assertEquals(0,s);
  p.setGain(2f);Arrays.fill(samples,Short.MAX_VALUE);p.process(samples,samples.length);
  p.setGain(0f);Arrays.fill(samples,Short.MAX_VALUE);p.process(samples,samples.length);
  for(short s:samples)assertEquals(0,s);
 }
}
