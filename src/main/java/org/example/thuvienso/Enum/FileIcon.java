package org.example.thuvienso.Enum;


public enum FileIcon {

    PDF("icons/pnj.png"),
    WORD("icons/word.png"),
    EXCEL("icons/excel.png"),
    POWERPOINT("icons/powerpoint.png"),
    VIDEO("icons/video.png"),
    AUDIO("icons/audio.png"),
    ARCHIVE("icons/archive.png"),
    TXT("icons/txt.png"),
    DEFAULT("icons/file.png");

    private final String objectName;

    FileIcon(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }
}
