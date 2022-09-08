package recipes;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import recipes.entity.Recipe;
import recipes.exception.DbException;
import recipes.service.RecipeService;

public class Recipes {

	private Scanner scanner = new Scanner(System.in);
	private RecipeService recipeService = new RecipeService();
	private Recipe curRecipe;

	// @formatter:off
	private List<String> operations = List.of(
			"1) Create and populate all tables",
			"2) Add a new recipe",
			"3) List recipes",
			"4) Select working recipe"
	);
	// @formatter:on

	/*
	 * main method prints menu options and asks user for input to make a selection
	 * and executes selection.
	 */
	public static void main(String[] args) {
		new Recipes().displayMenu();
	}

	private void displayMenu() {
		boolean done = false;

		while (!done) {
			try {
				int operation = getOperation();

				switch (operation) {
				case -1:
					done = exitMenu();
					break;

				case 1:
					createTables();
					break;

				case 2:
					addRecipe();
					break;

				case 3:
					listRecipes();
					break;

				case 4:
					setCurrentRecipe();
					break;

				default:
					System.out.println("\n" + operation + " is not a valid operation. Try again.");
					break;
				}
			} catch (Exception e) {
				System.out.println("\nError: " + e.toString() + " Try again.");
			}
		}
	}

	/*
	 * Set and display a selected recipe. Prints recipes and associated recipe IDs.
	 * Asks user to input a recipe ID to select a recipe. If the recipe ID exists in
	 * the list of recipes, that recipe is selected, otherwise error is printed.
	 */
	private void setCurrentRecipe() {
		// print and returns list of recipes
		List<Recipe> recipes = listRecipes();

		// user inputs a recipe ID
		Integer recipeId = getIntInput("Select a recipe ID");

		// ensures curRecipe is null prior to setting current recipe
		curRecipe = null;

		// loops through list to find recipe ID that matches user input ID. sets current
		// recipe to found recipe
		for (Recipe recipe : recipes) {
			if (recipe.getRecipeId().equals(recipeId)) {
				curRecipe = recipeService.fetchRecipeById(recipeId);
				break;
			}
		} // end of loop

		// returns error message if curRecipe is still null (recipe selection has
		// failed)
		if (Objects.isNull(curRecipe)) {
			System.out.println("\nInvalid recipe selected.");
		}

	}

	/*
	 * fetches list of recipes, prints recipes and recipe IDs to console, and
	 * returns list of recipe
	 */
	private List<Recipe> listRecipes() {
		List<Recipe> recipes = recipeService.fetchRecipes();

		System.out.println("\nRecipes:");

		// prints each recipe ID and recipe name
		recipes.forEach(recipe -> System.out.println("\t" + recipe.getRecipeId() + ": " + recipe.getRecipeName()));

		return recipes;
	}

	/*
	 * adds recipe to database and does not include ingredients, steps, and
	 * categories
	 */
	private void addRecipe() {
		// gets recipe info from user
		String name = getStringInput("Enter the recipe name");
		String notes = getStringInput("Enter the recipe notes");
		Integer numServings = getIntInput("Enter number of servings");
		Integer prepMinutes = getIntInput("Enter prep time (in minutes)");
		Integer cookMinutes = getIntInput("Enter cook time (in minutes)");

		LocalTime prepTime = minutesToLocalTime(prepMinutes);
		LocalTime cookTime = minutesToLocalTime(cookMinutes);

		// creates recipe object from user input
		Recipe recipe = new Recipe();

		recipe.setRecipeName(name);
		recipe.setNotes(notes);
		recipe.setNumServings(numServings);
		recipe.setPrepTime(prepTime);
		recipe.setCookTime(cookTime);

		// adds recipe to recipe_table, prints recipe name as confirmation. Exceptions
		// handled in displayMenu()
		Recipe dbRecipe = recipeService.addRecipe(recipe);
		System.out.println("Recipe added: " + dbRecipe);

		// sets current recipe to newly entered recipe
		curRecipe = recipeService.fetchRecipeById(dbRecipe.getRecipeId());
	}

	/*
	 * converts Integer time value to LocalTime object
	 */
	private LocalTime minutesToLocalTime(Integer numMinutes) {
		int min = Objects.isNull(numMinutes) ? 0 : numMinutes;
		int hours = min / 60;
		int minutes = min % 60;

		return LocalTime.of(hours, minutes);
	}

	/*
	 * Drops all tables, recreates tables, and populates tables with data from
	 * resources. Resets data to known, initial state.
	 */
	private void createTables() {
		recipeService.createAndPopulateTables();
		System.out.println("\nTables created and populated.");

	}

	/*
	 * exits menu by setting boolean value "done" to true in displayMenu()
	 */
	private boolean exitMenu() {
		System.out.println("\nExiting Menu...");
		return true;
	}

	/*
	 * prints options to user and asks user to select an option. Entering nothing
	 * returns -1, which exits the application
	 */
	private int getOperation() {
		printOperations();

		Integer op = getIntInput("\nEnter an operation number. (Press Enter to quit)");

		return Objects.isNull(op) ? -1 : op;
	}

	/*
	 * prints operations, with each operation on a separate line
	 */
	private void printOperations() {
		System.out.println("\nPlease select an option");

		// prints each operation using .forEach method that can be called on operations list
		operations.forEach(op -> System.out.println("\t" + op));

		// displays currently selected recipe. prints message that reflects state of current recipe
		if (Objects.isNull(curRecipe)) {
			System.out.println("\nNo recipe currently selected.");
		} else {
			System.out.println("\nSelected Recipe: " + curRecipe);
		}

	}

	/*
	 * gets and converts user String input into a Double
	 */
	@SuppressWarnings("unused")
	private Double getDoubleInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Double.parseDouble(input);

		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	/*
	 * gets and converts user String input into an Integer
	 */
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Integer.parseInt(input);

		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	/*
	 * gets user String input. if nothing is entered, null is returned
	 */
	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String line = scanner.nextLine();

		return line.isBlank() ? null : line.trim();
	}

}
