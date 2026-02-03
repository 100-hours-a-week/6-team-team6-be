package ktb.billage.infra.nickname;

import ktb.billage.contract.user.NicknameGenerator;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomNicknameGenerator implements NicknameGenerator {
    private static final String ALNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int NICKNAME_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder(NICKNAME_LENGTH);
        for (int i = 0; i < NICKNAME_LENGTH; i++) {
            int index = RANDOM.nextInt(ALNUM.length());
            builder.append(ALNUM.charAt(index));
        }
        return builder.toString();
    }
}
