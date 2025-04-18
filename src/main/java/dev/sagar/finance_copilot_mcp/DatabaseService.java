package dev.sagar.finance_copilot_mcp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

	private static final org.slf4j.Logger logger =
			org.slf4j.LoggerFactory.getLogger(DatabaseService.class);

	private final JdbcClient jdbcClient;

	private static final String SQL_QUERY_CATEGORY =
			"SELECT DISTINCT LOWER(category) AS category FROM financial_transaction WHERE date <= :date";

	public DatabaseService(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public List<Map<String, Object>> getAggregatedData(String sqlQuery) {
		logger.info("Executing SQL query: {}", sqlQuery);
		var rawResults = jdbcClient.sql(sqlQuery).query().listOfRows();

		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
		for (Map<String, Object> row : rawResults) {
			Object dateObj = row.get("date");
			if (dateObj instanceof java.sql.Date) {
				LocalDate localDate = ((java.sql.Date) dateObj).toLocalDate();
				row.put("date", localDate.format(formatter));
			} else if (dateObj instanceof java.util.Date) {
				Instant instant = ((java.util.Date) dateObj).toInstant();
				LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
				row.put("date", localDate.format(formatter));
			} else if (dateObj instanceof LocalDate) {
				row.put("date", ((LocalDate) dateObj).format(formatter));
			}
		}

		return rawResults;
	}

	public List<String> getCategories() {
		try (var query = jdbcClient.sql(SQL_QUERY_CATEGORY).param("date", LocalDate.now())
				.query(String.class).stream()) {
			List<String> categories = query.map(s -> s.toLowerCase()).toList();
			logger.info("Retrieved categories: {}", categories);
			return categories;
		}
	}

}
