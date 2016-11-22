package com.musicocracy.fpgk.ui;

import android.content.Intent;
import android.os.Bundle;

import com.musicocracy.fpgk.CyberJukeboxApplication;
import com.musicocracy.fpgk.mvp.presenter.BlacklistPresenter;
import com.musicocracy.fpgk.mvp.presenter.Presenter;
import com.musicocracy.fpgk.mvp.view.BlacklistView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlacklistActivity extends ActivityBase<BlacklistView> implements BlacklistView {
    private static final String TAG = "BlacklistActivity";
    @Inject BlacklistPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_blacklist, this);
    }

    @OnClick(R.id.filter_add_btn)
    public void addFilterClick() {
        Intent intent = new Intent(this, AddTermActivity.class);
        startActivity(intent);
    }

    @Override
    protected Presenter<BlacklistView> getPresenter() {
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
}
