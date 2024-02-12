package com.klaudia.mynotes.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

open class Category : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId.invoke()
    var ownerId: String = ""
    var categoryName: String = ""
    var color: String = ""
    //var notesOfCategory: RealmList<ObjectId> = realmListOf()
}