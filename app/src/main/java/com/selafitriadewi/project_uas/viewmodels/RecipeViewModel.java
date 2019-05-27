package com.selafitriadewi.project_uas.viewmodels;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.selafitriadewi.project_uas.models.Recipe;
import com.selafitriadewi.project_uas.repositories.RecipeRepository;
import com.selafitriadewi.project_uas.util.Resource;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRecipeRepository;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        mRecipeRepository = RecipeRepository.getInstance(application);
    }

    public LiveData<Resource<Recipe>> searchRecipeApi(String recipeId){
        return mRecipeRepository.searchRecipeApi(recipeId);
    }


}