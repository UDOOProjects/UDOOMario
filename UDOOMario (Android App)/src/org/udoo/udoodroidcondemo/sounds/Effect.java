package org.udoo.udoodroidcondemo.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Effect object to play sound effects. Create the effects in onCreate state
 * of the Activity,to allow SoundPool to load audio resources. Release
 * effects when no longer needed, for example in onDestroy state of the
 * Activity
 *
 */
public class Effect {
        /**
         * CONSTANTS :
         * PRIORITY : the priority of the sound. Currently has no
         * effect. Use a value of 1 for future compatibility. (Google doc.)
         * MAX_STREAMS : the maximum number of simultaneous streams for this
         * SoundPool object (Google doc.)
         * STREAM_TYPE : the audio stream type as described in AudioManager.
         * For example, game applications will normally use STREAM_MUSIC. (Google doc.)
         *  
         */
        private static final int PRIORITY = 1;
        private static final int MAX_STREAMS = 4;
        private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

        private SoundPool sound;
        private AudioManager am;
        private float volume;
        private int soundID;

        /**
         * Constructs a Effect object with the following characteristics:
         *
         * @param context
         *            The context of the application
         * @param resId
         *            The resource id of sound effect, like
         *            R.raw.music_effect_example
         */
        public Effect(Context context, int resId) {
                am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                volume = am.getStreamVolume(STREAM_TYPE);
                sound = new SoundPool(MAX_STREAMS, STREAM_TYPE, 0);
                soundID = sound.load(context, resId, PRIORITY);
        }

        /**
         * Play the sound
         */
        public void play() {
                sound.play(soundID, volume, volume, PRIORITY, 0, 1f);
        }
        /**
         * Release the SoundPool resources. Release all memory and native resources used by the SoundPool object.
         * The SoundPool can no longer be used and the reference should be set to null. (Google doc.)
         */
        public void release() {
                sound.release();
        }
}