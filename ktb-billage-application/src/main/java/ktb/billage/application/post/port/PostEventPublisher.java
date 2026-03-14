package ktb.billage.application.post.port;

import ktb.billage.application.post.event.PostCreateEvent;

public interface PostEventPublisher {
    void publishPostCreated(PostCreateEvent event);
}
