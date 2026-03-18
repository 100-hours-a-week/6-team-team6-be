package ktb.billage.application.userbehavior.port;

import ktb.billage.domain.post.userbehavior.UserBehaviorLog;

import java.util.List;

public interface UserBehaviorAiSyncPort {
    void sync(Long membershipId, Long groupId, List<UserBehaviorLog> recentLogs);
}
