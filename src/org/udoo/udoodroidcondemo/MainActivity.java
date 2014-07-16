/*
 * Created by ekirei
 * UDOO Team
 */

package org.udoo.udoodroidcondemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import me.palazzetti.adktoolkit.AdkManager;

import org.udoo.udoodroidcondemo.sounds.Effect;
import org.udoo.udoodroidcondemo.twitter.Twitter;
import org.udoo.udoodroidcondemo.twitter.TwitterReceiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final String TAG = "UDOOMario";
	private final String PREFS_NAME = "MarioPrefs";
	
	// ADK
	private AdkManager mAdkManager;
	
	// command to arduino	
	private final String FORWARD_SENDSTRING = "0";
	private final String BACK_SENDSTRING 	= "1";
	private final String RIGHT_SENDSTRING 	= "2";
	private final String LEFT_SENDSTRING 	= "3";
	private final String GOODBOY_SENDSTRING = "4";
	private final String BADBOY_SENDSTRING 	= "5";
	private final String CUTEBOY_SENDSTRING = "6";
	private final String HELLO_SENDSTRING 	= "7";
	private final String MOONWALK_SENDSTRING= "8";
	
	// speech variables
	SpeechRecognizer mSpeechRecognizer;
	
	// speech key
	private ArrayList<String> goodboy_strings = new ArrayList<String>(Arrays.asList("good boy", 
			"well done", "very good", "good work", "good guy", "great"));
	private ArrayList<String> badboy_strings = new ArrayList<String>(Arrays.asList("bad boy", 
			"fuck you", "bad guy"));
	private ArrayList<String> cuteboy_strings = new ArrayList<String>(Arrays.asList("cute boy", "cute", 
			"so cute", "nice"));	
	private ArrayList<String> forward_strings = new ArrayList<String>(Arrays.asList("go forward", 
			"go straight on", "forward"));
	private ArrayList<String> backward_strings = new ArrayList<String>(Arrays.asList("go backward", 
			"go back", "back"));
	private ArrayList<String> right_strings = new ArrayList<String>(Arrays.asList("turn right", 
			"look right", "right"));
	private ArrayList<String> left_strings = new ArrayList<String>(Arrays.asList("turn left", 
			"look left", "left"));
	private ArrayList<String> pizza_strings = new ArrayList<String>(Arrays.asList("pizza", "like pizza",
			"do you like pizza"));
	private ArrayList<String> hi_strings = new ArrayList<String>(Arrays.asList("hi"));
	private ArrayList<String> name_strings = new ArrayList<String>(Arrays.asList("name", "your name",
			"who are you", "what's your name"));
	private ArrayList<String> hello_strings = new ArrayList<String>(Arrays.asList("hello"));
	private ArrayList<String> comefrom_strings = new ArrayList<String>(Arrays.asList("come from", " are you from"));
	private ArrayList<String> goodbye_strings = new ArrayList<String>(Arrays.asList("goodbye")); 
	private ArrayList<String> moonwalk_strings = new ArrayList<String>(Arrays.asList("moonwalk", "moon walk",
			"Michael Jackson", "star", "stop", "dollar", "stock"));
	private ArrayList<String> allwords = new ArrayList<String>();
	
	// gui
	TextView debug_tv;
	ImageButton voiceButton;
	ImageView faceImage;
	Animation animationFadeIn;	
	Animation animationFadeOut;
	ImageButton twitterButton;
	
	AsyncTwitterReceiver mAsyncReceiver = null;
	private boolean running = false; 
	private String mLastFetchedId = "1";

	TextToSpeech tts;
	
	Effect cuteEffect; 
	Effect goodEffect;
	Effect badEffect;
	Effect moonwalk;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAdkManager = new AdkManager((UsbManager) getSystemService(Context.USB_SERVICE));
		registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());
		
		debug_tv = (TextView) findViewById(R.id.textView);
//	    debug_tv.setVisibility(View.GONE);
		
		faceImage = (ImageView) findViewById(R.id.face_imageView);
		
	    voiceButton = (ImageButton) findViewById(R.id.voice_imageButton);
	    voiceButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});
	    
	    twitterButton = (ImageButton) findViewById(R.id.twitter_imageButton);
	    twitterButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startTwitterCommandsFetch();
			}
		});
	    
	    allwords.addAll(hello_strings);
        allwords.addAll(hi_strings);
	    allwords.addAll(name_strings);
	    allwords.addAll(goodboy_strings);
        allwords.addAll(badboy_strings);
        allwords.addAll(cuteboy_strings);
        allwords.addAll(forward_strings);
        allwords.addAll(backward_strings);
        allwords.addAll(right_strings);
        allwords.addAll(left_strings);        
        allwords.addAll(moonwalk_strings);  
        allwords.addAll(pizza_strings);
        allwords.addAll(comefrom_strings);
        allwords.addAll(goodbye_strings);
        
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        
        cuteEffect = new Effect(this, R.raw.thanku); 
    	goodEffect = new Effect(this, R.raw.whohoo);
    	badEffect = new Effect(this, R.raw.no);
    	moonwalk = new Effect(this, R.raw.moonwalk);
        
        faceImage.setImageResource(R.drawable.normal);
        
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        MyRecognitionListener listener = new MyRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);
               
        // Get last stored values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mLastFetchedId = settings.getString("lastId", "1");
        
        tts = new TextToSpeech(getApplicationContext(), 
        	      new TextToSpeech.OnInitListener() {
	            @Override
	            public void onInit(int status) {
	            	if (status == TextToSpeech.SUCCESS) {
	       			 
	                    int result = tts.setLanguage(Locale.US);
	                    tts.setPitch(0.7F);
	         
	                    if (result == TextToSpeech.LANG_MISSING_DATA) {
	                        Log.e("TTS", "Lang missing data");
	                            // missing data, install it
	                            Intent installIntent = new Intent();
	                            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	                            startActivity(installIntent);          
	                    } else {
	                    	tts.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
	                    }
	         
	                } else {
	                    Log.e("TTS", "Initilization Failed!");          
	                }
	            }
            });
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    if(mSpeechRecognizer!=null){
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              

	    }
	    if(tts !=null){
	         tts.stop();
	         tts.shutdown();
	    }
	    mSpeechRecognizer = null;
	    tts = null;
	    mAdkManager.close();    
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastId", mLastFetchedId);
        editor.commit();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cuteEffect.release(); 
    	goodEffect.release();
    	badEffect.release();
        unregisterReceiver(mAdkManager.getUsbReceiver());
    }

	@Override
	protected void onResume() {
	    super.onResume();
	    if(mSpeechRecognizer == null){
        	mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            MyRecognitionListener listener = new MyRecognitionListener();
            mSpeechRecognizer.setRecognitionListener(listener);
        }
	    mAdkManager.open();
	}
    
    public void startTwitterCommandsFetch() {
        // Check if connection is available
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Start Twitter "game" once
            if (mAsyncReceiver == null) {
           	 	running = true;
                mAsyncReceiver = new AsyncTwitterReceiver();
                mAsyncReceiver.execute();
                twitterButton.setBackgroundResource(R.drawable.twitter_logo_clicked);
            } else {
				// Stop Twitter "game"
				running = false;
				mAsyncReceiver = null;
				twitterButton.setBackgroundResource(R.drawable.twitter_logo_noclick);
            }
        } else {
			Log.v(TAG, "No network connection available.");
        }
	}
		 
    public class AsyncTwitterReceiver extends AsyncTask<Void, String, Void> {
        private final static String TAG_LOG = "AsyncTwitterReceiver";

        @Override
        protected Void doInBackground(Void... params) {
            Twitter twits;

            try {
                while (running) {
                    twits = new TwitterReceiver().searchMentions("UDOOMario", mLastFetchedId);

                    if (twits.size() > 0) {
                        // Store the last one
                        mLastFetchedId = twits.get(0).getId();
		                Log.i(TAG_LOG, "last id: " + mLastFetchedId);

                		// lets write the results to the console as well
            			for (int i=0; i < twits.size(); i++) {
            				publishProgress(new String[] {twits.get(i).getUser().getScreenName(), twits.get(i).getText(),
       							 "" + (i+1), "" + twits.size() });
            				Thread.sleep(8000);
            				publishProgress("finish");
            				
            				Log.i(TAG_LOG, "User: " + twits.get(i).getUser().getName()
            						+ "; screenname: " + twits.get(i).getUser().getScreenName()
            						+ "; text: " + twits.get(i).getText()
            						+ "; data: " + twits.get(i).getDateCreated()
            						+ "; id: " + twits.get(i).getId());
            			}
            		} else {
            			Log.i(TAG_LOG, "No tweet");
            		}
                     
                    Thread.sleep(5000);
                }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

            return null;
        }
        
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            TextView twittertv = (TextView) findViewById(R.id.twitter_textView); 
            
            if (values.length > 1) {
	            Log.i(TAG_LOG, "user: " + values[0] + " - text: " + values[1]);
	            tts.speak(values[0] + " says: " + values[1].replace("@UDOOMario", ""), TextToSpeech.QUEUE_FLUSH, null);
	            twittertv.setText(Html.fromHtml("<big><b>" + values[0] + "</b></big> say: <br>" 
	            								+ values[1].replace("@UDOOMario", "") 
	            								+ "<br><small><i>" + values[2] + "/" + values[3] + "</i></small>"));
	            twittertv.setVisibility(View.VISIBLE);
	            
	            searchCommands(values[1]);
            } else {
            	if (twittertv.getVisibility() == View.VISIBLE){
            		twittertv.setVisibility(View.GONE);
            		twittertv.setText("");
            	}
            }  
        }
    
    } 
       
    private boolean searchCommands (String result) {   	
        boolean found = false;
        String stringFounded = "";        
        for (String string : allwords ) {
        	if (result.toLowerCase(Locale.US).contains(string.toLowerCase(Locale.US))) {
        		stringFounded = string;
        		found = true;
        		
        		if (hi_strings.contains(stringFounded)) tts.speak("Hi", TextToSpeech.QUEUE_FLUSH, null);
        		else if (hello_strings.contains(stringFounded)) sendHello();
        		else if (name_strings.contains(stringFounded)) tts.speak("My name is Mario, I'm a Android Robot powered by you doo", TextToSpeech.QUEUE_FLUSH, null);
        		else if (comefrom_strings.contains(stringFounded)) tts.speak("I come from Siena in Italy", TextToSpeech.QUEUE_FLUSH, null);
        		else if (goodboy_strings.contains(stringFounded)) sendGoodCase();
    	        else if (badboy_strings.contains(stringFounded)) sendBadCase();
    	        else if (cuteboy_strings.contains(stringFounded)) sendCuteCase();
    	        else if (forward_strings.contains(stringFounded)) mAdkManager.writeSerial(FORWARD_SENDSTRING);
    	        else if (backward_strings.contains(stringFounded)) mAdkManager.writeSerial(BACK_SENDSTRING);
    	        else if (right_strings.contains(stringFounded)) mAdkManager.writeSerial(RIGHT_SENDSTRING);
    	        else if (left_strings.contains(stringFounded)) mAdkManager.writeSerial(LEFT_SENDSTRING);
    	        else if (moonwalk_strings.contains(stringFounded)) moonWalkCase();
    	        else if (pizza_strings.contains(stringFounded)) tts.speak("Yes, I love pizza, but unfortunately I cannot eat it.", TextToSpeech.QUEUE_FLUSH, null);
    	        else if (goodbye_strings.contains(stringFounded)) sendGoodby();
        		
        		
        		break;
        	}
        }        
        return found;
    }
    
    private void moonWalkCase() {
    	Log.i(TAG, "moonwalk case");
    	moonwalk.play();
    	setNewFace(R.drawable.happy);   	
		mAdkManager.writeSerial(MOONWALK_SENDSTRING);
		returnToNormalState(6000);
    }
    
    private void sendHello() {
    	Log.i(TAG, "hello case");  
    	setNewFace(R.drawable.happy);
    	tts.speak("Hello next", TextToSpeech.QUEUE_FLUSH, null);
		mAdkManager.writeSerial(HELLO_SENDSTRING);
		returnToNormalState(5000);
    }
    
    private void sendGoodby() {
    	Log.i(TAG, "hello case");  
    	setNewFace(R.drawable.happy);
    	tts.speak("Goodbye and thank you", TextToSpeech.QUEUE_FLUSH, null);
		mAdkManager.writeSerial(HELLO_SENDSTRING);
		returnToNormalState(5000);
    }
    
    private void sendGoodCase() {
    	Log.i(TAG, "good case");  
    	setNewFace(R.drawable.happy);
		goodEffect.play();
		mAdkManager.writeSerial(GOODBOY_SENDSTRING);
		returnToNormalState(5000);
    }
    
    private void sendBadCase() {
    	Log.i(TAG, "bad case");  
    	setNewFace(R.drawable.sad);
		badEffect.play();
		mAdkManager.writeSerial(BADBOY_SENDSTRING);	
		returnToNormalState(5000);
    }
    
    private void sendCuteCase() {
    	Log.i(TAG, "cute case");  		
		setNewFace(R.drawable.cute);
		cuteEffect.play();
		mAdkManager.writeSerial(CUTEBOY_SENDSTRING);		
		returnToNormalState(5000);
    }
    
    private void setNewFace(int resourceID){
    	faceImage.startAnimation(animationFadeOut);
		faceImage.setVisibility(View.INVISIBLE);
		faceImage.setImageResource(resourceID);
		faceImage.startAnimation(animationFadeIn);
		faceImage.setVisibility(View.VISIBLE);
    }
    
    private void returnToNormalState(int millis) {
    	new CountDownTimer(millis, 1000) {

            public void onTick(long millisUntilFinished) {
                //do nothing, just let it tick
            }

            public void onFinish() {
            	Log.i(TAG, "returnToNormalState");
            	setNewFace(R.drawable.normal);
            }
         }.start();
    }
      
    @SuppressWarnings("unused")
	private void showToastMessage(String message){
    	  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
	/**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    	
        mSpeechRecognizer.startListening(intent);        
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                //do nothing, just let it tick
            }

            public void onFinish() {
            	Log.i(TAG, "stop countdown");
            	mSpeechRecognizer.stopListening();
            	voiceButton.setBackgroundResource(R.drawable.nose_up);
            }
         }.start();
    }
	
	class MyRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
                Log.d("Speech", "onBeginningOfSpeech");
        }

		@Override
        public void onBufferReceived(byte[] buffer) {
                Log.d("Speech", "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
                Log.d("Speech", "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
                Log.d("Speech", "onError: " + error);
//                if (error == 7 ){
//                	tts.speak("Sorry, but I didn't understand", TextToSpeech.QUEUE_FLUSH, null);
//                }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
                Log.d("Speech", "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
                Log.d("Speech", "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
                Log.d("Speech", "onReadyForSpeech");
                voiceButton.setBackgroundResource(R.drawable.nose_down);
        }
        

        @Override
        public void onResults(Bundle results) {
                Log.d("Speech", "onResults");
                ArrayList<String> resultsArray = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		        debug_tv.setText("Received text: " + resultsArray.get(0));
		        
		        if (!searchCommands(resultsArray.get(0))) {
		        	tts.speak("Sorry, but I didn't understand", TextToSpeech.QUEUE_FLUSH, null);
		        	//showToastMessage("Sentence is not recognized");
		        }
		        
                for (int i = 0; i < resultsArray.size();i++ ) {
                        Log.d("Speech", "result=" + resultsArray.get(i));           
                }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
//               Log.d("Speech", "onRmsChanged");
        }

	}

}
