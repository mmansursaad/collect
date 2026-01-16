package com.yedc.forms.instances

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class InstanceTest {

    @Test
    fun `canDeleteBeforeSend is true by default`() {
        val instance = _root_ide_package_.com.yedc.forms.instances.Instance.Builder().build()
        assertThat(instance.canDeleteBeforeSend(), equalTo(true))
    }
}
