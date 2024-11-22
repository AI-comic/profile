package kopo.aisw.springboot_basic.mapper;

import kopo.aisw.springboot_basic.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void save(User user);
    User findById(String userId);
    void update(User user);
    void delete(String userId);
}
