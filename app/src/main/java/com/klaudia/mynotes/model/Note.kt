package com.klaudia.mynotes.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.klaudia.mynotes.util.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.time.Instant

open class Note : RealmObject {
    @Transient
    var categoryName: String? = null

    @Transient
    var categoryColor: String? = "null"

    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var ownerId: String = ""
    var title: String = ""
    var content: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    var dateCreated: RealmInstant = Instant.now().toRealmInstant()
    var categoryId: ObjectId? = null
    var fontSize: Double = 16.0
}