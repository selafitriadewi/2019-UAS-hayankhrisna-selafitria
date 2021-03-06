package com.selafitriadewi.project_uas.repositories;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.selafitriadewi.project_uas.AppExecutors;
import com.selafitriadewi.project_uas.models.Recipe;
import com.selafitriadewi.project_uas.persistence.RecipeDao;
import com.selafitriadewi.project_uas.persistence.RecipeDatabase;
import com.selafitriadewi.project_uas.requests.ServiceGenerator;
import com.selafitriadewi.project_uas.requests.responses.ApiResponse;
import com.selafitriadewi.project_uas.requests.responses.RecipeResponse;
import com.selafitriadewi.project_uas.requests.responses.RecipeSearchResponse;
import com.selafitriadewi.project_uas.util.Constants;
import com.selafitriadewi.project_uas.util.NetworkBoundResource;
import com.selafitriadewi.project_uas.util.Resource;

import java.util.List;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";
    private static RecipeRepository instance;
    private RecipeDao recipeDao;

    public static RecipeRepository getInstance(Context context){
        if(instance == null){
            instance = new RecipeRepository(context);
        }
        return instance;
    }


    private RecipeRepository(Context context) {
        recipeDao = RecipeDatabase.getInstance(context).getRecipeDao();
    }

    public LiveData<Resource<List<Recipe>>> searchRecipesApi(final String query, final int pageNumber){
        return new NetworkBoundResource<List<Recipe>, RecipeSearchResponse>(AppExecutors.getInstance() ){

            @Override
            public void saveCallResult(@NonNull RecipeSearchResponse item) {
                if(item.getRecipes() != null){
                    Recipe[] recipes = new Recipe[item.getRecipes().size()];

                    int index = 0;

                    for(long rowId : recipeDao.insertRecipes((Recipe[])(item.getRecipes().toArray(recipes)))){
                        if(rowId == -1) {
                            Log.d(TAG, "saveCallResult: CONFLICT... This recipe is already in the cache");
                            recipeDao.updateRecipe(
                                    recipes[index].getRecipe_id(),
                                    recipes[index].getTitle(),
                                    recipes[index].getPublisher(),
                                    recipes[index].getImage_url(),
                                    recipes[index].getSocial_rank()
                            );
                        }
                        index++;
                    }
                }
            }

            @Override
            public boolean shouldFetch(@Nullable List<Recipe> data) {
                return true; // always query the network since the queries can be anything
            }

            @NonNull
            @Override
            public LiveData<List<Recipe>> loadFromDb() {
                return recipeDao.searchRecipes(query, pageNumber);
            }

            @NonNull
            @Override
            public LiveData<ApiResponse<RecipeSearchResponse>> createCall() {
                return ServiceGenerator.getRecipeApi()
                        .searchRecipe(
                                Constants.API_KEY,
                                query,
                                String.valueOf(pageNumber)
                        );
            }

        }.getAsLiveData();
    }


    public LiveData<Resource<Recipe>> searchRecipeApi(final String recipeId){
        return new NetworkBoundResource<Recipe, RecipeResponse>(AppExecutors.getInstance()){

            @Override
            public void saveCallResult(@NonNull RecipeResponse item) {

                // Recipe will be NULL if API key is expired
                if(item.getRecipe() != null){
                    item.getRecipe().setTimestamp((int)(System.currentTimeMillis() / 1000)); // save time in seconds
                    recipeDao.insertRecipe(item.getRecipe());
                }
            }

            @Override
            public boolean shouldFetch(@Nullable Recipe data) {
                Log.d(TAG, "shouldFetch: recipe: " + data.toString());
                int currentTime = (int)(System.currentTimeMillis() / 1000);
                Log.d(TAG, "shouldFetch: current time: " + currentTime);
                int lastRefresh = data.getTimestamp();
                Log.d(TAG, "shouldFetch: last refresh: " + lastRefresh);
                Log.d(TAG, "shouldFetch: it's been " + ((currentTime - lastRefresh) / 60 / 60 / 24)
                        + " days since this recipe was refreshed. 30 days must elapse.");
                if(((System.currentTimeMillis() / 1000) - data.getTimestamp()) >= Constants.RECIPE_REFRESH_TIME){
                    Log.d(TAG, "shouldFetch: SHOULD REFRESH RECIPE? " + true);
                    return true;
                }
                Log.d(TAG, "shouldFetch: SHOULD REFRESH RECIPE? " + false);
                return false;
            }

            @NonNull
            @Override
            public LiveData<Recipe> loadFromDb() {
                return recipeDao.getRecipe(recipeId);
            }

            @NonNull
            @Override
            public LiveData<ApiResponse<RecipeResponse>> createCall() {
                return ServiceGenerator.getRecipeApi().getRecipe(
                        Constants.API_KEY,
                        recipeId
                );
            }

        }.getAsLiveData();
    }
}








