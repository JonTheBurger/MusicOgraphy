package com.musicocracy.fpgk.domain.net;

import com.google.protobuf.InvalidProtocolBufferException;
import com.musicocracy.fpgk.domain.spotify.Browser;
import com.musicocracy.fpgk.domain.util.Logger;
import com.musicocracy.fpgk.domain.util.PartySettings;
import com.musicocracy.fpgk.net.proto.BasicReply;
import com.musicocracy.fpgk.net.proto.BasicReplyOrBuilder;
import com.musicocracy.fpgk.net.proto.BrowseSongsReply;
import com.musicocracy.fpgk.net.proto.BrowseSongsRequest;
import com.musicocracy.fpgk.net.proto.ConnectRequest;
import com.musicocracy.fpgk.net.proto.MessageType;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;
import rx.Subscription;
import rx.functions.Action1;

public class ServerHandler {
    private static final Subscription[] emptySubs = new Subscription[0];
    private static final String TAG = "ServerHandler";
    private static final int NUM_BROWSE_RESULTS = 10;
    private final ServerEventBus eventBus;
    private final PartySettings partySettings;
    private final Browser browser;
    private final SpotifyApi api;
    private final Logger log;
    private Subscription[] subscriptions = emptySubs;


    public ServerHandler(ServerEventBus eventBus, PartySettings partySettings, Browser browser, SpotifyApi api, Logger log) {
        this.eventBus = eventBus;
        this.partySettings = partySettings;
        this.browser = browser;
        this.api = api;
        this.log = log;
    }

    public void onCreate() {
        api.setAccessToken(partySettings.getSpotifyToken());
        subscriptions = new Subscription[] {
                createLogSub(),
                createClientConnectSub(),
                createBrowseRequestSub(),
                createVotableSongsRequestSub(),
                createPlayRequestSub(),
                createVoteRequestSub()
        };
    }

    private Subscription createLogSub() {
        return eventBus.getObservableLog()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        log.info(TAG, s);
                    }
                });
    }

    private Subscription createClientConnectSub() {
        return eventBus.getObservable(MessageType.CONNECT_REQUEST)
                .subscribe(new Action1<ProtoMessageBySender>() {
                    @Override
                    public void call(ProtoMessageBySender msgBySender) {
                        ConnectRequest request;
                        try {
                            request = ConnectRequest.parseFrom(msgBySender.message.getBody());
                        } catch (InvalidProtocolBufferException e) {
                            request = ConnectRequest.getDefaultInstance();
                            e.printStackTrace();
                        }

                        BasicReply reply;
                        if (request != ConnectRequest.getDefaultInstance() &&
                                request.getPartyName().equals(partySettings.getPartyName())) {
                            reply = BasicReply.newBuilder()
                                .setSuccess(true)
                                .setMessage("")
                                .setReplyingTo(msgBySender.message.getHeader().getType())
                                .build();
                        } else {
                            reply = BasicReply.newBuilder()
                                .setSuccess(false)
                                .setMessage("Invalid party credentials")
                                .setReplyingTo(msgBySender.message.getHeader().getType())
                                .build();
                        }
                        msgBySender.replyWith(reply);
                    }
                });
    }

    private Subscription createBrowseRequestSub() {
        return eventBus.getObservable(MessageType.BROWSE_SONGS_REQUEST)
                .subscribe(new Action1<ProtoMessageBySender>() {
                    @Override
                    public void call(ProtoMessageBySender msgBySender) {
                        BrowseSongsRequest request;
                        try {
                            request = BrowseSongsRequest.parseFrom(msgBySender.message.getBody());
                            log.info(TAG, "Successful parse");
                        } catch (InvalidProtocolBufferException e) {
                            log.error(TAG, e.toString());
                            request = BrowseSongsRequest.getDefaultInstance();
                            e.printStackTrace();
                        }

                        List<Track> browseTracks = browser.browseTracks(request.getSongTitle());
                        log.info(TAG, browseTracks.size() + " songs found");
                        BrowseSongsReply.Builder builder = BrowseSongsReply.newBuilder();

                        for (int i = 0; i < browseTracks.size() && i < NUM_BROWSE_RESULTS; i++) {
                            builder .addSongs(BrowseSongsReply.BrowsableSong.newBuilder()
                                    .setTitle(browseTracks.get(i).name)
                                    // Gets the name of the first artist
                                    .setArtist(browseTracks.get(i).artists.get(0).name)
                                    .setUri(browseTracks.get(i).uri)
                                    .setMusicService("Spotify")
                                    .build());
                        }
                        BrowseSongsReply reply = builder.build();

                        log.info(TAG, "Sending msg " + reply);
                        msgBySender.replyWith(reply);
                        log.info(TAG, "Send complete. ~" + reply.toByteArray().length + " byte body");
                    }
                });
    }

    private Subscription createVotableSongsRequestSub() {
        return null;
    }

    private Subscription createPlayRequestSub() {
        return null;
    }

    private Subscription createVoteRequestSub() {
        return null;
    }

    public void onDestroy() {
        for (int i = 0; i < subscriptions.length; i++) {
            safeUnsubscribe(subscriptions[i]);
        }
        subscriptions = emptySubs;
    }

    private static void safeUnsubscribe(Subscription sub) {
        if (sub != null && !sub.isUnsubscribed()) {
            sub.unsubscribe();
        }
    }
}