package groom.backend.domain.report.repository.impl;

import groom.backend.domain.report.entity.Report;
import groom.backend.domain.report.repository.spec.ReportRepository;
import groom.backend.domain.report.repository.spec.jpa.JpaReportRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final JpaReportRepository jpaReportRepository;

    public ReportRepositoryImpl(JpaReportRepository jpaReportRepository) {
        this.jpaReportRepository = jpaReportRepository;
    }

    @Override
    public Report save(Report report) {
        return jpaReportRepository.save(report);
    }

    @Override
    public Optional<Report> findById(Long id) {
        return jpaReportRepository.findById(id);
    }

    @Override
    public List<Report> findByAuthor(String author) {
        return jpaReportRepository.findByAuthor(author);
    }

    @Override
    public List<Report> findByPlaceId(Long placeId) {
        return jpaReportRepository.findByPlaceId(placeId);
    }

    @Override
    public List<Report> findByAuthorAndPlaceId(String author, Long placeId) {
        return jpaReportRepository.findByAuthorAndPlaceId(author, placeId);
    }

    @Override
    public void deleteById(Long id) {
        jpaReportRepository.deleteById(id);
    }

    @Override
    public void deleteAllById(List<Long> ids) {
        jpaReportRepository.deleteAllById(ids);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaReportRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndAuthor(Long id, String author) {
        return jpaReportRepository.existsById(id) && 
               jpaReportRepository.findById(id)
                   .map(report -> report.getAuthor().equals(author))
                   .orElse(false);
    }

    @Override
    public List<Report> findAll() {
        return jpaReportRepository.findAll();
    }
}


