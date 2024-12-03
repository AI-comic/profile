package kopo.aisw.springboot_basic.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FileInfo {
    private Long fileId;
    private Long boardId;
    private String originalFileName;
    private String savedFileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime regDate;
}
