package tech.dzolotov.counterapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.idling.CountingIdlingResource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class CounterActivity : AppCompatActivity(), CounterContract.View {
    @Inject
    lateinit var presenter: CounterContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.onViewCreated(savedInstanceState)
        findViewById<Button>(R.id.increase_button).setOnClickListener {
            presenter.increment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun updateCounter(counter: Int) {
        findViewById<TextView>(R.id.counter).text = "Counter: $counter"
    }

    override fun updateDescription(description: DescriptionResult) {
        val text = when (description) {
            is DescriptionResult.Error -> "Error is occured"
            is DescriptionResult.Loading -> "Loading"
            is DescriptionResult.Success -> description.text
        }
        findViewById<TextView>(R.id.description).text = text
    }
}

