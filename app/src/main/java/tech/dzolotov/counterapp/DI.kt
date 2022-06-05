package tech.dzolotov.counterapp

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class IODispatcher

@InstallIn(ActivityComponent::class)
@Module
abstract class CounterModule {
    @Binds
    @ActivityScoped
    abstract fun bindActivity(activity: CounterActivity): CounterContract.View

    @Binds
    @ActivityScoped
    abstract fun bindPresenter(impl: CounterPresenter): CounterContract.Presenter
}

@InstallIn(ActivityComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @ActivityScoped
    abstract fun bindDescription(impl: DescriptionRepository): IDescriptionRepository
}

@InstallIn(ActivityComponent::class)
@Module
object CounterActivityModule {
    @Provides
    @ActivityScoped
    fun provideActivity(activity: Activity): CounterActivity {
        return activity as CounterActivity
    }
}

@InstallIn(SingletonComponent::class)
@Module
object DispatchersModule {
    @Provides
    @IODispatcher
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
