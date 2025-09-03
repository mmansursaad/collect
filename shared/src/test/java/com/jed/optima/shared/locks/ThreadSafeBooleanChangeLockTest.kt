package com.jed.optima.shared.locks

class ThreadSafeBooleanChangeLockTest : ChangeLockTest() {
    override fun buildSubject(): ChangeLock {
        return ThreadSafeBooleanChangeLock()
    }
}
