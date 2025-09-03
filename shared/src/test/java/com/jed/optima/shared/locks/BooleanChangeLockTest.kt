package com.jed.optima.shared.locks

class BooleanChangeLockTest : ChangeLockTest() {
    override fun buildSubject(): ChangeLock {
        return BooleanChangeLock()
    }
}
