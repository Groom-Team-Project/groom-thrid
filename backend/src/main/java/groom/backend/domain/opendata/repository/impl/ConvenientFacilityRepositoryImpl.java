package groom.backend.domain.opendata.repository.impl;

import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ConvenientFacilityResponse;
import groom.backend.domain.opendata.entity.ConvenientFacility;
import groom.backend.domain.opendata.enums.FacilityType;
import groom.backend.domain.opendata.mapper.ConvenientFacilityMapper;
import groom.backend.domain.opendata.repository.spec.ConvenientFacilityRepository;
import groom.backend.domain.opendata.repository.spec.jpa.JpaConvenientFacilityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static java.sql.Types.DATE;

@Slf4j
@Repository
public class ConvenientFacilityRepositoryImpl implements ConvenientFacilityRepository {

    private static final int BATCH_SIZE = 500;

    private final JpaConvenientFacilityRepository jpaConvenientFacilityRepository;
    private final JdbcTemplate jdbcTemplate;

    public ConvenientFacilityRepositoryImpl(JpaConvenientFacilityRepository jpaConvenientFacilityRepository, JdbcTemplate jdbcTemplate) {
        this.jpaConvenientFacilityRepository = jpaConvenientFacilityRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void batchInsert(List<ConvenientFacility> items) {
        if (items == null || items.isEmpty()) return;

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO convenient_facility (")
                .append("facility_id, facility_seq, established_date, lat, lng")
                .append(", facility_name, facility_type, road_addr, is_operating, operation_status_name")
                .append(", created_at, updated_at, version) ")
                .append("VALUES (?, ?, ?, ?, ?")
                .append(", ?, ?, ?, ?, ?")
                .append(", now(), now(), 1) ")
                .append("ON CONFLICT (facility_id) DO UPDATE SET ")
                .append("facility_seq = EXCLUDED.facility_seq, established_date = EXCLUDED.established_date, lat = EXCLUDED.lat, ")
                .append("lng = EXCLUDED.lng, facility_name = EXCLUDED.facility_name, facility_type = EXCLUDED.facility_type, road_addr = EXCLUDED.road_addr, ")
                .append("is_operating = EXCLUDED.is_operating, operation_status_name = EXCLUDED.operation_status_name, ")
                .append("updated_at = now(), version = convenient_facility.version + 1");


        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            int start = i;
            int end = Math.min(i + BATCH_SIZE, items.size());
            List<ConvenientFacility> sub = items.subList(start, end);

            jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int idx) throws SQLException {
                    ConvenientFacility it = sub.get(idx);
                    // 아래 getter명은 실제 DTO에 맞게 변경하세요.
                    ps.setString(1, it.getFacilityId());
                    ps.setLong(2, it.getFacilitySeq());
                    if (it.getEstablishedDate() != null) {
                        ps.setDate(3, Date.valueOf(it.getEstablishedDate()));
                    } else {
                        ps.setNull(3, DATE);
                    }
                    ps.setDouble(4, it.getLat());
                    ps.setDouble(5, it.getLng());
                    ps.setString(6, it.getFacilityName());
                    ps.setString(7, it.getFacilityType());
                    ps.setString(8, it.getRoadAddr());
                    ps.setBoolean(9, it.getIsOperating());
                    ps.setString(10, it.getOperationStatusName());
                }

                @Override
                public int getBatchSize() {
                    return sub.size();
                }
            });
        }
    }

    @Override
    public List<ConvenientFacilityResponse> findByLatBetweenAndLngBetween(FacilityType facilityType, ViewportRequest viewportRequest) {
        List<ConvenientFacility> convenientFacilityList = jpaConvenientFacilityRepository.findByLatBetweenAndLngBetween(facilityType, viewportRequest);
        return ConvenientFacilityMapper.toResponseList(convenientFacilityList);
    }

    @Override
    public ConvenientFacilityResponse findById(Long id) {
        ConvenientFacility convenientFacility = jpaConvenientFacilityRepository.findById(id).orElse(null);
        if (convenientFacility == null) {
            throw new IllegalArgumentException("해당 ID의 편의 시설을 찾을 수 없습니다: " + id);
        }

        return ConvenientFacilityMapper.toResponse(convenientFacility);
    }
}
