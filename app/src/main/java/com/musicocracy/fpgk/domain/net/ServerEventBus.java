package com.musicocracy.fpgk.domain.net;

import com.google.protobuf.MessageLite;
import com.musicocracy.fpgk.net.proto.Envelope;
import com.musicocracy.fpgk.net.proto.MessageType;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * A wrapper around {@link RxTcpServer} that translates between {@link Envelope} and String
 * @see ClientEventBus
 */
public class ServerEventBus {
    private final RxTcpServer server;
    private final ProtoEnvelopeFactory factory;

    /**
     * Basic constructor.
     * @param server reactive client used as networking backend
     * @param factory {@link Envelope} to String translator
     */
    public ServerEventBus(RxTcpServer server, ProtoEnvelopeFactory factory) {
        this.server = server;
        this.factory = factory;
    }

    /**
     * Starts the server on the specified port. The server will automatically service any new clients
     * @param port Port number
     */
    public void start(int port) {
        server.start(port);
    }

    /**
     * Sends a message to every client. If no clients are connected, the message is swallowed.
     * @param message Message that will be broadcast to all clients.
     */
    public void sendToAll(MessageLite message) {
        server.sendToAll(factory.envelopeToBase64(factory.createEnvelopeFor(message)));
    }

    /**
     * Stops the server. The server will no longer respond to events.
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        server.stop();
    }

    /**
     * Boolean condition indicating if server is running.
     * @return Boolean condition indicating if server is running.
     * @see #getIsRunningObservable()
     */
    public boolean isRunning() {
        return server.isRunning();
    }

    /**
     * Waits until the isRunning status has changed for a duration.
     * @param timeSpan How long to wait for the isRunning status to change.
     * @param timeUnit How to interpret timeSpan.
     * @return If a timeout has occurred.
     */
    public boolean awaitNextIsRunningChanged(long timeSpan, TimeUnit timeUnit) {
        return server.awaitNextIsRunningChanged(timeSpan, timeUnit);
    }

    /**
     * An observable stream of messages received from a client. Messages are provided with the
     * accompanied sender connection, enabling users to reply to client messages should they choose
     * to.
     * @param type Designates the type of messages to listen for
     * @return An observable stream of messages by client sender.
     */
    public Observable<ProtoMessageBySender> getObservable(final MessageType type) {
        return server.getObservable()
            .map(new Func1<StringMessageBySender, ProtoMessageBySender>() {
                @Override
                public ProtoMessageBySender call(StringMessageBySender messageBySender) {
                    return new ProtoMessageBySender(messageBySender, factory);
                }
            })
            .filter(new Func1<ProtoMessageBySender, Boolean>() {
                @Override
                public Boolean call(ProtoMessageBySender sender) {
                    return sender.message.getHeader().getType() == type;
                }
            });
    }

    /**
     * An observable stream of client running status changes.
     * @return An observable stream of server running status changes.
     * @see #isRunning()
     */
    public Observable<Boolean> getIsRunningObservable() {
        return server.getIsRunningObservable();
    }

    /**
     * An observable stream of log messages provided as Strings.
     * @return An observable stream of log messages provided as Strings.
     */
    public Observable<String> getObservableLog() {
        return server.getObservableLog();
    }
}
