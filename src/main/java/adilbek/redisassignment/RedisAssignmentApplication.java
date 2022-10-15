package adilbek.redisassignment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

@Log4j2
@EnableCaching
@SpringBootApplication
public class RedisAssignmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisAssignmentApplication.class, args);
    }

    @Bean
    public RedisTemplate<String, Map<String, Object>> redisTemplateStandAlone(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Map<String, Object>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    ApplicationRunner applicationRunner(AccountService accountService) {
        return event -> {
            StopWatch first = time(accountService, new StopWatch());//first time
            StopWatch second = time(accountService, new StopWatch());//second time

            log.info("First: {}", first.getLastTaskTimeMillis());
            log.info("Second: {}", second.getLastTaskTimeMillis());
        };
    }

    private static StopWatch time(AccountService accountService, StopWatch stopWatch) throws IOException {
        stopWatch.start();
        List<Person> all = accountService.findAll();
        stopWatch.stop();
//        log.info(all);
        return stopWatch;
    }
}

@Data
@NoArgsConstructor
class Person implements Serializable {
    private List<String> addresses;
    private Set<Integer> houseNumbers;

    public Person(List<String> addresses, Set<Integer> houseNumbers) {
        this.addresses = addresses;
        this.houseNumbers = houseNumbers;
    }
}

@Slf4j
@Service
class AccountService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 500000; i++) {
            people.add(new Person(List.of("address + " + i), Set.of(1)));
        }
        objectMapper.writeValue(new File("C:\\Users\\AAkhmet\\IdeaProjects\\redis-assignment\\src\\main\\resources\\file2.json"), people);
    }

    @Cacheable("people")
    public List<Person> findAll() throws IOException {
        log.info("Find all method was called");
        return objectMapper.readValue(new File("C:\\Users\\AAkhmet\\IdeaProjects\\redis-assignment\\src\\main\\resources\\file2.json"), new TypeReference<List<Person>>() {
        });
    }
}
