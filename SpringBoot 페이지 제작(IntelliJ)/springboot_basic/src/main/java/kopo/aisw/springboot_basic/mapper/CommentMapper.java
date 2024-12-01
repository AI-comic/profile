package kopo.aisw.springboot_basic.mapper;

import kopo.aisw.springboot_basic.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommentMapper {
    void save(Comment comment);
    List<Comment> findByBoardId(Long boardId);
    void update(Comment comment);
    void delete(Long commentId);
    Comment findById(Long commentId);
}
