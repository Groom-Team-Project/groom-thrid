package groom.backend.domain.opendata.repository.impl;

import groom.backend.domain.opendata.entity.ChargerLocation;
import groom.backend.domain.opendata.repository.spec.ChargerLocationRepository;
import groom.backend.domain.opendata.repository.spec.jpa.JpaChargerLocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

import static java.sql.Types.DATE;
import static java.sql.Types.TIME;

@Slf4j
@Repository
public class ChargerLocationRepositoryImpl implements ChargerLocationRepository {

    private static final int BATCH_SIZE = 500;

    private final JpaChargerLocationRepository jpaChargerLocationRepository;
    private final JdbcTemplate jdbcTemplate;

    public ChargerLocationRepositoryImpl(JpaChargerLocationRepository jpaChargerLocationRepository, JdbcTemplate jdbcTemplate) {
        this.jpaChargerLocationRepository = jpaChargerLocationRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deleteAll() {
        log.info("충전소 데이터 전체 삭제 시작");
        jpaChargerLocationRepository.deleteAllInBatch();
    }

    @Override
    public void batchInsert(List<ChargerLocation> items) {
        if (items == null || items.isEmpty()) return;

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO charger_location (")
                .append("facility_name, city_name, district_name, district_code, road_addr, land_addr, lat, lng")
                .append(", description, weekday_start, weekday_end, saturday_start, saturday_end")
                .append(", holiday_start, holiday_end, capacity, is_air_pump, is_charger")
                .append(", manage_org_name, manage_org_contact, data_updated, provider_code, provider_name, crs")
                .append(", created_at, updated_at, version) ")
                .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?")
                .append(", ?, ?, ?, ?, ?, ?, ?, ?")
                .append(", ?, ?, ?, ?, ?, ?, ?, ?, now(), now(), 1) ");
                /*  TODO :: UPDATE는 정책 설정 후 추가
                .append("ON CONFLICT (facility_name, city_name) DO UPDATE SET ")
                .append("district_name = EXCLUDED.district_name, district_code = EXCLUDED.district_code, road_addr = EXCLUDED.road_addr, ")
                .append("land_addr = EXCLUDED.land_addr, lat = EXCLUDED.lat, lng = EXCLUDED.lng, description = EXCLUDED.description, ")
                .append("weekday_start = EXCLUDED.weekday_start, weekday_end = EXCLUDED.weekday_end, saturday_start = EXCLUDED.saturday_start, ")
                .append("saturday_end = EXCLUDED.saturday_end, holiday_start = EXCLUDED.holiday_start, holiday_end = EXCLUDED.holiday_end, ")
                .append("capacity = EXCLUDED.capacity, is_air_pump = EXCLUDED.is_air_pump, is_charger = EXCLUDED.is_charger, ")
                .append("manage_org_name = EXCLUDED.manage_org_name, manage_org_contact = EXCLUDED.manage_org_contact, ")
                .append("data_updated = EXCLUDED.data_updated, provider_code = EXCLUDED.provider_code, provider_name = EXCLUDED.provider_name, ")
                .append("crs = EXCLUDED.crs, updated_at = now(), version = charger_location.version + 1");
                 */

        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            int start = i;
            int end = Math.min(i + BATCH_SIZE, items.size());
            List<ChargerLocation> sub = items.subList(start, end);

            jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int idx) throws SQLException {
                    ChargerLocation it = sub.get(idx);
                    // 아래 getter명은 실제 DTO에 맞게 변경하세요.
                    ps.setString(1, it.getFacilityName());
                    ps.setString(2, it.getCityName());
                    ps.setString(3, it.getDistrictName());
                    ps.setInt(4, it.getDistrictCode());
                    ps.setString(5, it.getRoadAddr());
                    ps.setString(6, it.getLandAddr());
                    ps.setDouble(7, it.getLat());
                    ps.setDouble(8, it.getLng());
                    ps.setString(9, it.getDescription());
                    if (it.getWeekdayStart() != null) {
                        ps.setTime(10, Time.valueOf(it.getWeekdayStart()));
                    } else {
                        ps.setNull(10, TIME);
                    }
                    if (it.getWeekdayEnd() != null) {
                        ps.setTime(11, Time.valueOf(it.getWeekdayEnd()));
                    } else {
                        ps.setNull(11, TIME);
                    }
                    if (it.getSaturdayStart() != null) {
                        ps.setTime(12, Time.valueOf(it.getSaturdayStart()));
                    } else {
                        ps.setNull(12, TIME);
                    }
                    if (it.getSaturdayEnd() != null) {
                        ps.setTime(13, Time.valueOf(it.getSaturdayEnd()));
                    } else {
                        ps.setNull(13, TIME);
                    }
                    if (it.getHolidayStart() != null) {
                        ps.setTime(14, Time.valueOf(it.getHolidayStart()));
                    } else {
                        ps.setNull(14, TIME);
                    }
                    if (it.getHolidayEnd() != null) {
                        ps.setTime(15, Time.valueOf(it.getHolidayEnd()));
                    } else {
                        ps.setNull(15, TIME);
                    }

                    ps.setInt(16, it.getCapacity());
                    ps.setBoolean(17, it.getIsAirPump());
                    ps.setBoolean(18, it.getIsCharger());
                    ps.setString(19, it.getManageOrgName());
                    ps.setString(20, it.getManageOrgContact());
                    if (it.getDataUpdated() != null) {
                        ps.setDate(21, Date.valueOf(it.getDataUpdated()));
                    } else {
                        ps.setNull(21, DATE);
                    }
                    ps.setString(22, it.getProviderCode());
                    ps.setString(23, it.getProviderName());
                    ps.setString(24, it.getCrs());
                }

                @Override
                public int getBatchSize() {
                    return sub.size();
                }
            });
        }
    }
}
