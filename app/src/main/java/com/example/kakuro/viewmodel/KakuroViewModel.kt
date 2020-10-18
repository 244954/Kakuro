package com.example.kakuro.viewmodel

import android.arch.lifecycle.ViewModel
import com.example.kakuro.gamelogic.KakuroGame

class KakuroViewModel : ViewModel() {
    val kakuroGame = KakuroGame()
}