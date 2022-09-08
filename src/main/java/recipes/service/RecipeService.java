package recipes.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import recipes.dao.RecipeDao;
import recipes.entity.Recipe;
import recipes.exception.DbException;

public class RecipeService {

	private static final String SCHEMA_FILE = "recipe_schema.sql";
	private static final String DATA_FILE = "recipe_data.sql";

	private RecipeDao recipeDao = new RecipeDao();

	public Recipe fetchRecipeById(Integer recipeId) {
		return recipeDao.fetchRecipeById(recipeId)
				.orElseThrow(() -> new NoSuchElementException("Recipe with ID=" + recipeId + " does not exist."));
	}

	public void createAndPopulateTables() {
		loadFromFile(SCHEMA_FILE);
		loadFromFile(DATA_FILE);
	}

	private void loadFromFile(String fileName) {
		String content = readFileContent(fileName);

		List<String> sqlStatements = convertContentToSqlStatement(content);

		recipeDao.executeBatch(sqlStatements);

	}

	// this method converts text from content file to a list of SQL statements
	private List<String> convertContentToSqlStatement(String content) {
		// removes comments
		content = removeComments(content);

		// replaces whitespace with a single space
		content = replaceWhitespaceSequencesWithSingleSpace(content);

		// returns the filtered content put into a list of strings (each string is a sql
		// statement)
		return extractLinesFromContent(content);
	}

	private List<String> extractLinesFromContent(String content) {

		List<String> lines = new LinkedList<>();

		while (!content.isEmpty()) { // while the string is not empty
			int semicolon = content.indexOf(";");

			if (semicolon == -1) {
				if (!content.isBlank()) {
					lines.add(content);
				}

				content = "";
			} else {
				lines.add(content.substring(0, semicolon).trim());
				content = content.substring(semicolon + 1);
			}
		}

		return lines;
	}

	private String replaceWhitespaceSequencesWithSingleSpace(String content) {
		return content.replaceAll("\\s+", " ");
		// \\s+ is a regular expression that represents whitespace
	}

	private String removeComments(String content) {
		StringBuilder builder = new StringBuilder(content);

		int commentPos = 0;

		while ((commentPos = builder.indexOf("--", commentPos)) != -1) {
			// indexOf returns -1 if specified substring is not found

			int eolPos = builder.indexOf("\n", commentPos + 1);

			if (eolPos == -1) {
				builder.replace(commentPos, builder.length(), "");

			} else {
				builder.replace(commentPos, eolPos + 1, "");
			}
		} // end of loop

		return builder.toString();
	}

	private String readFileContent(String fileName) {
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
			return Files.readString(path);

		} catch (Exception e) {
			throw new DbException(e);
		}
	}

	public Recipe addRecipe(Recipe recipe) {
		return recipeDao.insertRecipe(recipe);
	}

	public List<Recipe> fetchRecipes() {
		return recipeDao.fetchAllRecipes();
	}
}
