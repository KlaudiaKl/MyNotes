package com.klaudia.mynotes.navigation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.klaudia.mynotes.R
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Image
import com.klaudia.mynotes.model.rememberImagesState

import com.klaudia.mynotes.presentation.components.AddNewCategoryDialog
import com.klaudia.mynotes.presentation.components.AlertDialog
import com.klaudia.mynotes.presentation.components.BottomSheetContent
import com.klaudia.mynotes.presentation.components.CategorySelectionDialog
import com.klaudia.mynotes.presentation.screens.add_edit.AddEditEntryScreen
import com.klaudia.mynotes.presentation.screens.add_edit.AddEditViewModel
import com.klaudia.mynotes.presentation.screens.authentication.AuthenticationScreen
import com.klaudia.mynotes.presentation.screens.authentication.AuthenticationViewModel
import com.klaudia.mynotes.presentation.screens.home.HomeScreen
import com.klaudia.mynotes.presentation.screens.home.HomeViewModel
import com.klaudia.mynotes.presentation.screens.list_notes_of_category.ListNotesOfCatViewModel
import com.klaudia.mynotes.presentation.screens.list_notes_of_category.ListNotesOfCategoryScreen
import com.klaudia.mynotes.presentation.screens.manage_categories.ManageCategoriesScreen
import com.klaudia.mynotes.presentation.screens.manage_categories.ManageCategoriesViewModel
import com.klaudia.mynotes.util.Constants.ADD_EDIT_CATEGORY_ARG_KEY
import com.klaudia.mynotes.util.Constants.ADD_EDIT_SCREEN_ARG_KEY
import com.klaudia.mynotes.util.Constants.APP_ID
import com.klaudia.mynotes.util.Constants.MANAGE_CATEGORIES_SCREEN_ARG_KEY
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController
) {
    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationScreenRoute(
            navigateToHomeScreen = {
                navController.popBackStack()
                navController.navigate(Screen.HomeScreen.route)
            }
        )
        homeScreenRoute(
            navigateToAuthenticationScreen = {
                navController.popBackStack()
                navController.navigate(Screen.AuthenticationScreen.route)
            },
            navigateToAddEditScreenWithArgs = {
                navController.navigate(Screen.AddEditEntryScreen.getEntryId(entryId = it))
            },
            navigateToAddEditScreen = { navController.navigate(Screen.AddEditEntryScreen.route) },
            navigateToManageCategoriesScreen = {
                navController.navigate(Screen.ManageCategoriesScreen.route)
            },
            navigateToListNotesOfCategory = {
                navController.navigate(Screen.ListNotesOfCategoryScreen.getCategoryId(categoryId = it))
                Log.d("Nav controller catId", it)
            }
        )

        addEditScreenRoute(
            navigateBack = {
                navController.popBackStack()
            },
           // navigateToManageCategoriesScreen = {
             //   navController.navigate(Screen.ManageCategoriesScreen.route)
          //  }
        )

        manageCategoriesRoute(
            navigateBack = {
                navController.popBackStack()
            },
            navigateToListNotesOfCategory = {
                navController.navigate(Screen.ListNotesOfCategoryScreen.getCategoryId(categoryId = it))
                Log.d("Nav controller catId", it)
            }
        )

        listNotesOfCategoryRoute(
            navigateToAddEditScreenWithArgs = {
                navController.navigate(Screen.AddEditEntryScreen.getEntryId(entryId = it))
            },
            navigateToAddEditScreenWithCategoryArg = {
                navController.navigate(Screen.AddEditEntryScreen.getCategoryId(categoryId = it))
            },
        )
    }
}

//extension functions
//AUTHENTICATION
fun NavGraphBuilder.authenticationScreenRoute(
    navigateToHomeScreen: () -> Unit
) {
    composable(route = Screen.AuthenticationScreen.route) {
        val viewModel: AuthenticationViewModel = hiltViewModel()
        val loadingState by viewModel.loadingState

        AuthenticationScreen(
            authenticated = false,
            loadingState = loadingState,
            navigateToHomeScreen = {
                navigateToHomeScreen()
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeScreenRoute(
    navigateToAuthenticationScreen: () -> Unit,
    navigateToAddEditScreen: () -> Unit,
    navigateToAddEditScreenWithArgs: (String) -> Unit,
    navigateToManageCategoriesScreen: () -> Unit,
    navigateToListNotesOfCategory: (String) -> Unit
) {


    composable(route = Screen.HomeScreen.route) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember { mutableStateOf(false) }
        var newCategoryDialogOpened by remember { mutableStateOf(false) }
        var category by remember { mutableStateOf(Category()) }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val viewModel: HomeViewModel = hiltViewModel()
        //val viewModel: SharedViewModel = hiltViewModel()
        val notes by viewModel.notesWithCategories.collectAsState()
        val categories by viewModel.categories

        HomeScreen(
            drawerState = drawerState,
            navigateToAddEditScreen = { navigateToAddEditScreen() },
            navigateToAddEditScreenWithArgs = navigateToAddEditScreenWithArgs,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onSignedOutClicked = {
                signOutDialogOpened = true
            },
            noteEntries = notes,
            onAddCategoryClicked = {
                category = it
                newCategoryDialogOpened = true
            },
            onManageCategoriesClicked = { navigateToManageCategoriesScreen() },
            usersCategories = categories,
            onCategoryClick = navigateToListNotesOfCategory,
            onSortButtonClick = { viewModel.toggleSortOrder() }
        )
        AddNewCategoryDialog(
            title = stringResource(R.string.add_category),
            text = context.getString(R.string.category_name),
            onDialogOpen = newCategoryDialogOpened,
            onDialogClosed = { newCategoryDialogOpened = false },
            onCategoryAdded = { name, categoryColor ->
                scope.launch(Dispatchers.IO) {
                    viewModel.insertCategory(
                        category,
                        name = name,
                        onSuccess = { newCategoryDialogOpened = false },
                        onError = { message ->
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("ADD CATEGORY", message)
                        },
                        color = categoryColor
                    )
                }
            }
        )
        AlertDialog(
            title = stringResource(id = R.string.sign_out),
            message = context.getString(R.string.sign_out_from_your_google_account),
            dialogOpened = signOutDialogOpened,
            onDialogClosed = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuthenticationScreen()
                        }
                    }
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.addEditScreenRoute(
    navigateBack: () -> Unit,
    //navigateToManageCategoriesScreen: () -> Unit
) {
    composable(
        route = Screen.AddEditEntryScreen.route,
        arguments = listOf(
            navArgument(name = ADD_EDIT_SCREEN_ARG_KEY) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(name = ADD_EDIT_CATEGORY_ARG_KEY) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        val viewModel: AddEditViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val context = LocalContext.current
        var deleteNoteDialogOpened by remember { mutableStateOf(false) }
        var leaveDialogOpened by remember { mutableStateOf(false) }
        var selectCategory by remember { mutableStateOf(false) }
        //var currentNote: Note by remember { mutableStateOf(Note) }
        val categories by viewModel.categories
        val shareIntentLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {})
        val shareNoteEvent = viewModel.shareNoteEvent.collectAsState(initial = null).value
        val imagesState = viewModel.imagesState



        LaunchedEffect(shareNoteEvent) {
            shareNoteEvent?.let {
                shareIntentLauncher.launch(it)
            }
        }

        AddEditEntryScreen(
            onTitleChanged = { viewModel.setTitle(title = it) },
            onContentChanged = { viewModel.setContent(content = it) },
            onBackPressed = { leaveDialogOpened = true },
            onSaveClicked = {
                viewModel.upsertNote(
                    note = it,
                    onSuccess = navigateBack,
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            uiState = uiState,
            onDeleteClicked = {
                deleteNoteDialogOpened = true
            },
            onManageCategoriesClicked = {
                selectCategory = true
                viewModel.getCategories()
                //navigateToManageCategoriesScreen
            },
            onFontSizeChange = { newSize ->
                viewModel.setFontSize(size = newSize)
            },
            imagesState = imagesState,
            onShareClicked = {
                viewModel.prepareShareNoteIntent(uiState.title, uiState.content)
            },
            onImageSelect = {
                val type = context.contentResolver.getType(it)?.split("/")?.last() ?: "jpg"
                viewModel.addImage(
                    image = it,
                    imageType = type
                )
            },
            onImageDeleteClicked = {
                imagesState.deleteImage(it)
            }
        )

        CategorySelectionDialog(
            noteCatId = uiState.categoryId,
            categories = categories,
            onDialogOpen = selectCategory,
            onConfirm = {
                viewModel.setCategoryId(it)
                Log.d("catIdNavGraph", uiState.categoryId ?: "no category id")
            },
            onDialogClosed = { selectCategory = false })

        AlertDialog(
            title = stringResource(id = R.string.delete_entry),
            message = stringResource(id = R.string.permanently_delete_this_entry),
            dialogOpened = deleteNoteDialogOpened,
            onDialogClosed = { deleteNoteDialogOpened = false },
            onYesClicked = {
                viewModel.deleteNote(
                    onSuccess = {
                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                        navigateBack()
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            if (it == "No Internet Connection.")
                                "We need an Internet Connection for this operation."
                            else it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                )
            }
        )
        AlertDialog(
            title = context.getString(R.string.leave),
            message = context.getString(R.string.changes_won_t_be_saved_if_you_don_t_click_the_save_button),
            dialogOpened = leaveDialogOpened,
            onDialogClosed = { leaveDialogOpened = false },
            onYesClicked = {
                navigateBack()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.manageCategoriesRoute(
    navigateBack: () -> Unit,
    navigateToListNotesOfCategory: (String) -> Unit
) {
    composable(route = Screen.ManageCategoriesScreen.route) {
        // val viewModel: SharedViewModel = hiltViewModel()
        val viewModel: ManageCategoriesViewModel = hiltViewModel()
        var deleteCategoryDialogOpened by remember { mutableStateOf(false) }
        var renameCategoryDialogOpened by remember { mutableStateOf(false) }
        var newCategoryDialogOpened by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val uiState = viewModel.categoryUiState
        val isBottomSheetVisible = remember { mutableStateOf(false) }
        val bottomSheetState = rememberBottomSheetScaffoldState()
        var category by remember { mutableStateOf(Category()) }
        val categories by viewModel.categories

        LaunchedEffect(bottomSheetState.bottomSheetState.currentValue) {
            isBottomSheetVisible.value =
                bottomSheetState.bottomSheetState.currentValue == SheetValue.Expanded
        }
        fun toggleBottomSheetState() {
            isBottomSheetVisible.value = !isBottomSheetVisible.value
        }
        if (isBottomSheetVisible.value) {
            BottomSheetScaffold(
                scaffoldState = bottomSheetState,
                //sheetPeekHeight = 250.dp,
                sheetSwipeEnabled = true,
                sheetContent = {
                    BottomSheetContent(
                        onDeleteClick = {
                            deleteCategoryDialogOpened = true
                            toggleBottomSheetState()
                        },
                        onRenameClick = {
                            category = it
                            renameCategoryDialogOpened = true
                            toggleBottomSheetState()
                        },
                        categoryUiState = uiState
                    )
                },

                content = {
                    ManageCategoriesScreen(
                        onAddCategoryClicked = { cat ->
                            category = cat
                            newCategoryDialogOpened = true
                        },
                        navigateToCategoryScreenWithArgs = navigateToListNotesOfCategory,
                        navigateBack = navigateBack,
                        deleteCategory = { /*TODO*/ },
                        usersCategories = categories,
                        onLongClick = {
                            toggleBottomSheetState()
                            viewModel.getSelectedCategoryId(it)
                        }
                    )
                }
            )
        } else {
            ManageCategoriesScreen(
                onAddCategoryClicked = { cat ->
                    category = cat
                    newCategoryDialogOpened = true
                },
                navigateToCategoryScreenWithArgs = navigateToListNotesOfCategory,
                navigateBack = navigateBack,
                deleteCategory = { /*TODO*/ },
                usersCategories = categories,
                onLongClick = {
                    toggleBottomSheetState()
                    viewModel.getSelectedCategoryId(it)
                }
            )
        }
        AddNewCategoryDialog(
            onDialogOpen = renameCategoryDialogOpened,
            onDialogClosed = { renameCategoryDialogOpened = false },
            onCategoryAdded = { name, categoryColor ->
                category.categoryName = name
                Log.d("rename", viewModel.categoryName.value)
                viewModel.upsertCategory(
                    category = category,
                    name = name,
                    onSuccess = {
                        renameCategoryDialogOpened = false
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("rename", message)
                    },
                    categoryId = viewModel.categoryId,
                    color = categoryColor
                )
            },
            currentName = category.categoryName,
            currentColor = category.color,
            title = stringResource(id = R.string.rename),
            text = context.getString(R.string.type_the_new_name)
        )

        AddNewCategoryDialog(
            onDialogOpen = newCategoryDialogOpened,
            onDialogClosed = { newCategoryDialogOpened = false },
            onCategoryAdded = { name, categoryColor ->
                val newCategory = Category()
                viewModel.setName(name)
                Log.d("ADD CATEGORY", name + category.categoryName)
                viewModel.upsertCategory(
                    null,
                    newCategory,
                    onSuccess = {},
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("ADD CATEGORY", message)
                    },
                    name = name,
                    color = categoryColor
                )
            },
            title = stringResource(id = R.string.add_category),
            text = stringResource(id = R.string.category_name)
        )


        AlertDialog(
            title = context.getString(R.string.delete_this_category),
            message = context.getString(R.string.are_you_sure_you_want_to_delete_this_category_and_everything_in_it),
            dialogOpened = deleteCategoryDialogOpened,
            onDialogClosed = { deleteCategoryDialogOpened = false },
            onYesClicked = {
                viewModel.deleteCategory(
                    onSuccess = {
                        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                        navigateBack()
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            if (it == "No Internet Connection.")
                                "We need an Internet Connection for this operation."
                            else it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.listNotesOfCategoryRoute(
    navigateToAddEditScreenWithArgs: (String) -> Unit,
    navigateToAddEditScreenWithCategoryArg: (String) -> Unit
) {
    composable(
        route = Screen.ListNotesOfCategoryScreen.route,
        arguments = listOf(navArgument(name = MANAGE_CATEGORIES_SCREEN_ARG_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) { backStackEntry ->
        val viewModel: ListNotesOfCatViewModel = hiltViewModel()
        val uiState = viewModel.categoryUiState
        val notes by viewModel.notesOfCategory
        val context = LocalContext.current
        ListNotesOfCategoryScreen(
            navigateToAddEditScreenWithCategoryArg = navigateToAddEditScreenWithCategoryArg,
            noteEntries = notes,
            navigateToAddEditScreenWithArgs = navigateToAddEditScreenWithArgs,
            categoryUiState = uiState,
            categoryName = uiState.categoryName,
            onNameChanged = {
                viewModel.setName(it)
            },
            onSaveNameButtonClick = {
                viewModel.upsertCategory(
                    it,
                    onSuccess = { Log.i("ListScreenRename", "name changed successfully") },
                    onError = { error ->
                        Toast.makeText(context, "Renaming was unsuccessful", Toast.LENGTH_SHORT)
                            .show()
                        Log.i("ListScreenRename", error)
                    },
                    name = uiState.categoryName,
                    color = it.color
                )
            }
        )
    }


}