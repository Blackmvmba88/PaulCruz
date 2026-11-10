package com.blackmamba.paulcruz.dsp;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
public class SafeSpeechProcessorTest {
 @Test public void silenceRemainsSilent(){SafeSpeechProcessor p=new SafeSpeechProcessor(48000);short[] data=new short[4800];p.process(data,data.length);for(short s:data)assertEquals(0,s);}
 @Test public void fullScaleIsLimited(){SafeSpeechProcessor p=new SafeSpeechProcessor(48000);p.setGain(2f);short[] data=new short[48000];Arrays.fill(data,Short.MAX_VALUE);p.process(data,data.length);for(short s:data)assertTrue(Math.abs((int)s)<=11469);}
 @Test public void invalidGainRejected(){SafeSpeechProcessor p=new SafeSpeechProcessor(48000);for(float x:new float[]{Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){try{p.setGain(x);fail("invalid gain");}catch(IllegalArgumentException expected){}}}
 @Test public void respectsFrameCount(){SafeSpeechProcessor p=new SafeSpeechProcessor(48000);short[] data={5000,5000,5000};p.process(data,1);assertEquals(5000,data[1]);assertEquals(5000,data[2]);}
 @Test(expected=IllegalArgumentException.class) public void rejectsInvalidFrames(){new SafeSpeechProcessor(48000).process(new short[2],-1);}
 @Test(expected=IllegalArgumentException.class) public void rejectsInvalidRate(){new SafeSpeechProcessor(0);}
}
