package com.selafitriadewi.project_uas.requests.responses;

public class CheckRecipeApiKey {

    static boolean isRecipeApiKeyValid(RecipeSearchResponse response){
        return response.getError() == null;
    }

    static boolean isRecipeApiKeyValid(RecipeResponse response){
        return response.getError() == null;
    }
}

