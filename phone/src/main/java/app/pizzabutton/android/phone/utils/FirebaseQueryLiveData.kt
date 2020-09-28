package app.pizzabutton.android.phone.utils

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.*

private val TAG = FirebaseQueryLiveData::class.java.simpleName

class FirebaseQueryLiveData : LiveData<DataSnapshot> {
    private val query: Query
    private val listener: MyValueEventListener = MyValueEventListener()

    constructor(query: Query) {
        this.query = query
    }

    constructor(ref: DatabaseReference) {
        query = ref
    }

    override fun onActive() {
        Log.d(TAG, "onActive")
        query.addValueEventListener(listener)
    }

    override fun onInactive() {
        Log.d(TAG, "onInactive")
        query.removeEventListener(listener)
    }

    private inner class MyValueEventListener : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            value = dataSnapshot
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(
                TAG,
                "Can't listen to query $query", databaseError.toException()
            )
        }
    }
}