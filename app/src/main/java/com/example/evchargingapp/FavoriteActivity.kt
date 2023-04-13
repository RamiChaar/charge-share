package com.example.evchargingapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.errorprone.annotations.Var
import kotlin.properties.Delegates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class FavoriteActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewActionBar = layoutInflater.inflate(R.layout.station_info_bar, null);
        val toolBarLabel = viewActionBar.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.toolBarTitle)
        toolBarLabel.text = "Your Favorite Stations"
        supportActionBar?.customView = viewActionBar;
        supportActionBar?.setDisplayShowCustomEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setHomeButtonEnabled(true);

        setContentView(R.layout.activity_favorite)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if(currentUser != null) {
            val collectionRef = db.collection("FavoriteStations")
            val query = collectionRef.whereEqualTo("UID", currentUser.uid)
            query.get().addOnSuccessListener { querySnapshot ->
                // Query snapshot contains all documents in the "users" collection
                val favoritesList = findViewById<ConstraintLayout>(R.id.favoritesList)
                var prevChildLayout: View? = null
                var i = 0;
                for (document in querySnapshot) {
                    if(document.data["UID"] == currentUser.uid) {
                        val inflater = LayoutInflater.from(this)
                        val childLayout = inflater.inflate(R.layout.info_window, null)
                        val title = childLayout.findViewById<TextView>(R.id.title)
                        val snippet = childLayout.findViewById<TextView>(R.id.snippet)
                        childLayout.id = document.data["id"].toString().toInt()
                        title.text = document.data["name"].toString()
                        snippet.text =
                            "Status: " + document.data["status"] +
                            "\nAccess: " + document.data["access"] +
                            "\nLevel: " + document.data["level"]

                        favoritesList.addView(childLayout)

                        childLayout.setOnClickListener { view ->
                            val intent = Intent(this, StationInfoActivity::class.java)
                            intent.putExtra("id", view.id.toString())
                            Log.d("debug", "launching info for " +  view.id.toString())
                            stationInfoLauncher.launch(intent)
                        }

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(favoritesList) // Clone the parent ConstraintLayout's constraints
                        constraintSet.connect(childLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                        constraintSet.connect(childLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                        constraintSet.connect(childLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                        constraintSet.constrainWidth(childLayout.id, ConstraintSet.MATCH_CONSTRAINT)

                        if (i == 0) {
                            // For first child layout, set constraints to parent start and add margin
                            constraintSet.connect(childLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8.dpToPx())
                        } else if (prevChildLayout != null) {
                            // For subsequent child layouts, set constraints to previous child layout end and add margin
                            constraintSet.connect(childLayout.id, ConstraintSet.TOP, prevChildLayout.id, ConstraintSet.BOTTOM, 8.dpToPx())
                        }
                        constraintSet.applyTo(favoritesList)
                        i++
                        prevChildLayout = childLayout
                    }
                }
            }.addOnFailureListener { e ->
                // Handle errors
                Log.e("debug", "Error getting documents", e)
            }
        }
    }

    private fun refresh() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if(currentUser != null) {
            val collectionRef = db.collection("FavoriteStations")
            val query = collectionRef.whereEqualTo("UID", currentUser.uid)
            query.get().addOnSuccessListener { querySnapshot ->
                // Query snapshot contains all documents in the "users" collection
                val favoritesList = findViewById<ConstraintLayout>(R.id.favoritesList)
                favoritesList.removeAllViews()
                var prevChildLayout: View? = null
                var i = 0;
                for (document in querySnapshot) {
                    if(document.data["UID"] == currentUser.uid) {
                        val inflater = LayoutInflater.from(this)
                        val childLayout = inflater.inflate(R.layout.info_window, null)
                        val title = childLayout.findViewById<TextView>(R.id.title)
                        val snippet = childLayout.findViewById<TextView>(R.id.snippet)
                        childLayout.id = document.data["id"].toString().toInt()
                        title.text = document.data["name"].toString()
                        snippet.text =
                            "Status: " + document.data["status"] +
                            "\nAccess: " + document.data["access"] +
                            "\nLevel: " + document.data["level"]

                        favoritesList.addView(childLayout)

                        childLayout.setOnClickListener { view ->
                            val intent = Intent(this, StationInfoActivity::class.java)
                            intent.putExtra("id", view.id.toString())
                            Log.d("debug", "launching info for " +  view.id.toString())
                            stationInfoLauncher.launch(intent)
                        }

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(favoritesList) // Clone the parent ConstraintLayout's constraints
                        constraintSet.connect(childLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                        constraintSet.connect(childLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                        constraintSet.connect(childLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                        constraintSet.constrainWidth(childLayout.id, ConstraintSet.MATCH_CONSTRAINT)

                        if (i == 0) {
                            // For first child layout, set constraints to parent start and add margin
                            constraintSet.connect(childLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8.dpToPx())
                        } else if (prevChildLayout != null) {
                            // For subsequent child layouts, set constraints to previous child layout end and add margin
                            constraintSet.connect(childLayout.id, ConstraintSet.TOP, prevChildLayout.id, ConstraintSet.BOTTOM, 8.dpToPx())
                        }
                        constraintSet.applyTo(favoritesList)
                        i++
                        prevChildLayout = childLayout
                    }
                }
            }.addOnFailureListener { e ->
                // Handle errors
                Log.e("debug", "Error getting documents", e)
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private var stationInfoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.resultCode) {

                Activity.RESULT_OK -> {

                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("debug", "FavoriteActivity: " + "onResume")
        refresh()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val returnIntent = Intent()
        setResult(1, returnIntent)
        finish()
        return super.onOptionsItemSelected(menuItem)
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
