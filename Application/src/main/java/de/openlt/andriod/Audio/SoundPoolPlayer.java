package de.openlt.andriod.Audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.android.bluetoothlegatt.R;

import java.util.HashMap;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer= null;
    private HashMap mSounds = new HashMap();

    public SoundPoolPlayer(Context pContext)
    {
        // setup Soundpool
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

        mSounds.put(R.raw.laser_gun_shot_2, this.mShortPlayer.load(pContext, R.raw.laser_gun_shot_2, 1));
        mSounds.put(R.raw.shield_hit_1, this.mShortPlayer.load(pContext, R.raw.shield_hit_1, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = (Integer) mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}
