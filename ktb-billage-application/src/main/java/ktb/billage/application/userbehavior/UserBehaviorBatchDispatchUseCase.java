package ktb.billage.application.userbehavior;

import ktb.billage.application.userbehavior.event.UserBehaviorBatchRequestedEvent;
import ktb.billage.application.userbehavior.port.UserBehaviorAiSyncPort;
import ktb.billage.domain.post.userbehavior.UserBehaviorLog;
import ktb.billage.domain.post.userbehavior.UserBehaviorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserBehaviorBatchDispatchUseCase {
    private final UserBehaviorLogService userBehaviorLogService;
    private final UserBehaviorAiSyncPort userBehaviorAiSyncPort;

    public void handle(UserBehaviorBatchRequestedEvent event) {
        String batchKey = UUID.randomUUID().toString();
        List<UserBehaviorLog> logs = userBehaviorLogService.reserveOldestPending(event.membershipId(), batchKey);
        if (logs.isEmpty()) {
            return;
        }

        Long groupId = logs.getFirst().getGroupId();
        try {
            userBehaviorAiSyncPort.sync(event.membershipId(), groupId, logs);
            userBehaviorLogService.deleteBatch(batchKey);
        } catch (Exception e) {
            userBehaviorLogService.releaseBatch(batchKey);
            throw e;
        }
    }
}
