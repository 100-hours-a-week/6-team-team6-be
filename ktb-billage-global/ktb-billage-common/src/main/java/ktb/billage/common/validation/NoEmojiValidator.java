package ktb.billage.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoEmojiValidator implements ConstraintValidator<NoEmoji, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            if (isEmoji(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }

        return true;
    }

    private boolean isEmoji(int codePoint) {
        return (codePoint >= 0x1F300 && codePoint <= 0x1FAFF)
                || (codePoint >= 0x1F600 && codePoint <= 0x1F64F)
                || (codePoint >= 0x1F680 && codePoint <= 0x1F6FF)
                || (codePoint >= 0x2600 && codePoint <= 0x26FF)
                || (codePoint >= 0x2700 && codePoint <= 0x27BF)
                || (codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF);
    }
}
