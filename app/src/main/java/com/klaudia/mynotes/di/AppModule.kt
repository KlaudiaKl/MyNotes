package com.klaudia.mynotes.di

import android.app.Application
import android.content.Context
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.klaudia.mynotes.data.AddEditRepository
import com.klaudia.mynotes.data.AddEditRepositoryImpl
import com.klaudia.mynotes.data.CategoryService
import com.klaudia.mynotes.data.GoogleSignInRepository
import com.klaudia.mynotes.data.GoogleSignInRepositoryImpl
import com.klaudia.mynotes.data.HomeRepository
import com.klaudia.mynotes.data.HomeRepositoryImpl
import com.klaudia.mynotes.data.ListNotesOfCategoryRepository
import com.klaudia.mynotes.data.ListNotesOfCategoryRepositoryImpl
import com.klaudia.mynotes.data.ManageCategoriesRepository
import com.klaudia.mynotes.data.ManageCategoriesRepositoryImpl
import com.klaudia.mynotes.data.MongoDbRepository
import com.klaudia.mynotes.data.MongoDbRepositoryImpl
import com.klaudia.mynotes.data.db.entity.ImageToDeleteDao
import com.klaudia.mynotes.data.db.entity.ImageToUploadDao
import com.klaudia.mynotes.util.Constants.SIGN_IN_REQUEST
import com.klaudia.mynotes.util.Constants.SIGN_UP_REQUEST
import com.klaudia.mynotes.util.Constants.WEB_CLIENT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    //@Singleton
    @Provides
    fun provideMongoDbRepository(impl: MongoDbRepositoryImpl): MongoDbRepository = impl

    @ViewModelScoped
    @Provides
    fun provideCategoryService(mongoDbRepository: MongoDbRepository): CategoryService =
        CategoryService(mongoDbRepository)

    @ViewModelScoped
    @Provides
    fun provideHomeRepository(
        mongoDbRepository: MongoDbRepository,
        categoryService: CategoryService
    ): HomeRepository = HomeRepositoryImpl(mongoDbRepository, categoryService)

    @ViewModelScoped
    @Provides
    fun provideAddEditRepository(
        mongoDbRepository: MongoDbRepository,
        imageToDelete: ImageToDeleteDao,
        imageToUpload: ImageToUploadDao
    ): AddEditRepository =
        AddEditRepositoryImpl(mongoDbRepository, imageToDelete, imageToUpload)

    @ViewModelScoped
    @Provides
    fun provideManageCategoriesRepository(
        mongoDbRepository: MongoDbRepository,
        categoryService: CategoryService
    ): ManageCategoriesRepository =
        ManageCategoriesRepositoryImpl(mongoDbRepository, categoryService)

    @ViewModelScoped
    @Provides
    fun provideListNotesOfCategoryRepository(
        mongoDbRepository: MongoDbRepository,
        categoryService: CategoryService
    ): ListNotesOfCategoryRepository =
        ListNotesOfCategoryRepositoryImpl(mongoDbRepository, categoryService)

    @Provides
    fun providesFirebaseAuth() = Firebase.auth

    @Provides
    fun provideFirebaseFirestore() = Firebase.firestore


    @Provides
    fun provideOneTapClient(
        @ApplicationContext
        context: Context
    ) = Identity.getSignInClient(context)

    @Provides
    @Named(SIGN_UP_REQUEST)
    fun provideSignUpRequest(
        app: Application
    ) = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(WEB_CLIENT)
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .build()

    @Provides
    fun provideAuthRepository(
        auth: FirebaseAuth,
        oneTapClient: SignInClient,
        @Named(SIGN_IN_REQUEST)
        signInRequest: BeginSignInRequest,
        @Named(SIGN_UP_REQUEST)
        signUpRequest: BeginSignInRequest,
        db: FirebaseFirestore
    ): GoogleSignInRepository = GoogleSignInRepositoryImpl(
        firebaseAuth = auth,
        oneTapClient = oneTapClient,
        signInRequest = signInRequest,
        signUpRequest = signUpRequest,
        db = db
    )

    @Provides
    fun provideGoogleSignInOptions(
        app: Application
    ) = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(WEB_CLIENT)
        .requestEmail()
        .build()

    @Provides
    @Named(SIGN_IN_REQUEST)
    fun provideSignInRequest(
        app: Application
    ) = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(WEB_CLIENT)
                .setFilterByAuthorizedAccounts(true)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()



}