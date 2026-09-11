package org.example.thuvienso.Enum;

import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;

public enum TypeFile {

    PDF("application/pdf"),

    MP4("video/mp4"),
    MP3("audio/mpeg"),

    PNG("image/png"),

    JPG("image/jpeg"),

    DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

    PPT("application/vnd.ms-powerpoint"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),

    XLS("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

    ZIP("application/zip");


    private final String mimeType;

    TypeFile(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static TypeFile fromMimeType(String mimeType) {
        for (TypeFile type : values()) {
            if (type.getMimeType().equalsIgnoreCase(mimeType)) {
                return type;
            }
        }

        throw new AppException(ErrorCode.FILE_NOT_SUPPORTED);
    }
}