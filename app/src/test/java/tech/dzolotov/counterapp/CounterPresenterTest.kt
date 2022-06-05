package tech.dzolotov.counterapp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CounterPresenterTest {

    lateinit var view: CounterContract.View

    lateinit var presenter: CounterContract.Presenter

    @Before
    fun setup() = runTest {
        view = mockk()
        every { view.updateCounter(any()) } returns Unit
        every { view.updateDescription(any()) } returns Unit
        presenter = CounterPresenter(view)
        presenter.onViewCreated(null)
    }

    @Test
    fun checkIncrement() {
        presenter.increment()
        verify(exactly = 1) { view.updateCounter(1) }
        presenter.increment()
        verify(exactly = 1) { view.updateCounter(2) }
    }
}