package com.yedc.shared.locks

class ThreadSafeBooleanChangeLockTest : ChangeLockTest() {
    override fun buildSubject(): ChangeLock {
        return ThreadSafeBooleanChangeLock()
    }
}
