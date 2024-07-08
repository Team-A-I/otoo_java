package com.project.otoo_java.redis.repository;
import org.springframework.data.repository.CrudRepository;
import com.project.otoo_java.redis.entity.RedisRefreshToken;

public interface RedisRepository extends  CrudRepository<RedisRefreshToken, String> {

}
