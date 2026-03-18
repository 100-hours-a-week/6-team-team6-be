package ktb.billage.application.userbehavior.port;

import ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent;
import ktb.billage.application.userbehavior.event.UserBehaviorCapturedEvent;

public interface UserBehaviorEventPublisher {
    void publishCaptured(UserBehaviorCapturedEvent event);

    void publishBatchRequested(UserBehaviorBatchRequestedEvent event);
}
