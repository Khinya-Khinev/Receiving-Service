package com.waregang.receiving_service.test_utils;

import com.redis.testcontainers.RedisContainer;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public abstract class BaseIT {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename(".env")
            .ignoreIfMissing()
            .load();

    private static final String POSTGRES_VERSION = dotenv.get("POSTGRES_VERSION");
    private static final String DB_NAME = dotenv.get("DB_NAME");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    private static final String REDIS_VERSION = dotenv.get("REDIS_VERSION");

    private static final String KAFKA_VERSION = dotenv.get("KAFKA_VERSION");

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:" + POSTGRES_VERSION
    )
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD);

    @Container
    @ServiceConnection
    private static final RedisContainer redis = new RedisContainer(
            DockerImageName.parse("redis:" + REDIS_VERSION)
    );

    @Container
    @ServiceConnection
    private static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:" + KAFKA_VERSION)
    );

    @PersistenceContext
    protected EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        entityManager.getEntityManagerFactory()
                .unwrap(SessionFactoryImplementor.class)
                .getSchemaManager()
                .truncateMappedObjects();
    }
}
