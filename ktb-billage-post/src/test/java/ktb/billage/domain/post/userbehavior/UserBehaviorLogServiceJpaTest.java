package ktb.billage.domain.post.userbehavior;

import jakarta.persistence.EntityManager;
import ktb.billage.domain.PostJpaTestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserBehaviorLogService.class)
@ContextConfiguration(classes = PostJpaTestApplication.class)
class UserBehaviorLogServiceJpaTest {
    @Autowired
    private UserBehaviorLogService userBehaviorLogService;

    @Autowired
    private UserBehaviorLogRepository userBehaviorLogRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save: 행동 로그를 PENDING 상태로 저장한다")
    void save_SavesPendingLog() {
        userBehaviorLogService.save(1L, 10L, UserBehaviorType.SEARCH, "캠핑 의자");

        entityManager.flush();
        entityManager.clear();

        List<UserBehaviorLog> logs = userBehaviorLogRepository.findAll();
        assertThat(logs).hasSize(1);

        UserBehaviorLog log = logs.getFirst();
        assertThat(log.getMembershipId()).isEqualTo(1L);
        assertThat(log.getGroupId()).isEqualTo(10L);
        assertThat(log.getType()).isEqualTo(UserBehaviorType.SEARCH);
        assertThat(log.getContent()).isEqualTo("캠핑 의자");
        assertThat(log.getStatus()).isEqualTo(UserBehaviorLogStatus.PENDING);
        assertThat(log.getBatchKey()).isNull();
    }

    @Test
    @DisplayName("countPending: 특정 membership의 PENDING 로그만 센다")
    void countPending_CountsOnlyPendingLogsForMembership() {
        userBehaviorLogService.save(1L, 10L, UserBehaviorType.SEARCH, "a");
        userBehaviorLogService.save(1L, 10L, UserBehaviorType.CLICK, "1");
        userBehaviorLogService.save(2L, 20L, UserBehaviorType.SEARCH, "other");

        List<UserBehaviorLog> firstMembershipLogs = userBehaviorLogRepository.findAll().stream()
                .filter(log -> log.getMembershipId().equals(1L))
                .toList();
        firstMembershipLogs.getFirst().markProcessing("batch-1");

        entityManager.flush();
        entityManager.clear();

        long pendingCount = userBehaviorLogService.countPending(1L);

        assertThat(pendingCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("reserveOldestPending: 5개 이상이면 가장 오래된 5개를 PROCESSING으로 예약한다")
    void reserveOldestPending_ReservesOldestFiveLogs() {
        for (int i = 1; i <= 6; i++) {
            userBehaviorLogService.save(1L, 10L, UserBehaviorType.CLICK, String.valueOf(i));
        }

        List<UserBehaviorLog> reserved = userBehaviorLogService.reserveOldestPending(1L, "batch-1");

        entityManager.flush();
        entityManager.clear();

        assertThat(reserved).hasSize(5);
        assertThat(reserved)
                .extracting(UserBehaviorLog::getContent)
                .containsExactly("1", "2", "3", "4", "5");

        List<UserBehaviorLog> allLogs = userBehaviorLogRepository.findAll().stream()
                .sorted(Comparator.comparingLong(UserBehaviorLog::getId))
                .toList();

        assertThat(allLogs.subList(0, 5))
                .extracting(UserBehaviorLog::getStatus)
                .containsOnly(UserBehaviorLogStatus.PROCESSING);
        assertThat(allLogs.subList(0, 5))
                .extracting(UserBehaviorLog::getBatchKey)
                .containsOnly("batch-1");
        assertThat(allLogs.get(5).getStatus()).isEqualTo(UserBehaviorLogStatus.PENDING);
        assertThat(allLogs.get(5).getBatchKey()).isNull();
    }

    @Test
    @DisplayName("reserveOldestPending: 5개 미만이면 아무것도 예약하지 않는다")
    void reserveOldestPending_ReturnsEmpty_WhenLessThanFiveLogsExist() {
        for (int i = 1; i <= 4; i++) {
            userBehaviorLogService.save(1L, 10L, UserBehaviorType.CLICK, String.valueOf(i));
        }

        List<UserBehaviorLog> reserved = userBehaviorLogService.reserveOldestPending(1L, "batch-1");

        entityManager.flush();
        entityManager.clear();

        assertThat(reserved).isEmpty();
        assertThat(userBehaviorLogRepository.findAll())
                .extracting(UserBehaviorLog::getStatus)
                .containsOnly(UserBehaviorLogStatus.PENDING);
        assertThat(userBehaviorLogRepository.findAll())
                .extracting(UserBehaviorLog::getBatchKey)
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("releaseBatch: 해당 batchKey 로그를 다시 PENDING으로 되돌린다")
    void releaseBatch_ReleasesOnlyMatchingBatch() {
        UserBehaviorLog first = userBehaviorLogRepository.save(new UserBehaviorLog(1L, 10L, UserBehaviorType.SEARCH, "a"));
        UserBehaviorLog second = userBehaviorLogRepository.save(new UserBehaviorLog(1L, 10L, UserBehaviorType.CLICK, "b"));
        UserBehaviorLog third = userBehaviorLogRepository.save(new UserBehaviorLog(1L, 10L, UserBehaviorType.CLICK, "c"));

        first.markProcessing("batch-1");
        second.markProcessing("batch-1");
        third.markProcessing("batch-2");
        entityManager.flush();
        entityManager.clear();

        userBehaviorLogService.releaseBatch("batch-1");

        entityManager.flush();
        entityManager.clear();

        List<UserBehaviorLog> logs = userBehaviorLogRepository.findAll().stream()
                .sorted(Comparator.comparingLong(UserBehaviorLog::getId))
                .toList();

        assertThat(logs.get(0).getStatus()).isEqualTo(UserBehaviorLogStatus.PENDING);
        assertThat(logs.get(0).getBatchKey()).isNull();
        assertThat(logs.get(1).getStatus()).isEqualTo(UserBehaviorLogStatus.PENDING);
        assertThat(logs.get(1).getBatchKey()).isNull();
        assertThat(logs.get(2).getStatus()).isEqualTo(UserBehaviorLogStatus.PROCESSING);
        assertThat(logs.get(2).getBatchKey()).isEqualTo("batch-2");
    }

    @Test
    @DisplayName("deleteBatch: 해당 batchKey 로그만 삭제한다")
    void deleteBatch_DeletesOnlyMatchingBatch() {
        UserBehaviorLog first = userBehaviorLogRepository.save(new UserBehaviorLog(1L, 10L, UserBehaviorType.SEARCH, "a"));
        UserBehaviorLog second = userBehaviorLogRepository.save(new UserBehaviorLog(1L, 10L, UserBehaviorType.CLICK, "b"));
        UserBehaviorLog third = userBehaviorLogRepository.save(new UserBehaviorLog(1L, 10L, UserBehaviorType.CLICK, "c"));

        first.markProcessing("batch-1");
        second.markProcessing("batch-1");
        third.markProcessing("batch-2");
        entityManager.flush();
        entityManager.clear();

        userBehaviorLogService.deleteBatch("batch-1");

        entityManager.flush();
        entityManager.clear();

        List<UserBehaviorLog> remaining = userBehaviorLogRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getContent()).isEqualTo("c");
        assertThat(remaining.getFirst().getBatchKey()).isEqualTo("batch-2");
    }
}
