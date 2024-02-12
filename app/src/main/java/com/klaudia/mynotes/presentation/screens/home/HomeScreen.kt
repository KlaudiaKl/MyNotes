package com.klaudia.mynotes.presentation.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.klaudia.mynotes.R
import com.klaudia.mynotes.data.Categories
import com.klaudia.mynotes.data.Notes
import com.klaudia.mynotes.model.Category
import com.klaudia.mynotes.model.Note
import com.klaudia.mynotes.model.RequestState
import com.klaudia.mynotes.presentation.components.CategoryHolder
import com.klaudia.mynotes.presentation.components.EmptyPage
import com.klaudia.mynotes.presentation.components.NoteHolder
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    navigateToAddEditScreen: () -> Unit,
    navigateToAddEditScreenWithArgs: (String) -> Unit,
    onMenuClicked: () -> Unit,
    onSignedOutClicked: () -> Unit,
    onAddCategoryClicked: (Category) -> Unit,
    onManageCategoriesClicked: () -> Unit,
    noteEntries: Notes,
    usersCategories: Categories,
    onCategoryClick: (String) -> Unit,
    onSortButtonClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val padding by remember { mutableStateOf(PaddingValues()) }

    NavigationDrawer(
        drawerState = drawerState,
        onSignedOutClicked = onSignedOutClicked,
        onAddCategoryClicked = {
            onAddCategoryClicked(Category().apply {
                this.categoryName = ""
            })
        },
        onManageCategoriesClicked = onManageCategoriesClicked
    )
    {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeScreenTopBar(
                    scrollBehavior = scrollBehavior,
                    onMenuClicked = onMenuClicked
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(
                        end = padding.calculateEndPadding(LayoutDirection.Ltr)
                    ),
                    onClick = navigateToAddEditScreen
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Note Icon"
                    )
                }
            }
        ) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                when (noteEntries) {
                    is RequestState.Success -> {
                        if (noteEntries.data != null) {
                            HomeScreenContent(
                                notes = noteEntries.data,
                                onClick = navigateToAddEditScreenWithArgs,
                                categories = usersCategories,
                                onCategoryClick = onCategoryClick,
                                onSortButtonClick = onSortButtonClick
                            )
                            Log.d("ENTRIES", noteEntries.data.toString())
                        } else {
                            Log.d("EMPTY LIST", "noteEntries == null")
                        }
                    }

                    is RequestState.Error -> {
                        Log.d("ERROR home screen", "${noteEntries.error.message}")
                    }

                    RequestState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignedOutClicked: () -> Unit,
    onAddCategoryClicked: () -> Unit,
    onManageCategoriesClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenWidth =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp * 0.8f }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                content = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App logo"
                        )

                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .requiredWidth(250.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.google_logo),
                                        contentDescription = "Google Logo",
                                        // tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.sign_out),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            selected = false,
                            onClick = onSignedOutClicked
                        )
                        Divider()
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .requiredWidth(250.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.add_category),
                                        // tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Add a category",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            selected = false,
                            onClick = onAddCategoryClicked
                        )
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .requiredWidth(250.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Manage your categories",
                                        // tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.manage_categories),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            selected = false,
                            onClick = onManageCategoriesClicked
                        )
                    }
                },
                modifier = Modifier.width(screenWidth)
            )


        },
        content = content
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    notes: Map<LocalDate, List<Note>>,
    categories: Categories,
    onClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSortButtonClick: () -> Unit
) {

    Log.d("ComposeDebug", "HomeScreenContent recomposing with ${notes.size} notes.")
    //CATEGORIES
    when (categories) {
        is RequestState.Success -> {
            val categoriesList = categories.data ?: emptyList()
            Log.d("Categories:", categoriesList.toString())
            if (categoriesList.isNotEmpty()) {
                LazyRow(modifier = Modifier.padding(12.dp)) {
                    categoriesList.forEach { cat ->
                        item(key = cat._id.toString()) {
                            CategoryHolder(
                                category = cat,
                                onClick = onCategoryClick,
                                onLongClick = {}, null
                            )
                        }
                    }
                }
            }
        }
        // Handle other states (Loading, Error)
        else -> {}
    }
    Log.d("Before sort button", notes.entries.toString())
    IconButton(modifier = Modifier
        .padding(horizontal = 14.dp)
        .requiredWidth(150.dp)
        .background(MaterialTheme.colorScheme.surface),
        onClick = {
            onSortButtonClick()
        }

    ) {
        Row(
            modifier = Modifier
            //.padding(horizontal = 12.dp)
        )
        {
            Text(text = stringResource(R.string.sort_by_date))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Sort by date"
            )
        }
    }
    //NOTES
    if (notes.isNotEmpty()) {
        LazyColumn(
            // state = listState,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
            //.navigationBarsPadding()
        ) {
            notes.forEach { (localDate, entries) ->

                items(
                    items = entries,
                    //key = { it._id.toString() }
                ) {
                    NoteHolder(
                        note = it,
                        onClick = onClick,
                        categoryName = it.categoryName ?: "No category",
                        color = it.categoryColor?: MaterialTheme.colorScheme.background.toString()
                    )
                }
            }
        }
    } else {
        EmptyPage()
    }
}