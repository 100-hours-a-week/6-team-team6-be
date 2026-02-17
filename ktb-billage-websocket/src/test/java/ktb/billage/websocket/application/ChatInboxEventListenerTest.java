package ktb.billage.websocket.application;

import ktb.billage.websocket.application.event.ChatInboxSendEvent;
import ktb.billage.websocket.application.port.ChatInboxNotifier;
import ktb.billage.websocket.dto.ChatSendAckResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatInboxEventListenerTest {
    @Mock
    private ChatInboxNotifier chatInboxNotifier;

    @InjectMocks
    private ChatInboxEventListener chatInboxEventListener;

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

        chatInboxEventListener.handle(event);

        verify(chatInboxNotifier).sendToUserInbox(20L, ack);
    }
}
