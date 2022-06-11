package org.railwaystations.rsapi.adapter.out.db;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.railwaystations.rsapi.core.model.Coordinates;
import org.railwaystations.rsapi.core.model.License;
import org.railwaystations.rsapi.core.model.Photo;
import org.railwaystations.rsapi.core.model.Station;
import org.railwaystations.rsapi.core.model.Statistic;
import org.railwaystations.rsapi.core.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StationDao {

    String JOIN_QUERY = """
                SELECT s.countryCode, s.id, s.DS100, s.title, s.lat, s.lon, s.active,
                        p.urlPath, p.license, p.createdAt, p.outdated, u.id AS photographerId,
                        u.name, u.url AS photographerUrl, u.license AS photographerLicense, u.anonymous
                FROM countries c
                    LEFT JOIN stations s ON c.id = s.countryCode
                    LEFT JOIN photos p ON p.countryCode = s.countryCode AND p.id = s.id
                    LEFT JOIN users u ON u.id = p.photographerId
                """;

    @SqlQuery(JOIN_QUERY + " WHERE c.active = true AND s.countryCode IN (<countryCodes>)")
    @RegisterRowMapper(StationMapper.class)
    Set<Station> findByCountryCodes(@BindList("countryCodes") Set<String> countryCodes);

    @SqlQuery(JOIN_QUERY + " WHERE c.active = true")
    @RegisterRowMapper(StationMapper.class)
    Set<Station> all();

    @SqlQuery(JOIN_QUERY + " WHERE (s.countryCode = :countryCode OR :countryCode IS NULL) AND s.id = :id")
    @RegisterRowMapper(StationMapper.class)
    Set<Station> findByKey(@Bind("countryCode") String countryCode, @Bind("id") String id);

    @SqlQuery(JOIN_QUERY + " WHERE s.id = :id")
    @RegisterRowMapper(StationMapper.class)
    Set<Station> findById(@Bind("id") String id);

    @SqlQuery("""
        SELECT :countryCode countryCode, COUNT(*) stations, COUNT(p.urlPath) photos, COUNT(distinct p.photographerId) photographers
        FROM stations s
            LEFT JOIN photos p ON p.countryCode = s.countryCode AND p.id = s.id
        WHERE s.countryCode = :countryCode OR :countryCode IS NULL
        """)
    @RegisterRowMapper(StatisticMapper.class)
    @SingleValue
    Statistic getStatistic(@Bind("countryCode") String countryCode);

    @SqlQuery("""
        SELECT u.name photographer, COUNT(*) photocount
        FROM stations s
            JOIN photos p ON p.countryCode = s.countryCode AND p.id = s.id
            JOIN users u ON u.id = p.photographerId
        WHERE s.countryCode = :countryCode OR :countryCode IS NULL
        GROUP BY u.name
        ORDER BY COUNT(*) DESC
        """)
    @KeyColumn("photographer")
    @ValueColumn("photocount")
    Map<String, Long> getPhotographerMap(@Bind("countryCode") String countryCode);

    @SqlUpdate("INSERT INTO stations (countryCode, id, title, lat, lon, ds100, active) VALUES (:key.country, :key.id, :title, :coordinates?.lat, :coordinates?.lon, :DS100, :active)")
    void insert(@BindBean Station station);

    @SqlUpdate("DELETE FROM stations WHERE countryCode = :country AND id = :id")
    void delete(@BindBean Station.Key key);

    @SqlUpdate("UPDATE stations SET active = :active WHERE countryCode = :key.country AND id = :key.id")
    void updateActive(@BindBean("key") Station.Key key, @Bind("active") boolean active);

    @SqlQuery(JOIN_QUERY + " WHERE createdAt > :since ORDER BY createdAt DESC")
    @RegisterRowMapper(StationMapper.class)
    List<Station> findRecentImports(@Bind("since") Instant since);

    /**
     * Count nearby stations using simple pythagoras (only valid for a few km)
     */
    @SqlQuery("SELECT COUNT(*) FROM stations WHERE SQRT(POWER(71.5 * (lon - :lon),2) + POWER(111.3 * (lat - :lat),2)) < 0.5")
    int countNearbyCoordinates(@BindBean Coordinates coordinates);

    @SqlQuery("SELECT MAX(CAST(substring(id,2) AS INT)) FROM stations WHERE id LIKE 'Z%'")
    int getMaxZ();

    @SqlUpdate("UPDATE stations SET title = :new_title WHERE countryCode = :key.country AND id = :key.id")
    void changeStationTitle(@BindBean Station station, @Bind("new_title") String newTitle);

    @SqlUpdate("UPDATE stations SET lat = :coords.lat, lon = :coords.lon WHERE countryCode = :key.country AND id = :key.id")
    void updateLocation(@BindBean("key") Station.Key key, @BindBean("coords") Coordinates coordinates);

    class StationMapper implements RowMapper<Station> {

        public Station map(ResultSet rs, StatementContext ctx) throws SQLException {
            var key = new Station.Key(rs.getString("countryCode"), rs.getString("id"));
            var photoUrlPath = rs.getString("urlPath");
            Photo photo = null;
            if (photoUrlPath != null) {
                var photographer = User.builder()
                        .name(rs.getString("name"))
                        .url(rs.getString("photographerUrl"))
                        .license(License.valueOf(rs.getString("photographerLicense")))
                        .id(rs.getInt("photographerId"))
                        .anonymous(rs.getBoolean("anonymous"))
                        .build();
                photo = Photo.builder()
                        .stationKey(key)
                        .urlPath(photoUrlPath)
                        .photographer(photographer)
                        .createdAt(rs.getTimestamp("createdAt").toInstant())
                        .license(License.valueOf(rs.getString("license")))
                        .outdated(rs.getBoolean("outdated"))
                        .build();
            }
            return new Station(key, rs.getString("title"),
                    new Coordinates(rs.getDouble("lat"), rs.getDouble("lon")),
                    rs.getString("DS100"), photo, rs.getBoolean("active"));
        }

    }

    class StatisticMapper implements RowMapper<Statistic> {
        @Override
        public Statistic map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Statistic(rs.getString("countryCode"), rs.getLong("stations"), rs.getLong("photos"), rs.getLong("photographers"));
        }
    }
}
