package com.cazapp.repository

import android.content.Context
import com.cazapp.R
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

class RealmRepository(context: Context) {

    val app: App

    init {
        Realm.init(context)
        app = App(AppConfiguration.Builder(context.getString(R.string.realm_client_app_id)).build())
    }

}