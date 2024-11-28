package kopo.aisw.springboot_basic.mapper;

import kopo.aisw.springboot_basic.domain.Board;
import kopo.aisw.springboot_basic.domain.Criteria;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardMapper {
    void save(Board board);
    Board findById(Long boardId);
    List<Board> findAll();
    void update(Board board);
    void delete(Long boardId);
    void updateViewCount(Long boardId);

    // 페이징 처리된 게시글 목록 조회
    List<Board> getListWithPaging(Criteria cri);

    // 전체 게시글 수 조회
    int getTotalCount();
}
