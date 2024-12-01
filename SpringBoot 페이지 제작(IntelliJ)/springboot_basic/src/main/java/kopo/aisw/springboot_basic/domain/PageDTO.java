package kopo.aisw.springboot_basic.domain;

import lombok.Getter;

@Getter
public class PageDTO {
    private int startPage; // 시작 페이지 번호
    private int endPage; // 끝 페이지 번호
    private boolean prev, next; // 이전/다음 페이지 존재 여부
    private int total; // 전체 게시글 수
    private Criteria cri; // 현재 페이지, 페이지당 게시글 수

    public PageDTO(Criteria cri, int total) {
        this.cri = cri;
        this.total = total;

        // 현재 페이지를 기준으로 화면에 보여질 마지막 페이지 번호
        this.endPage = (int) (Math.ceil(cri.getPageNum() / 10.0)) * 10;

        // 시작 페이지 번호
        this.startPage = this.endPage - 9;

        // 실제 마지막 페이지 번호
        int realEnd = (int) (Math.ceil((total * 1.0) / cri.getAmount()));

        // 실제 마지막 페이지가 endPage보다 작으면 endPage를 조정
        if (realEnd < this.endPage) {
            this.endPage = realEnd;
        }

        // 이전/다음 페이지 버튼 표시 여부
        this.prev = this.startPage > 1;
        this.next = this.endPage < realEnd;
    }
}
