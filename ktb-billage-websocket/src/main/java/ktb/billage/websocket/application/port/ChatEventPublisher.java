package ktb.billage.websocket.application.port;

import ktb.billage.websocket.application.event.BuyerFirstMessageSentEvent;

public interface ChatEventPublisher {
    void publishFirstMessageSent(BuyerFirstMessageSentEvent event);
}
