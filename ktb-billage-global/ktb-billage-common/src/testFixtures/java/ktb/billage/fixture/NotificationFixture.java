package ktb.billage.fixture;

import ktb.billage.domain.chat.Chatroom;
import ktb.billage.domain.group.Group;
import ktb.billage.domain.notification.Notification;
import ktb.billage.domain.notification.Type;
import ktb.billage.domain.post.Post;
import ktb.billage.domain.user.User;

public final class NotificationFixture {

    private NotificationFixture() {}

    public static Notification one(User user, Group group, Post post) {
        return new Notification(
                user.getId(),
                group.getId(),
                post.getId(),
                null,
                "title - post %d".formatted(post.getId()),
                "description - post %d".formatted(post.getId()),
                Type.POST
        );
    }

    public static Notification one(User user, Group group, Chatroom chatroom) {
        return new Notification(
                user.getId(),
                group.getId(),
                null,
                chatroom.getId(),
                "title - chatroom %d".formatted(chatroom.getId()),
                "description - chatroom %d".formatted(chatroom.getId()),
                Type.CHATROOM
        );
    }
}
