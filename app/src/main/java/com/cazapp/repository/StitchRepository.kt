package com.cazapp.repository

import android.os.AsyncTask
import android.util.Log
import com.cazapp.model.PublicList
import com.cazapp.model.Song
import com.google.gson.Gson
import com.mongodb.stitch.android.core.Stitch
import com.google.gson.reflect.TypeToken
import com.mongodb.stitch.android.core.StitchAppClient
import com.mongodb.stitch.core.auth.StitchCredential
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential
import org.bson.BsonBoolean
import org.bson.Document
import org.bson.codecs.BooleanCodec
import org.bson.conversions.Bson


class StitchRepository : SongRepository {

    private val client: StitchAppClient = Stitch.getDefaultAppClient()

    override fun getSongs(query: String, searchContent: Boolean, callback: SongRepository.ResultCallback<List<Song>>) {
        GetSongsTask(client, query, searchContent, callback).execute()

    }

    override fun getSong(_id: String, callback: SongRepository.ResultCallback<Song>) {
        GetSongTask(client, _id, callback).execute()
    }

    override fun createList(listSong: PublicList, callback: SongRepository.ResultCallback<Boolean>) {
        CreateListTask(client, listSong, callback).execute()
    }

    override fun deleteList(_id: String, callback: SongRepository.ResultCallback<Boolean>) {
        DeleteListTask(client, _id, callback).execute()
    }

    override fun getLists(callback: SongRepository.ResultCallback<List<PublicList>>) {
        GetListsTask(client, callback).execute()
    }

    override fun updateList(publicList: PublicList, callback: SongRepository.ResultCallback<Boolean>) {
        UpdateListTask(client, publicList, callback).execute()
    }

    override fun isLoggedIn(): Boolean =
        client.auth.user != null && client.auth.user?.loggedInProviderName != "anon-user"

    override fun getList(_id: String, callback: SongRepository.ResultCallback<PublicList?>) {
        PublicListTask(client, _id, callback).execute()
    }

    override fun getSongsById(ids: List<String>, callback: SongRepository.ResultCallback<List<Song>>) {
        GetSongsById(client,ids,callback).execute()
    }

    private class GetSongsById( val client: StitchAppClient,val ids: List<String>, val callback: SongRepository.ResultCallback<List<Song>>) : AsyncTask<Unit,Unit,Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {
            client.callFunction("getSongs",listOf(ids), List::class.java)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        val gson = Gson()
                        val resultJson = gson.toJson(it.result, object : TypeToken<List<Document>>() {}.type)
                        val songL: List<Song> = gson.fromJson(resultJson, object : TypeToken<List<Song>>() {}.type)
                        callback.onSuccessExecute(songL)
                    } else {
                        callback.onFailureExecute()
                    }
                }
        }
    }

    private class PublicListTask(
        val client: StitchAppClient,
        val _id: String,
        val callback: SongRepository.ResultCallback<PublicList?>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {


            client.callFunction(
                "getList",
                listOf(_id),
                Document::class.java
            )
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        val jsonResult = Gson().toJson(it.result!!)
                        callback.onSuccessExecute(Gson().fromJson(jsonResult,PublicList::class.java))
                    } else {
                        callback.onFailureExecute()
                    }
                }

        }

    }

    private class UpdateListTask(
        val client: StitchAppClient,
        val publicList: PublicList,
        val callback: SongRepository.ResultCallback<Boolean>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {


            client.callFunction(
                "updatePublicList",
                listOf(publicList.toDocument(), publicList._id!!.toHexString()),
                Document::class.java
            )
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        callback.onSuccessExecute(true)
                    } else {
                        callback.onFailureExecute()
                    }
                }

        }

    }

    private class GetListsTask(
        val client: StitchAppClient,
        val callback: SongRepository.ResultCallback<List<PublicList>>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {
            var listL: List<PublicList>

            client.callFunction("getLists", listOf(""), List::class.java)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        val gson = Gson()
                        val resultJson = gson.toJson(it.result, object : TypeToken<List<Document>>() {}.type)
                        listL = gson.fromJson(resultJson, object : TypeToken<List<PublicList>>() {}.type)
                        callback.onSuccessExecute(listL)
                    } else {
                        callback.onFailureExecute()
                    }
                }

        }

    }

    private class DeleteListTask(
        val client: StitchAppClient,
        val _id: String,
        val callback: SongRepository.ResultCallback<Boolean>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {

            client.callFunction("deletePublicList", listOf(_id), Document::class.java)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        callback.onSuccessExecute(it.result?.getInteger("deletedCount") == 1)
                    } else {
                        callback.onFailureExecute()
                    }
                }

        }
    }

    private class CreateListTask(
        val client: StitchAppClient,
        val listSong: PublicList,
        val callback: SongRepository.ResultCallback<Boolean>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {

            client.callFunction("createPublicList", listOf(listSong.toDocument()), Document::class.java)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        callback.onSuccessExecute(it.result?.getObjectId("insertedId") == listSong._id)
                    } else {
                        callback.onFailureExecute()
                    }
                }


        }

    }

    private class GetSongsTask(
        val client: StitchAppClient,
        val query: String,
        val searchContent: Boolean,
        val callback: SongRepository.ResultCallback<List<Song>>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {
            var songL: List<Song>

            client.callFunction("searchSongs", listOf(query, searchContent), List::class.java)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        Log.d("Result", it.result.toString())
                        val gson = Gson()
                        val resultJson = gson.toJson(it.result, object : TypeToken<List<Document>>() {}.type)
                        songL = gson.fromJson(resultJson, object : TypeToken<List<Song>>() {}.type)
                        callback.onSuccessExecute(songL)
                    } else {
                        callback.onFailureExecute()
                    }
                }

        }

    }

    private class GetSongTask(
        val client: StitchAppClient,
        val _id: String,
        val callback: SongRepository.ResultCallback<Song>
    ) : AsyncTask<Unit, Unit, Unit>() {
        override fun onPreExecute() {
            callback.onPreExecute()
        }

        override fun doInBackground(vararg params: Unit?) {
            var song: Song

            client.callFunction("getSong", listOf(_id), Document::class.java)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        Log.d("Result", it.result.toString())
                        val gson = Gson()
                        val resultJson = gson.toJson(it.result, Document::class.java)
                        song = gson.fromJson(resultJson, Song::class.java)
                        callback.onSuccessExecute(song)

                    } else {
                        callback.onFailureExecute()
                    }
                }


        }

    }
}
