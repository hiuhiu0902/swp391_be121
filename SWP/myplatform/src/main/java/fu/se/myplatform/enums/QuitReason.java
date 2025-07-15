package fu.se.myplatform.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum QuitReason {
    HEALTH,
    FAMILY_FRIENDS,
    DOCTOR_RECOMMENDED,
    SAVE_MONEY,
    GOOD_EXAMPLE,
    BETTER_FUTURE,
    TAKE_CONTROL,
    ENVIRONMENT,
    LOOK_SMELL_BETTER,
    FOR_PETS,
    HARD_TO_FIND_PLACES,
    BABY_ON_THE_WAY;
    @JsonCreator
    public static QuitReason fromString(String value) {
        return QuitReason.valueOf(value.toUpperCase()); // Chuyển chuỗi thành chữ hoa trước khi so khớp
    }

}

