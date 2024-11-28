package kopo.aisw.springboot_basic.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Board {
    private Long boardId;
    private String title;
    private String content;
    private String userId;
    private int viewCount;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
