package com.example.otoo_java.redis.repository;
import org.springframework.data.repository.CrudRepository;
import com.example.otoo_java.redis.entity.RedisRefreshToken;

public interface RedisRepository extends  CrudRepository<RedisRefreshToken, String> {

}
