package com.blackmamba.paulcruz.dsp;
/** PCM16 mono prototype. Digital ceiling is NOT an acoustic dB SPL guarantee. */
public final class SafeSpeechProcessor {
 private static final float CEILING=0.35f, THRESHOLD=0.10f, FLOOR=0.009f;
 private final float lowCoef, highCoef, rampStep;
 private float low, high, envelope, dynamicGain=1f, appliedGain=0f;
 private volatile float userGain=0.5f;
 public SafeSpeechProcessor(int sampleRate) {
  if(sampleRate<8000||sampleRate>192000)throw new IllegalArgumentException("sampleRate");
  lowCoef=(float)Math.exp(-2*Math.PI*250/sampleRate);
  highCoef=(float)Math.exp(-2*Math.PI*3500/sampleRate);
  rampStep=1f/(sampleRate*0.5f); // max 1x gain rise per half second
 }
 public void setGain(float value){
  if(!Float.isFinite(value))throw new IllegalArgumentException("gain");
  userGain=Math.max(0f,Math.min(2f,value));
 }
 public void reset(){low=high=envelope=0f;dynamicGain=1f;appliedGain=0f;}
 public void process(short[] pcm,int count){
  if(pcm==null||count<0||count>pcm.length)throw new IllegalArgumentException("pcm/count");
  for(int i=0;i<count;i++){
   float requested=userGain;
   appliedGain=requested<appliedGain?requested:Math.min(requested,appliedGain+rampStep);
   float sample=pcm[i]/32768f;
   low=lowCoef*low+(1f-lowCoef)*sample;
   float hp=sample-low;
   high=highCoef*high+(1f-highCoef)*hp;
   float band=high; // low-pass of high-passed signal: actual 250–3500 Hz speech band
   envelope=Math.max(Math.abs(band),envelope*0.995f);
   float gate=envelope<FLOOR?0.12f:1f;
   float desired=band*gate*appliedGain;
   float abs=Math.abs(desired);
   float target=abs>THRESHOLD?(THRESHOLD+(abs-THRESHOLD)/4f)/abs:1f;
   dynamicGain=Math.min(target,dynamicGain+0.00015f);
   float out=Math.max(-CEILING,Math.min(CEILING,desired*dynamicGain));
   pcm[i]=(short)Math.round(out*32767f);
  }
 }
 public static float maxDigitalPeak(){return CEILING;}
}
