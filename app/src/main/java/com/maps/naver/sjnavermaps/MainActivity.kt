package com.maps.naver.sjnavermaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.db.NULL
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var state : State = State.INITIAL

    private var departureMarker : Marker?= null
    private var arrivalMarker : Marker?= null

    enum class State{
        INITIAL, START, END, SHOW_PATH, NONE_PATH
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: run {
                val options = NaverMapOptions().camera(
                    CameraPosition(
                        NaverMap.DEFAULT_CAMERA_POSITION.target,
                        NaverMap.DEFAULT_CAMERA_POSITION.zoom,
                        30.0,
                        45.0
                    )
                )
                MapFragment.newInstance(options).also {
                    supportFragmentManager.beginTransaction().add(R.id.map, it).commit()
                }
            }
        mapFragment.getMapAsync(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onMapReady(naverMap: NaverMap) {

        naverMap.setOnMapClickListener { _, coord ->
            toast(getString(R.string.format_map_click, coord.latitude, coord.longitude))
        }

        naverMap.setOnMapLongClickListener { _, coord ->
            toast(
                getString(
                    R.string.format_map_long_click,
                    coord.latitude,
                    coord.longitude
                )
            )
            when (state) {
                State.INITIAL -> {
                    departureMarker = Marker().apply {
                        position = LatLng(coord.latitude, coord.longitude)
                        captionText = "출발"
                        map = naverMap
                    }
                    state = State.START
                }
                State.START -> {
                    arrivalMarker= Marker().apply {
                        captionText = "도착"
                        position = LatLng(coord.latitude, coord.longitude)
                        map = naverMap
                    }
                    state = State.END
                }
            }
        }

        fab_remove_markers.setOnClickListener {
            if(state != State.INITIAL)
            {
                if(state == State.START) departureMarker?.map = null
                else
                {
                    departureMarker?.map = null
                    arrivalMarker?.map = null
                }
                state = State.INITIAL
            }
        }
    }
}