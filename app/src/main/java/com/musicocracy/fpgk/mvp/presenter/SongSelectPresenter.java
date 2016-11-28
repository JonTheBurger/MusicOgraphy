package com.musicocracy.fpgk.mvp.presenter;

import com.musicocracy.fpgk.mvp.model.SongSelectModel;
import com.musicocracy.fpgk.mvp.view.SongSelectView;
import com.musicocracy.fpgk.net.proto.BrowseSongsReply;
import com.musicocracy.fpgk.net.proto.BrowseSongsRequest;
import com.musicocracy.fpgk.net.proto.VotableSongsReply;
import com.musicocracy.fpgk.net.proto.VotableSongsRequest;

import java.util.ArrayList;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SongSelectPresenter implements Presenter<SongSelectView> {
    private final SongSelectModel model;
    private final Subscription browseSubscription;
    private final Subscription voteSubscription;
    private SongSelectView view;

    public SongSelectPresenter(SongSelectModel model) {
        this.model = model;
        browseSubscription = model.getBrowseReply()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BrowseSongsReply>() {
                    @Override
                    public void call(BrowseSongsReply BrowseSongsReply) {
                        onBrowseResultsReceived(BrowseSongsReply);
                    }
                });

        voteSubscription = model.getVotableSongsReply()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<VotableSongsReply>() {
                    @Override
                    public void call(VotableSongsReply VotableSongsReply) {
                        onVotableSongsReceived(VotableSongsReply);
                    }
                });
    }

    public void populateBrowseSongs(String requestString) {
        BrowseSongsRequest msg = BrowseSongsRequest.newBuilder()
                .setSongTitle(requestString)
                .build();

        model.sendBrowseMsg(msg);
    }

    public void onBrowseResultsReceived(BrowseSongsReply msg) {
        ArrayList<String> testList = new ArrayList<>();
        for (int i = 0; i < msg.getSongsCount(); i++) {
            testList.add(msg.getSongs(i).toString());
        }

        view.updateSongs(testList);
    }

    public void populateVoteSongs() {
        VotableSongsRequest msg = VotableSongsRequest.newBuilder().build();

        model.sendVotableSongsRequestMsg(msg);
    }

    public void onVotableSongsReceived(VotableSongsReply msg) {
        ArrayList<String> testList = new ArrayList<>();
        for (int i = 0; i < msg.getSongsCount(); i++) {
            testList.add(msg.getSongs(i).toString());
        }
        view.updateSongs(testList);
    }

    public void onDestroy() {
        if (browseSubscription != null && !browseSubscription.isUnsubscribed()) {
            browseSubscription.unsubscribe();
        }
    }

    @Override
    public void setView(SongSelectView view) {
        this.view = view;
    }
}
