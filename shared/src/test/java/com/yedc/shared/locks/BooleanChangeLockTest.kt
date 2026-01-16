package com.yedc.shared.locks

class BooleanChangeLockTest : ChangeLockTest() {
    override fun buildSubject(): ChangeLock {
        return BooleanChangeLock()
    }
}
