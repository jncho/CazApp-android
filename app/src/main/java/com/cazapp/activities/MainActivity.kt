package com.cazapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.cazapp.R
import com.cazapp.localdb.ListLocalSong
import com.cazapp.localdb.LocalSong
import com.cazapp.repository.SongRepository
import com.cazapp.repository.SongRepositoryFactory
import com.mongodb.stitch.android.core.Stitch
import kotlinx.android.synthetic.main.activity_main.*
import org.bson.types.ObjectId
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.cazapp.fragments.*
import com.cazapp.model.PublicList
import com.cazapp.model.PublicSong
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    lateinit var actionbar: ActionBar
    lateinit var toolbar: Toolbar
    lateinit var loginDialog: AlertDialog.Builder

    var actualFragment: Int = 0
    var repository: SongRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(my_toolbar)

        toolbar = my_toolbar
        actionbar = supportActionBar!!
        actionbar.title = "CazApp"

        loginDialog = createAlertLoginDialog()

        // Drawer layout
        setupDrawerToggle()
        setupNavigationView()

        if (repository == null) {
            repository = SongRepositoryFactory.createRepository()
        }

        if (intent != null) {
            onNewIntent(intent)
        }

        if (savedInstanceState == null) {
            setFragment(0)
        }


    }

    override fun onResume() {
        super.onResume()
        refreshLoginLogoutButtons()


    }

    private fun createAlertLoginDialog(): AlertDialog.Builder {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Login required")
        alertDialog.setMessage("You must login to use this function")
        alertDialog.setPositiveButton("Ok",null)
        return alertDialog
    }

    fun refreshLoginLogoutButtons() {

        val user = Stitch.getDefaultAppClient().auth.user

        if (user == null) {
            navigation_view.menu.findItem(R.id.action_login).isVisible = true
            navigation_view.menu.findItem(R.id.action_login).isEnabled = true
            navigation_view.menu.findItem(R.id.action_logout).isVisible = false
            navigation_view.menu.findItem(R.id.action_logout).isEnabled = false
        } else if (user.loggedInProviderName != "anon-user") {
            navigation_view.menu.findItem(R.id.action_login).isVisible = false
            navigation_view.menu.findItem(R.id.action_login).isEnabled = false
            navigation_view.menu.findItem(R.id.action_logout).isVisible = true
            navigation_view.menu.findItem(R.id.action_logout).isEnabled = true

            val header = navigation_view.getHeaderView(0)
            if (user.profile?.name != null)
                header.findViewById<TextView>(R.id.header_profile).text = user.profile?.name
            if (user.profile?.pictureUrl != null)
                DownloadImageTask(
                    this,
                    header.findViewById(R.id.profile_pic),
                    header.findViewById(R.id.loading_profile_pic)
                ).execute(user.profile?.pictureUrl)
        } else {
            navigation_view.menu.findItem(R.id.action_login).isVisible = true
            navigation_view.menu.findItem(R.id.action_login).isEnabled = true
            navigation_view.menu.findItem(R.id.action_logout).isVisible = false
            navigation_view.menu.findItem(R.id.action_logout).isEnabled = false
            val header = navigation_view.getHeaderView(0)

            header.findViewById<TextView>(R.id.header_profile).text = "Anonymous"
            header.findViewById<ImageView>(R.id.profile_pic).setImageResource(R.mipmap.anonymous_pic_profile)

        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_VIEW) {
            val url = intent.data!!.toString()
            val tokens = url.split('/')
            if (tokens.size ==5 && tokens[4]!=""){
                // Get list from repository
                val callback = object:SongRepository.ResultCallback<PublicList?>{
                    override fun onFailureExecute() {
                        frame_loading.visibility = View.GONE
                        Snackbar.make(drawer_layout,"Conection error.",Snackbar.LENGTH_LONG)
                    }

                    override fun onPreExecute() {
                        frame_loading.visibility = View.VISIBLE
                    }

                    override fun onSuccessExecute(result: PublicList?) {
                        if (result != null) {
                            frame_loading.visibility = View.GONE
                            val ft = supportFragmentManager?.beginTransaction()
                            val fragment = PublicListFragment.newInstance(result)
                            ft?.replace(R.id.fragment, fragment)?.addToBackStack("publicList")
                            ft?.commit()
                        }else{
                            Snackbar.make(drawer_layout,"List not exist.",Snackbar.LENGTH_LONG)
                        }
                    }
                }

                repository!!.getList(tokens[4],callback)


            }else{
                Snackbar.make(drawer_layout,"The list does not exist",Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupDrawerToggle() {
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout as DrawerLayout,
            my_toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {}
        drawerToggle.isDrawerIndicatorEnabled = true
        (drawer_layout as DrawerLayout).addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupNavigationView() {
        navigation_view.setNavigationItemSelectedListener { result ->
            when (result.itemId) {
                R.id.action_songs -> setFragment(0)
                R.id.action_private_lists -> setFragment(1)
                R.id.action_public_lists -> setFragment(2)
                R.id.action_favorites -> setFragment(3)
                R.id.action_logout, R.id.action_login -> {
                    loading.visibility = View.VISIBLE
                    frame_loading.visibility = View.VISIBLE

                    if (Stitch.getDefaultAppClient().auth.user?.loggedInProviderName != "anon-user") {
                        Stitch.getDefaultAppClient().auth.logout().addOnCompleteListener {
                            if (it.isSuccessful) {
                                loading.visibility = View.GONE
                                frame_loading.visibility = View.GONE
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                Log.e("Cazapp", "Error to logout user")
                                Snackbar.make(drawer_layout, "Error to login", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        loading.visibility = View.GONE
                        frame_loading.visibility = View.GONE
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }


                }

            }
            // Close the drawer
            (drawer_layout as DrawerLayout).closeDrawer(GravityCompat.START)
            true
        }
    }

    fun hideHeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setFragment(position: Int) {
        val fragmentManager: FragmentManager
        val fragmentTransaction: FragmentTransaction
        hideHeyboard()
        when (position) {
            0 -> {
                actualFragment = 0
                fragmentManager = supportFragmentManager
                fragmentTransaction = fragmentManager.beginTransaction()
                val songsFragment = SongsFragment()
                fragmentTransaction.replace(R.id.fragment, songsFragment).addToBackStack("songs")
                fragmentTransaction.commit()
            }
            1 -> {
                actualFragment = 1
                fragmentManager = supportFragmentManager
                fragmentTransaction = fragmentManager.beginTransaction()
                val listsFragment = LocalListsFragment()
                fragmentTransaction.replace(R.id.fragment, listsFragment).addToBackStack("lists")
                fragmentTransaction.commit()
            }
            2 -> {
                actualFragment = 2
                fragmentManager = supportFragmentManager
                fragmentTransaction = fragmentManager.beginTransaction()
                val publicListsFragment = PublicListsFragment()
                fragmentTransaction.replace(R.id.fragment, publicListsFragment).addToBackStack("publicLists")
                fragmentTransaction.commit()
            }
            3 -> {
                actualFragment = 3
                fragmentManager = supportFragmentManager
                fragmentTransaction = fragmentManager.beginTransaction()
                val favoritesFragment = FavoritesFragment()
                fragmentTransaction.replace(R.id.fragment, favoritesFragment).addToBackStack("favorites")
                fragmentTransaction.commit()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("actualFragment", actualFragment)
    }

    private inner class DownloadImageTask(val activity: Activity, var bmImage: ImageView, val loading: View) :
        AsyncTask<String, Void, Bitmap>() {

        override fun onPreExecute() {
            super.onPreExecute()
            activity.runOnUiThread {
                loading.visibility = View.VISIBLE
                bmImage.visibility = View.INVISIBLE
            }
        }

        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = urls[0]
            var bmp: Bitmap? = null
            try {
                val `in` = java.net.URL(urldisplay).openStream()
                bmp = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message)

                e.printStackTrace()
            }

            activity.runOnUiThread {
                loading.visibility = View.GONE
                bmImage.visibility = View.VISIBLE
            }



            return bmp
        }

        override fun onPostExecute(result: Bitmap) {
            bmImage.setImageBitmap(result)
        }
    }

}