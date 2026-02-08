package ktb.billage.domain.group.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidInvitationGenerator implements InvitationGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
