package ktb.billage.application.chat;

import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.domain.chat.service.ChatMessageCommandService;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.TestContainerSupport;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=ktb.billage.application.chat.ChatFacadeQueryCountTest$SqlCaptureStatementInspector"
})
class ChatFacadeQueryCountTest extends TestContainerSupport {

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private ChatFacade chatFacade;

    @Test
    void getMyParticipatingChatrooms_요청당_쿼리개수와_SQL을_확인한다() {
        String suffix = String.valueOf(System.nanoTime());
        User me = fixtures.유저_생성("chat-query-count-me-" + suffix);

        for (int i = 0; i < 21; i++) {
            Group group = fixtures.그룹_생성("chat-query-count-group-" + i);
            Membership myMembership = fixtures.그룹_가입(group, me);

            User partner = fixtures.유저_생성("chat-query-count-partner-" + i + "-" + suffix);
            Membership partnerMembership = fixtures.그룹_가입(group, partner);

            Post post = fixtures.게시글_생성(partnerMembership, i + 1);
            Chatroom chatroom = fixtures.채팅방_생성(post, myMembership);
            fixtures.채팅_전송(chatroom, partnerMembership, i);
        }

        SqlCaptureStatementInspector.clear();

        ChatResponse.ChatroomSummaries result = chatFacade.getMyParticipatingChatrooms(me.getId(), null);

        List<String> executedSql = SqlCaptureStatementInspector.snapshot();
        Map<String, Long> queryCounts = aggregateQueryCounts(executedSql);

        assertThat(result.chatroomSummaries()).hasSize(20);
        assertThat(result.cursorDto().hasNext()).isTrue();
        assertThat(executedSql).isNotEmpty();

        System.out.println("===== getMyParticipatingChatrooms query count =====");
        System.out.println("total: " + executedSql.size());
        queryCounts.forEach((sql, count) -> System.out.println(count + "x | " + sql));
    }

    private Map<String, Long> aggregateQueryCounts(List<String> sqls) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String sql : sqls) {
            counts.merge(normalizeWhitespace(sql), 1L, Long::sum);
        }
        return counts;
    }

    private String normalizeWhitespace(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    public static final class SqlCaptureStatementInspector implements StatementInspector {
        private static final List<String> CAPTURED_SQL = new CopyOnWriteArrayList<>();

        @Override
        public String inspect(String sql) {
            if (sql != null && !sql.isBlank()) {
                CAPTURED_SQL.add(sql);
            }
            return sql;
        }

        static void clear() {
            CAPTURED_SQL.clear();
        }

        static List<String> snapshot() {
            return List.copyOf(CAPTURED_SQL);
        }
    }
}
