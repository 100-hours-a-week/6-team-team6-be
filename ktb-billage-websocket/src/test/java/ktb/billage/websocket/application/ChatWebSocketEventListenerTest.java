package ktb.billage.websocket.application;

import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatWebSocketNotifier;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketEventListenerTest {
    @Mock
    private ChatWebSocketNotifier chatWebSocketNotifier;

    @InjectMocks
    private ChatWebSocketEventListener chatWebSocketEventListener;

    @Test
    void handle_ShouldForwardEventToNotifier() {
        ChatSendAckResponse ack = new ChatSendAckResponse(
                1L,
                100L,
                "200",
                "message",
                Instant.parse("2026-02-16T00:00:00Z")
        );
        ChatInboxSendEvent event = new ChatInboxSendEvent(20L, ack);

        chatWebSocketEventListener.handle(event);

        verify(chatWebSocketNotifier).sendToUserInbox(20L, ack);
    }
}
