package ktb.billage.application.userbehavior;

import ktb.billage.application.userbehavior.event.UserBehaviorCapturedEvent;
import ktb.billage.application.userbehavior.port.UserBehaviorEventPublisher;
import ktb.billage.domain.post.userbehavior.UserBehaviorLogService;
import ktb.billage.domain.post.userbehavior.UserBehaviorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBehaviorCaptureUseCaseUnitTest {
    @Mock
    private UserBehaviorLogService userBehaviorLogService;

    @Mock
    private UserBehaviorEventPublisher userBehaviorEventPublisher;

    @InjectMocks
    private UserBehaviorCaptureUseCase useCase;

    @Test
    @DisplayName("행동 로그 저장 후 pending 개수가 5개 이상이면 배치 요청 이벤트를 발행한다")
    void handle_PublishesBatchRequest_WhenPendingCountReachesThreshold() {
        UserBehaviorCapturedEvent event = new UserBehaviorCapturedEvent(10L, 20L, UserBehaviorType.SEARCH, "캠핑 의자");
        when(userBehaviorLogService.countPending(10L)).thenReturn(5L);

        useCase.handle(event);

        verify(userBehaviorLogService).save(10L, 20L, UserBehaviorType.SEARCH, "캠핑 의자");
        verify(userBehaviorEventPublisher).publishBatchRequested(any());
    }

    @Test
    @DisplayName("pending 개수가 5개 미만이면 배치 요청 이벤트를 발행하지 않는다")
    void handle_DoesNotPublishBatchRequest_WhenPendingCountIsBelowThreshold() {
        UserBehaviorCapturedEvent event = new UserBehaviorCapturedEvent(10L, 20L, UserBehaviorType.CLICK, "43");
        when(userBehaviorLogService.countPending(10L)).thenReturn(4L);

        useCase.handle(event);

        verify(userBehaviorLogService).save(10L, 20L, UserBehaviorType.CLICK, "43");
        verify(userBehaviorEventPublisher, never()).publishBatchRequested(any());
    }
}
