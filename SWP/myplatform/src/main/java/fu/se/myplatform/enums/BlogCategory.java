package fu.se.myplatform.enums;

public enum BlogCategory {
    QUIT_JOURNEY("Hành trình cai thuốc"),     // Chia sẻ quá trình cai thuốc của bản thân
    SUCCESS_STORY("Câu chuyện thành công"),    // Chia sẻ sau khi cai thuốc thành công
    EXPERIENCE("Kinh nghiệm cá nhân"),         // Chia sẻ kinh nghiệm, mẹo riêng khi cai
    MOTIVATION("Động lực"),                     // Chia sẻ động lực, lý do cai thuốc
    CHALLENGE("Thách thức và khó khăn"),       // Chia sẻ khó khăn và cách vượt qua
    LIFE_STORY("Chuyện đời thường");          // Chia sẻ ngẫu hứng, câu chuyện cuộc sống

    private final String displayName;

    BlogCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
