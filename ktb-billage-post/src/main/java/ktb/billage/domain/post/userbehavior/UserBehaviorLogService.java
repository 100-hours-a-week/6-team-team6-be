package ktb.billage.domain.post.userbehavior;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBehaviorLogService {
    private static final int BATCH_SIZE = 5;

    private final UserBehaviorLogRepository userBehaviorLogRepository;

    @Transactional
    public void save(Long membershipId, Long groupId, UserBehaviorType type, String content) {
        userBehaviorLogRepository.save(new UserBehaviorLog(membershipId, groupId, type, content));
    }

    @Transactional(readOnly = true)
    public long countPending(Long membershipId) {
        return userBehaviorLogRepository.countByMembershipIdAndStatus(membershipId, UserBehaviorLogStatus.PENDING);
    }

    @Transactional
    public List<UserBehaviorLog> reserveOldestPending(Long membershipId, String batchKey) {
        List<UserBehaviorLog> logs = userBehaviorLogRepository.findForUpdateByMembershipIdAndStatusOrderByIdAsc(
                membershipId,
                UserBehaviorLogStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE)
        );
        if (logs.size() < BATCH_SIZE) {
            return List.of();
        }
        logs.forEach(log -> log.markProcessing(batchKey));
        return logs;
    }

    @Transactional
    public void releaseBatch(String batchKey) {
        List<UserBehaviorLog> logs = userBehaviorLogRepository.findAllByBatchKeyOrderByIdAsc(batchKey);
        logs.forEach(UserBehaviorLog::markPending);
    }

    @Transactional
    public void deleteBatch(String batchKey) {
        userBehaviorLogRepository.deleteAllByBatchKey(batchKey);
    }
}
