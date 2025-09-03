package com.jed.optima.audioclips

data class PlaybackFailedException(val uRI: String, val exceptionMsg: Int) : Exception()
