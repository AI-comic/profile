package kopo.aisw.springboot_basic.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Criteria {
    private int pageNum;    // 현재 페이지 번호
    private int amount;     // 페이지당 보여줄 게시글 수

    private String type;    // 검색 유형 (T: 제목, C: 내용, W: 작성자)
    private String keyword; // 검색어

    public Criteria() {
        this(1, 10);    // 기본값: 1페이지, 10개씩
    }

    public Criteria(int pageNum, int amount) {
        this.pageNum = pageNum;
        this.amount = amount;
    }

    // MyBatis에서 LIMIT 구문의 시작 위치를 계산
    public int getOffset() {
        return (pageNum - 1) * amount;
    }

    // 검색 조건을 배열로 변환
    public String[] getTypeArr() {
        return type == null ? new String[]{} : type.split("");
    }
}
