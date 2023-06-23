package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import timber.log.Timber

class GameViewModel: ViewModel() {

    companion object {
        // These represent different important times in the game,
        // such as game length

        // To represent when the game is over
        private const val DONE = 0L

        // Number of milliseconds in a second
        private const val ONE_SECOND = 1000L

        // This is the total time of the game
        private const val COUNTDOWN_TIME = 61000L

        // This is the warning buzzer time
        private const val WARNING_BUZZER_TIME = 10000L
    }

    private val timer: CountDownTimer

    private val _currentTime = MutableLiveData<Long>()
    /**
     * currentTime in seconds
     */
    val currentTime : LiveData<Long>
        get() = _currentTime

    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    // The current word
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
        get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int>
        get() = _score

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish : LiveData<Boolean>
        get() = _eventGameFinish

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

//    private val _buzzer = MutableLiveData<BuzzType>()
//    val buzzer : LiveData<BuzzType>
//        get() = _buzzer

    private val _buzzer = MediatorLiveData<BuzzType>()

    /**
     * !! don't forget to call the [onBuzzerBuzzingComplete]
     * to finish the event
     */
    val buzzer : LiveData<BuzzType>
        get() = _buzzer

    init {
        Timber.i("GameViewModel created!!")
        resetList()
        nextWord()

        _score.value = 0
        _eventGameFinish.value = false

        timer = object: CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisecondsUntilFinished: Long) {
                _currentTime.value = (millisecondsUntilFinished/ ONE_SECOND)
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _buzzer.value = BuzzType.GAME_OVER
                _eventGameFinish.value = true
            }
        }
        timer.start()
        setupBuzzers()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Timber.i("GameViewModel destroyed!!")
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        // and reset the list cause we want a timer based game
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = _score.value?.dec()
        nextWord()
    }

    fun onCorrect() {
        _score.value = _score.value?.inc()
        nextWord()
        _buzzer.value = BuzzType.CORRECT
    }

    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    /**
     * setup when the buzzers would happen
     */
    private fun setupBuzzers() {
        _buzzer.apply {
            addSource(currentTime, Observer { timeInSeconds->
                if (timeInSeconds < (WARNING_BUZZER_TIME / ONE_SECOND)) {
                    _buzzer.value = BuzzType.COUNTDOWN_PANIC
//                    _buzzer.removeSource(currentTime)
                }
            })
        }
    }

    fun onBuzzerBuzzingComplete() {
        _buzzer.value = BuzzType.NO_BUZZ
    }
}

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

enum class BuzzType(val pattern: LongArray) {
    CORRECT(CORRECT_BUZZ_PATTERN),
    GAME_OVER(GAME_OVER_BUZZ_PATTERN),
    COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
    NO_BUZZ(NO_BUZZ_PATTERN)


}