package tech.dzolotov.counterapp

import android.os.Bundle
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import javax.inject.Inject

var idling = CountingIdlingResource("counter-presenter")

class CounterPresenter @Inject constructor(private val view: CounterContract.View) :
    CounterContract.Presenter {

    @Inject
    @IODispatcher
    lateinit var coroutineDispatcher: CoroutineDispatcher

    lateinit var model: CounterModel

    @Inject
    lateinit var descriptionRepository: IDescriptionRepository

    lateinit var viewRef: WeakReference<CounterContract.View>

    fun updateDescription(description: DescriptionResult) {
        model.description = description
        view.updateDescription(description)
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        viewRef = WeakReference(view)
        model = CounterModel(
            savedInstanceState?.getInt("counter"),
            description = savedInstanceState?.getString("description")
                ?.let { DescriptionResult.Success(it) })
        model.counter?.let {
            viewRef.get()?.updateCounter(it)    //начальное значение
        }
        model.description?.let {
            viewRef.get()?.updateDescription(it)
        }
        if (model.description == null) {        //загрузка при отсутствии сохраненного успешного результата
            //значение по умолчанию, если в тесте не используется Hilt (например, в Unit-тесте)
            if (!this::coroutineDispatcher.isInitialized) coroutineDispatcher = Dispatchers.IO
            CoroutineScope(coroutineDispatcher).launch {
                idling.increment()
                updateDescription(DescriptionResult.Loading())
                delay(2000)
                updateDescription(DescriptionResult.Success(descriptionRepository.getDescription()))
                idling.decrement()
            }
        }
    }

    override fun saveState(bundle: Bundle) {
        model.counter?.let { bundle.putInt("counter", it) }
        val description = model.description
        if (description is DescriptionResult.Success) {
            bundle.putString("description", description.text)
        }
    }

    override fun increment() {
        model.counter = model.counter?.inc() ?: 1
        viewRef.get()?.updateCounter(model.counter!!)
    }
}
