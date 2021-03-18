package com.techelevator.dao;

import java.util.ArrayList;
import java.util.List;

import com.techelevator.model.Recipe;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.techelevator.model.Meal;

@Service
public class MealSqlDAO implements MealDAO
{
	private JdbcTemplate jdbcTemplate;

	public MealSqlDAO(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Meal> getAll()
	{
		List<Meal> meals = new ArrayList<>();

		String sql = "";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
		while(results.next())
		{
			Meal meal = mapRowToMeal(results);
			meals.add(meal);
		}
		return meals;
	}


	public List<Meal> getById(int mealPlanId)
	{
        List<Meal> meals = new ArrayList<>();

		String sql = "SELECT meal_plan_id" +
					", meal_category" +
					", day_of_week" +
					", meal_date" +
                    " FROM meal_plan_recipes" +
                    " WHERE meal_plan_id = ?" +
                    " GROUP BY meal_plan_id, meal_category, day_of_week, meal_date; ";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, mealPlanId);
		while(results.next())
		{
			Meal meal =  mapRowToMeal(results);
			List<Recipe> recipes = getRecipesByMeal(meal);
			meal.setRecipes(recipes);
            meals.add(meal);
		}

		return meals;
	}

	private List<Recipe> getRecipesByMeal(Meal meal)
    {
        List<Recipe> recipes = new ArrayList<>();

        String sql = "SELECT r.user_id\n" +
                "    , r.recipe_id\n" +
                "    , r.recipe_name\n" +
                "    , r.directions\n" +
                "    , r.number_of_servings\n" +
                "    , r.cooking_time\n" +
                "    , r.difficulty\n" +
                "FROM meal_plan_recipes as m\n" +
                "INNER JOIN recipes as r\n" +
                "    ON m.recipe_id = r.recipe_id\n" +
                "WHERE m.meal_plan_id = ?\n" +
                "    AND m.meal_category = ?\n" +
                "    AND m.meal_date = ?;";

        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql
                                                    , meal.getMealPlanId()
                                                    , meal.getMealCategory()
                                                    , meal.getMealDate());

        while(rows.next())
        {
            Recipe recipe = new Recipe();
            recipe.setUserId(rows.getInt("user_id"));
            recipe.setRecipeId(rows.getInt("recipe_id"));
            recipe.setRecipeName(rows.getString("recipe_name"));
            recipe.setDirections(rows.getString("directions"));
            recipe.setNumberOfServings(rows.getInt("number_of_servings"));
            recipe.setCookingTime(rows.getInt("cooking_time"));
            recipe.setDifficulty(rows.getString("difficulty"));

            recipes.add(recipe);
        }

        return recipes;
    }

	public Meal add(Meal newMeal)
	{
//		List<Meal> recipes = new ArrayList<>();
		
		String sql = "INSERT INTO meal_plan_recipes" + 
				"(" + 
				"meal_plan_id" + 
				", recipe_id" + 
				", meal_category" + 
				", day_of_week" + 
				", meal_date" + 
				")" + 
				" VALUES (1, ?, ?, ?, ?)" + 
				" RETURNING meal_plan_id;";

		SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, Integer.class,
												newMeal.getMealPlanId(),
												newMeal.getRecipeId(),
												newMeal.getMealCategory(),
												newMeal.getDayOfWeek(),
												newMeal.getMealDate());
//		while(rows.next())
//		{
//			Meal meal = new Meal();
//			mapRowToMeal(meal);
//		}
		
		return mapRowToMeal(rows);

	}

	private Meal mapRowToMeal(SqlRowSet rowSet)
	{
		Meal meal = new Meal();
		meal.setMealPlanId(rowSet.getInt("meal_plan_id"));
		meal.setRecipeId(rowSet.getInt("recipe_id"));
		meal.setMealCategory(rowSet.getString("meal_category"));
		meal.setDayOfWeek(rowSet.getString("day_of_week"));
		meal.setMealDate(rowSet.getDate("meal_date").toLocalDate());
		return meal;
	}

}
