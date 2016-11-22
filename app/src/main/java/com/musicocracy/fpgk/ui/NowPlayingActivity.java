package com.musicocracy.fpgk.ui;

import android.os.Bundle;

import com.musicocracy.fpgk.CyberJukeboxApplication;
import com.musicocracy.fpgk.mvp.presenter.NowPlayingPresenter;
import com.musicocracy.fpgk.mvp.presenter.Presenter;
import com.musicocracy.fpgk.mvp.view.NowPlayingView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class NowPlayingActivity extends ActivityBase<NowPlayingView> implements NowPlayingView {
    private static final String TAG = "NowPlayingActivity";
    @Inject NowPlayingPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_now_playing, this);
    }

    @Override
    protected Presenter<NowPlayingView> getPresenter() {
        return presenter;
    }

    @Override
    protected void butterKnifeBind() {
        ButterKnife.bind(this);
    }

    @Override
    protected void daggerInject() {
        CyberJukeboxApplication.getComponent(this).inject(this);
    }

    @Override
    public void updateArtist(String artistName) {

    }

    @Override
    public void updateSong(String songName) {

    }

    @Override
    public void updateVotableSongs(List<String> votableSongs) {

    }
}
