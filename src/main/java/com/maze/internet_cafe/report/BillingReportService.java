package com.maze.internet_cafe.report;

import com.maze.internet_cafe.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillingReportService {

    private final SessionRepository sessionRepository;

    public BillingReportService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<DailyReportDto> dailyReport(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atTime(LocalTime.MAX);
        List<Object[]> rows = sessionRepository.dailyAggregationByDate(from, to);
        List<DailyReportDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            // r[0] should be java.sql.Date or java.time.LocalDate depending on dialect
            LocalDate day;
            if (r[0] instanceof java.sql.Date) {
                day = ((java.sql.Date) r[0]).toLocalDate();
            } else if (r[0] instanceof java.time.LocalDate) {
                day = (java.time.LocalDate) r[0];
            } else {
                day = LocalDate.now(); // fallback, should not happen
            }
            long totalSessions = ((Number) r[1]).longValue();
            long totalMinutes = r[2] == null ? 0L : ((Number) r[2]).longValue();
            BigDecimal totalRevenue = r[3] == null ? BigDecimal.ZERO : new BigDecimal(r[3].toString());
            result.add(new DailyReportDto(day, totalSessions, totalMinutes, totalRevenue));
        }
        return result;
    }
}
