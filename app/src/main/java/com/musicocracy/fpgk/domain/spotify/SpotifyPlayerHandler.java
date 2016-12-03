package com.musicocracy.fpgk.domain.spotify;

import com.musicocracy.fpgk.domain.util.Logger;
import com.musicocracy.fpgk.domain.util.RxUtils;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class SpotifyPlayerHandler implements SpotifyPlayer.NotificationCallback {
    private static final String TAG = "SpotifyPlayerHandler";
    private static final int TIME_BEFORE_NEXT_SONG = 10000; //ms
    private final Logger log;
    private SpotifyPlayer player;
    private boolean playerStarted = false;

    public SpotifyPlayerHandler(Logger log, SpotifyPlayer player) {
        this.log = log;
        this.player = player;

        player.addNotificationCallback(this);
    }

    public void play(String uri) {
        if (!playerStarted) {
            // TODO: Load uri from algorithm
            player.playUri(null, uri, 0, 0);
            playerStarted = true;
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackChanged) {
            scheduleTimer();
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        log.error(TAG, "Error in Playback of Spotify Player.");
    }

    private void scheduleTimer() {
        long currentTrackDuration = player.getMetadata().currentTrack.durationMs;
        Observable.timer(currentTrackDuration - TIME_BEFORE_NEXT_SONG, TimeUnit.MILLISECONDS)
            .subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    // TODO: retrieve song from DJ algorithm, clear votes for previously played song
                    String nextURI = null;
                    player.queue(null, nextURI);
                    log.info(TAG, "Song Queued(URI): " + nextURI);
                }
            });
    }

    public void stopTimer() {
        player.removeNotificationCallback(this);
    }
}