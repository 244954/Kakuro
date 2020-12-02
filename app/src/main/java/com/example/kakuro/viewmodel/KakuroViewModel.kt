package com.example.kakuro.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.gamelogic.KakuroGame

class KakuroViewModel : ViewModel() {
    lateinit var kakuroGame : KakuroGame

    fun startViewModel(size: Int, board: Array<Array<Int>>) {
        // seems suspect, but works just fine. Doing it in init would require some extra work
        if (!this::kakuroGame.isInitialized) {
            kakuroGame = KakuroGame(size, board)
        }
    }

    fun startViewModelFromSavedState(size: Int, boardSaved: Array<Array<KakuroCell?>>) {
        if (!this::kakuroGame.isInitialized) {
            kakuroGame = KakuroGame(size, boardSaved)
        }
    }
}