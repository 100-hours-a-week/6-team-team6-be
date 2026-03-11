package ktb.billage.application.chat;

import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.dto.ChatResponse;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.membership.Membership;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.user.User;
import ktb.billage.fixture.Fixtures;
import ktb.billage.support.TestContainerSupport;
import ktb.billage.support.querycount.QueryCountCaptor;
import ktb.billage.support.querycount.QueryCountResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static ktb.billage.support.querycount.QueryCountAssertions.assertQueryCountLessThan;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=ktb.billage.support.querycount.QueryCountCaptor$SqlCaptureStatementInspector"
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

        QueryCountResult<ChatResponse.ChatroomSummaries> queryCountResult =
                QueryCountCaptor.measure(() -> chatFacade.getMyParticipatingChatrooms(me.getId(), null));
        ChatResponse.ChatroomSummaries result = queryCountResult.result();

        assertThat(result.chatroomSummaries()).hasSize(20);
        assertThat(result.cursorDto().hasNext()).isTrue();
        assertThat(queryCountResult.executedSql()).isNotEmpty();
        assertQueryCountLessThan(queryCountResult, 100);

        System.out.println("===== getMyParticipatingChatrooms query count =====");
        System.out.println(queryCountResult.describe());
    }
}
