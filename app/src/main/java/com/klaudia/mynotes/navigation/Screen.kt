package com.klaudia.mynotes.navigation

import com.klaudia.mynotes.util.Constants.ADD_EDIT_CATEGORY_ARG_KEY
import com.klaudia.mynotes.util.Constants.ADD_EDIT_SCREEN_ARG_KEY
import com.klaudia.mynotes.util.Constants.MANAGE_CATEGORIES_SCREEN_ARG_KEY

sealed class Screen (val route: String ){
    object AuthenticationScreen: Screen(route = "auth_screen")
    object HomeScreen: Screen(route = "home_screen")
    object AddEditEntryScreen: Screen(route = "add_edit_entry_screen?$ADD_EDIT_SCREEN_ARG_KEY=" +"{$ADD_EDIT_SCREEN_ARG_KEY}&$ADD_EDIT_CATEGORY_ARG_KEY={$ADD_EDIT_CATEGORY_ARG_KEY}"){
        fun getEntryId(entryId: String) = "add_edit_entry_screen?$ADD_EDIT_SCREEN_ARG_KEY=$entryId"
        fun getCategoryId(categoryId: String) = "add_edit_entry_screen?$ADD_EDIT_CATEGORY_ARG_KEY=$categoryId"
    }
    object ManageCategoriesScreen: Screen(route = "manage_categories_screen")

   object ListNotesOfCategoryScreen: Screen(route = "list_notes_of_category_screen?$MANAGE_CATEGORIES_SCREEN_ARG_KEY=" +"{$MANAGE_CATEGORIES_SCREEN_ARG_KEY}"){
        fun getCategoryId(categoryId: String) = "list_notes_of_category_screen?$MANAGE_CATEGORIES_SCREEN_ARG_KEY=$categoryId"
    }
}
