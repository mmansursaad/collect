package com.yedc.android.database.itemsets

import com.yedc.android.itemsets.FastExternalItemsetsRepository

class DatabaseFastExternalItemsetsRepository : FastExternalItemsetsRepository {

    override fun deleteAllByCsvPath(path: String) {
        _root_ide_package_.com.yedc.android.fastexternalitemset.ItemsetDbAdapter().open().use {
            it.delete(path)
        }
    }
}
