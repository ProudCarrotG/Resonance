package com.resonance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resonance.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
