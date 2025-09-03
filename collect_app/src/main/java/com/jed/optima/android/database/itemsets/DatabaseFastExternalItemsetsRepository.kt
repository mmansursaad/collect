package com.jed.optima.android.database.itemsets

import com.jed.optima.android.itemsets.FastExternalItemsetsRepository

class DatabaseFastExternalItemsetsRepository : FastExternalItemsetsRepository {

    override fun deleteAllByCsvPath(path: String) {
        com.jed.optima.android.fastexternalitemset.ItemsetDbAdapter().open().use {
            it.delete(path)
        }
    }
}
