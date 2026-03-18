package ktb.billage.application.userbehavior;

import ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent;
import ktb.billage.application.userbehavior.event.UserBehaviorCapturedEvent;
import ktb.billage.application.userbehavior.port.UserBehaviorEventPublisher;
import ktb.billage.domain.post.userbehavior.UserBehaviorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserBehaviorCaptureUseCase {
    private final UserBehaviorLogService userBehaviorLogService;
    private final UserBehaviorEventPublisher userBehaviorEventPublisher;

    @Transactional
    public void handle(UserBehaviorCapturedEvent event) {
        userBehaviorLogService.save(event.membershipId(), event.groupId(), event.type(), event.content());

        if (userBehaviorLogService.countPending(event.membershipId()) >= 5) {
            userBehaviorEventPublisher.publishBatchRequested(new UserBehaviorBatchRequestedEvent(event.membershipId()));
        }
    }
}
