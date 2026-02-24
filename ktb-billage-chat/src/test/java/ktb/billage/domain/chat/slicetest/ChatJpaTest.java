package ktb.billage.domain.chat.slicetest;

import ktb.billage.domain.chat.ChatMessage;
import ktb.billage.domain.chat.ChatMessageRepository;
import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.chat.ChatroomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = ChatJpaTest.JpaSliceConfig.class)
public class ChatJpaTest {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void buyer_with_null_last_read_counts_all_partner_messages() {
        Chatroom chatroom = saveChatroom(100L, 1L);
        saveMessage(chatroom, 2L, Instant.parse("2025-01-01T00:00:00Z"));
        saveMessage(chatroom, 2L, Instant.parse("2025-01-01T00:01:00Z"));
        saveMessage(chatroom, 1L, Instant.parse("2025-01-01T00:02:00Z"));

        Long result = chatMessageRepository.countAllPartnerMessagesCount(chatroom.getId(), 2L);
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void buyer_with_last_read_counts_only_between_range_in_chatroom() {
        Chatroom chatroom = saveChatroom(101L, 10L);
        ChatMessage firstPartner = saveMessage(chatroom, 20L, Instant.parse("2025-01-02T00:00:00Z"));
        saveMessage(chatroom, 10L, Instant.parse("2025-01-02T00:01:00Z"));
        ChatMessage lastPartner = saveMessage(chatroom, 20L, Instant.parse("2025-01-02T00:02:00Z"));

        Chatroom otherChatroom = saveChatroom(102L, 11L);
        saveMessage(otherChatroom, 20L, Instant.parse("2025-01-02T00:03:00Z"));

        Long result = chatMessageRepository.countUnreadPartnerMessagesBetween(
                chatroom.getId(),
                20L,
                lastPartner.getId(),
                firstPartner.getId()
        );
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void seller_with_null_last_read_counts_all_partner_messages() {
        Chatroom chatroom = saveChatroom(200L, 30L);
        saveMessage(chatroom, 30L, Instant.parse("2025-01-03T00:00:00Z"));
        saveMessage(chatroom, 30L, Instant.parse("2025-01-03T00:01:00Z"));
        saveMessage(chatroom, 999L, Instant.parse("2025-01-03T00:02:00Z"));

        Long result = chatMessageRepository.countAllPartnerMessagesCount(chatroom.getId(), 30L);
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void seller_with_last_read_counts_only_between_range_in_chatroom() {
        Chatroom chatroom = saveChatroom(201L, 40L);
        ChatMessage firstPartner = saveMessage(chatroom, 40L, Instant.parse("2025-01-04T00:00:00Z"));
        saveMessage(chatroom, 888L, Instant.parse("2025-01-04T00:01:00Z"));
        ChatMessage lastPartner = saveMessage(chatroom, 40L, Instant.parse("2025-01-04T00:02:00Z"));

        Long result = chatMessageRepository.countUnreadPartnerMessagesBetween(
                chatroom.getId(),
                40L,
                lastPartner.getId(),
                firstPartner.getId()
        );
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void last_read_equals_last_message_returns_zero() {
        Chatroom chatroom = saveChatroom(300L, 50L);
        saveMessage(chatroom, 60L, Instant.parse("2025-01-05T00:00:00Z"));
        ChatMessage lastPartner = saveMessage(chatroom, 60L, Instant.parse("2025-01-05T00:01:00Z"));

        Long result = chatMessageRepository.countUnreadPartnerMessagesBetween(
                chatroom.getId(),
                60L,
                lastPartner.getId(),
                lastPartner.getId()
        );
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void deleted_messages_are_excluded_from_counts() {
        Chatroom chatroom = saveChatroom(400L, 70L);
        ChatMessage firstPartner = saveMessage(chatroom, 80L, Instant.parse("2025-01-06T00:00:00Z"));
        ChatMessage deletedPartner = saveMessage(chatroom, 80L, Instant.parse("2025-01-06T00:01:00Z"));
        ChatMessage lastPartner = saveMessage(chatroom, 80L, Instant.parse("2025-01-06T00:02:00Z"));

        ReflectionTestUtils.setField(deletedPartner, "deletedAt", Instant.parse("2025-01-06T00:03:00Z"));
        chatMessageRepository.saveAndFlush(deletedPartner);

        Long result = chatMessageRepository.countUnreadPartnerMessagesBetween(
                chatroom.getId(),
                80L,
                lastPartner.getId(),
                firstPartner.getId()
        );
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void multiple_chatrooms_keep_input_order() {
        Chatroom chatroom1 = saveChatroom(500L, 90L);
        saveMessage(chatroom1, 100L, Instant.parse("2025-01-07T00:00:00Z"));
        saveMessage(chatroom1, 100L, Instant.parse("2025-01-07T00:01:00Z"));

        Chatroom chatroom2 = saveChatroom(501L, 91L);
        saveMessage(chatroom2, 110L, Instant.parse("2025-01-07T00:02:00Z"));

        Long result1 = chatMessageRepository.countAllPartnerMessagesCount(chatroom1.getId(), 100L);
        Long result2 = chatMessageRepository.countAllPartnerMessagesCount(chatroom2.getId(), 110L);

        assertThat(result1).isEqualTo(2L);
        assertThat(result2).isEqualTo(1L);
    }

    private Chatroom saveChatroom(Long postId, Long buyerId) {
        return chatroomRepository.saveAndFlush(new Chatroom(postId, buyerId));
    }

    private ChatMessage saveMessage(Chatroom chatroom, Long senderId, Instant sendAt) {
        return chatMessageRepository.saveAndFlush(new ChatMessage(senderId, chatroom, "message", sendAt));
    }

    @Configuration
    @EnableJpaRepositories(basePackageClasses = {ChatroomRepository.class, ChatMessageRepository.class})
    @EntityScan(basePackages = "ktb.billage.domain")
    static class JpaSliceConfig {
    }
}
