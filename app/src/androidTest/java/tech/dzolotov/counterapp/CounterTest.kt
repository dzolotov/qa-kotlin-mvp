package tech.dzolotov.counterapp

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        println("Run custom test runner")
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

open class CounterActivityScreen : Screen<CounterActivityScreen>() {
    val counter = KTextView { withId(R.id.counter) }
    val increaseButton = KButton { withId(R.id.increase_button) }
    val description = KTextView { withId(R.id.description) }
}

@TestInstallIn(components = [ActivityComponent::class], replaces = [RepositoryModule::class])
@Module
abstract class TestDataSourceModule {
    @Binds
    @ActivityScoped
    abstract fun bindDescription(impl: TestDescriptionDataSource): IDescriptionRepository
}

@TestInstallIn(components = [SingletonComponent::class], replaces = [DispatchersModule::class])
@Module
object DispatchersTestModule {
    @IODispatcher
    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = StandardTestDispatcher()
}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CounterTest {
    @get:Rule
    val rule = ActivityScenarioRule(CounterActivity::class.java)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    val counterScreen = CounterActivityScreen()

    @Inject
    @IODispatcher
    lateinit var dispatcher: CoroutineDispatcher

    @Before
    fun init() {
//        IdlingRegistry.getInstance().register(idling)
        hiltRule.inject()
        rule.scenario.onActivity {
            (dispatcher as TestDispatcher).scheduler.runCurrent()
        }
    }

    @Test
    fun checkCounter() {
        counterScreen {
            description.hasText("Loading")
            (dispatcher as TestDispatcher).scheduler.run {
                advanceTimeBy(2000)
                runCurrent()
            }
            description.hasText("Data from test")
            counter.hasText("Click below for increment")
            increaseButton.click()
            counter.hasText("Counter: 1")
            increaseButton.click()
            counter.hasText("Counter: 2")
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.setOrientationLeft()
            counter.hasText("Counter: 2")
            increaseButton.click()
            counter.hasText("Counter: 3")
            description.hasText("Data from test")
        }
    }
}

class TestAutomator {

    lateinit var device: UiDevice
    lateinit var packageName: String

    @Before
    fun setup() {
        packageName = BuildConfig.APPLICATION_ID

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        //ждем запуска Launcher-процесса (для передачи интента)
        val launcherPage = device.launcherPackageName
        device.wait(Until.hasObject(By.pkg(launcherPage).depth(0)), 5000L)
        //создаем контекст (для доступа к сервисам) и запускаем наше приложение
        val context = ApplicationProvider.getApplicationContext<Context>()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)       //каждый запуск сбрасывает предыдущее состояние
        }
        context.startActivity(launchIntent)
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000L)
    }

    @Test
    fun testCounterE2E() {
        //надпись со счетчиком
        val counter = device.findObject(By.res(packageName, "counter"))
        assertEquals("Click below for increment", counter.text)
        //кнопка увеличения счетчика
        val button = device.findObject(By.res(packageName, "increase_button"))
        assertEquals("+", button.text)
        //текст с данными из внешней системы
        val description = device.findObject(By.res(packageName, "description"))
        //при запуске там индикатор загрузки
        assertEquals("Loading", description.text)
        //ждем 2 секунды (до загрузки данных)
        Thread.sleep(2000)
        //проверяем появление строки из внешней системы
        assertEquals("Text from external data source", description.text)
        //проверяем работу счетчика нажатий
        button.click()
        assertEquals("Counter: 1", counter.text)
        button.click()
        assertEquals("Counter: 2", counter.text)
        //проверяем сохранение состояния и корректность работы после поворота экрана
        device.setOrientationLeft()
        //ссылка на объекты в UiAutomator2 устаревают при пересоздании/изменении Activity, ищем заново
        val counter2 = device.findObject(By.res(packageName, "counter"))
        val button2 = device.findObject(By.res(packageName, "increase_button"))
        val description2 = device.findObject(By.res(packageName, "description"))

        assertEquals("Counter: 2", counter2.text)
        button2.click()
        assertEquals("Counter: 3", counter2.text)
        assertEquals("Text from external data source", description2.text)
    }
}

class TestDescriptionDataSource @Inject constructor() : IDescriptionRepository {
    override suspend fun getDescription(): String = "Data from test"
}