package io.dropwizard.jdbi.args;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.io.IOException;
import java.util.OptionalLong;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalLongTest {
    private final Environment env = new Environment("test-optional-long");

    private TestDao dao;

    @BeforeEach
    public void setupTests() throws IOException {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:mem:optional-long-" + UUID.randomUUID() + "?user=sa");
        dataSourceFactory.setInitialSize(1);
        final DBI dbi = new DBIFactory().build(env, dataSourceFactory, "test");
        try (Handle h = dbi.open()) {
            h.execute("CREATE TABLE test (id INT PRIMARY KEY, optional BIGINT)");
        }
        dao = dbi.onDemand(TestDao.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        for (LifeCycle managedObject : env.lifecycle().getManagedObjects()) {
            managedObject.stop();
        }
    }

    @Test
    public void testPresent() {
        dao.insert(1, OptionalLong.of(42L));

        assertThat(dao.findOptionalLongById(1).getAsLong()).isEqualTo(42);
    }

    @Test
    public void testAbsent() {
        dao.insert(2, OptionalLong.empty());

        assertThat(dao.findOptionalLongById(2).isPresent()).isFalse();
    }

    interface TestDao {

        @SqlUpdate("INSERT INTO test(id, optional) VALUES (:id, :optional)")
        void insert(@Bind("id") int id, @Bind("optional") OptionalLong optional);

        @SqlQuery("SELECT optional FROM test WHERE id = :id")
        OptionalLong findOptionalLongById(@Bind("id") int id);
    }
}
