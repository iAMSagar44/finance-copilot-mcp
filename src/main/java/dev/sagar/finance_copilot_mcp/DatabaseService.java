package dev.sagar.finance_copilot_mcp;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DatabaseService.class);

	private final JdbcClient jdbcClient;

	private static final String SQL_QUERY_CATEGORY = "SELECT DISTINCT LOWER(category) AS category FROM financial_transaction WHERE date <= :date";

	public DatabaseService(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Tool(description = "Executing SQL queries against the database for data queries.")
	public List<Map<String, Object>> getAggregatedData(
			@ToolParam(description = "The generated SQL query to execute") String sqlQuery) {
		logger.info("Executing SQL query: {}", sqlQuery);
		return jdbcClient.sql(sqlQuery).query().listOfRows(); // Ensure connections are
																// released after query
																// execution
	}

	public List<String> getCategories() {
		try (var query = jdbcClient.sql(SQL_QUERY_CATEGORY)
			.param("date", LocalDate.now())
			.query(String.class)
			.stream()) {
			List<String> categories = query.map(s -> s.toLowerCase()).toList();
			logger.info("Retrieved categories: {}", categories);
			return categories;
		}
	}

}
