package dev.sagar.finance_copilot_mcp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class PersonalFinanceService {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PersonalFinanceService.class);

	private static final String SYSTEM_PROMPT = """
			Your job is to answer questions about a user's personal finances by generating SQL queries, executing it and analysing the data. You have the following rules:

			A. 	Provide a valid SQL query based on the table schema and user's question, ensuring the query is:
				1.	Well-formed and compatible with the PostgreSQL database.
				2.	Compliant with these rules:
			        Convert transaction_detail and category columns to lowercase.
			        Use the LIKE operator for filtering on transaction_detail.
					For questions about expenses or spending, filter transactions where transaction_type is 'DEBIT'. For questions about income or credits, filter transactions where transaction_type is 'CREDIT'.
				    Do not invent categories - Retrieve the list of categories and only select the appropriate category or categories based on the question if explicitly relevant to the question.
				    Produce only SELECT queries (no INSERT, UPDATE, DELETE, or schema modifications).
				    Exclude the ID column unless explicitly requested.
				    Provide a detailed breakdown of transactions only if the question requests it.
				    List transactions only if explicitly requested.
				    If the question requires an unsupported operation, state that it isn’t supported.
				    If the DDL does not support answering the question, state that explicitly.
				3. 	Generate a raw SQL query, with no markdown or extra punctuation.

				The transaction_type column is restricted to only two possible values: 'DEBIT' and 'CREDIT'.

			B. 	ALWAYS validate the generated SQL query before executing it.
					If any issues or improvements are identified during validation or execution, update the query based on the feedback.
					Retry up to 3 times, refining the query each time based on the latest feedback.
					If the query still fails after 3 attempts, return the most improved version along with the final feedback or error message.

			C. 	Execute the SQL query against the database and return the results.
					For data inquiries, present results in tables by default.
				    Only provide a detailed breakdown of the transactions if requested by the user.
					Only list the transactions if specifically requested by the user.
					If the query returns no results, provide a message indicating that no results were found and seek clarification from the user to help refine the query.
					If the SQL query cannot be generated or if the query fails, retry up to 2 times before giving up.
					If all retries fail, politely ask the user to refine their question or break it into smaller, more specific tasks.
					The currency is in AUD (Australian Dollar).
						""";

	@Value("classpath:/prompts/schema.sql")
	private Resource ddlResource;

	@Value("${app.database.type}")
	private String databaseType;

	private JdbcClient jdbcClient;

	private final SQLEvaluatorService evaluationService;

	public PersonalFinanceService(JdbcClient jdbcClient, SQLEvaluatorService evaluationService) {
		this.jdbcClient = jdbcClient;
		this.evaluationService = evaluationService;
	}

	@Tool(description = SYSTEM_PROMPT)
	public void generateSQL(@ToolParam(description = "The user's question") String question) {

		logger.info("Generating SQL query for question: {}", question);

	}

	@Tool(description = "Retrieve the table schema to generate the SQL query")
	public String retrieveTableSchema() throws IOException {
		logger.info("Retrieving table schema");
		return ddlResource.getContentAsString(Charset.defaultCharset());
	}

	@Tool(description = "Retrieve the list of categories to generate the SQL query based on the user's question")
	public String retrieveListOfCategories() {
		logger.info("Retrieving list of categories");
		return String.join(",", jdbcClient.sql("SELECT category FROM categories").query(String.class).list());
	}

	@Tool(description = "Validate the generated SQL query based on the user's question")
	public EvaluationResponse validateSQLQuery(@ToolParam(description = "The user's question") String question,
			@ToolParam(description = "The generated SQL query to validate") String sqlQuery) throws IOException {
		logger.info("Validating SQL query: {}", sqlQuery);
		return evaluationService.evaluateQuery(question, sqlQuery);
	}

	@Tool(description = """
			Execute the generated SQL query based on the user's financial question against the database for data queries.
			""")
	public List<Map<String, Object>> executeSQLQuery(
			@ToolParam(description = "The generated SQL query to execute") String sqlQuery) {
		logger.info("Executing SQL query: {}", sqlQuery);
		return jdbcClient.sql(sqlQuery).query().listOfRows();
	}

}
