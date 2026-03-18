package ktb.billage.application.userbehavior;

import ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent;
import ktb.billage.application.userbehavior.port.UserBehaviorAiSyncPort;
import ktb.billage.domain.post.userbehavior.UserBehaviorLog;
import ktb.billage.domain.post.userbehavior.UserBehaviorLogService;
import ktb.billage.domain.post.userbehavior.UserBehaviorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBehaviorBatchDispatchUseCaseUnitTest {
    @Mock
    private UserBehaviorLogService userBehaviorLogService;

    @Mock
    private UserBehaviorAiSyncPort userBehaviorAiSyncPort;

    @InjectMocks
    private UserBehaviorBatchDispatchUseCase useCase;

    @Test
    @DisplayName("5건 배치를 예약하면 AI 서버 전송 후 해당 배치를 삭제한다")
    void handle_DeletesBatch_WhenAiSyncSucceeds() {
        UserBehaviorBatchRequestedEvent event = new UserBehaviorBatchRequestedEvent(10L);
        List<UserBehaviorLog> logs = List.of(
                new UserBehaviorLog(10L, 20L, UserBehaviorType.SEARCH, "캠핑 의자"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.SEARCH, "썬크림"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.CLICK, "43"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.CLICK, "67"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.CLICK, "123")
        );
        when(userBehaviorLogService.reserveOldestPending(eq(10L), anyString())).thenReturn(logs);

        useCase.handle(event);

        verify(userBehaviorAiSyncPort).sync(10L, 20L, logs);
        verify(userBehaviorLogService).deleteBatch(anyString());
        verify(userBehaviorLogService, never()).releaseBatch(anyString());
    }

    @Test
    @DisplayName("AI 서버 전송에 실패하면 배치를 복구하고 예외를 다시 던진다")
    void handle_ReleasesBatch_WhenAiSyncFails() {
        UserBehaviorBatchRequestedEvent event = new UserBehaviorBatchRequestedEvent(10L);
        List<UserBehaviorLog> logs = List.of(
                new UserBehaviorLog(10L, 20L, UserBehaviorType.SEARCH, "캠핑 의자"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.SEARCH, "썬크림"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.CLICK, "43"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.CLICK, "67"),
                new UserBehaviorLog(10L, 20L, UserBehaviorType.CLICK, "123")
        );
        when(userBehaviorLogService.reserveOldestPending(eq(10L), anyString())).thenReturn(logs);
        doThrow(new IllegalStateException("AI failed")).when(userBehaviorAiSyncPort).sync(10L, 20L, logs);

        assertThatThrownBy(() -> useCase.handle(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI failed");

        verify(userBehaviorLogService).releaseBatch(anyString());
        verify(userBehaviorLogService, never()).deleteBatch(anyString());
    }
}
