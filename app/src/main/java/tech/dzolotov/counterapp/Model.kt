package tech.dzolotov.counterapp


sealed class DescriptionResult {
    data class Success(val text: String) : DescriptionResult()
    class Error() : DescriptionResult()
    class Loading() : DescriptionResult()
}

class CounterModel(var counter: Int?, var description: DescriptionResult? = null)