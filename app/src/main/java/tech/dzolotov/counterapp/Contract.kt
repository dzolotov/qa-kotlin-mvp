package tech.dzolotov.counterapp

import android.os.Bundle

interface CounterContract {
    interface View {
        fun updateCounter(counter: Int)
        fun updateDescription(description: DescriptionResult)
    }

    interface Presenter {
        fun increment()
        fun onViewCreated(bundle: Bundle?)
        fun saveState(bundle: Bundle)
    }
}