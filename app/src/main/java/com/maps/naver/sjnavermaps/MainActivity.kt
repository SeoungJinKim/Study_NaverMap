package com.maps.naver.sjnavermaps

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.db.NULL
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var state: State = State.INITIAL

    private var departureMarker: Marker? = null
    private var arrivalMarker: Marker? = null

    enum class State {
        INITIAL, START, END, SHOW_PATH, NONE_PATH
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 어플이 사용되는 동안 화면 끄지 않기
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 세로모드 고정
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

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
                    arrivalMarker = Marker().apply {
                        captionText = "도착"
                        position = LatLng(coord.latitude, coord.longitude)
                        map = naverMap
                    }
                    state = State.END
                }
            }
        }

        fab_remove_markers.setOnClickListener {
            if (state != State.INITIAL) {
                if (state == State.START) departureMarker?.map = null
                else {
                    departureMarker?.map = null
                    arrivalMarker?.map = null
                }
                toast("마커를 제거하였습니다.")
                state = State.INITIAL
                fab_control_direction.setImageResource(R.drawable.ic_directions_black_24dp)
            }
        }

        fab_control_direction.setOnClickListener {
            when (state) {
                State.INITIAL, State.START -> {
                    toast("출발, 도착 마커를 생성해주십시오")
                }
                State.END -> {
                    state = State.SHOW_PATH
                    fab_control_direction.setImageResource(R.drawable.ic_remove_black_24dp)
                    toast("경로를 표시합니다.")
                }
                State.SHOW_PATH -> {
                    state = State.NONE_PATH
                    fab_control_direction.setImageResource(R.drawable.ic_directions_black_24dp)
                    toast("경로를 제거합니다.")
                    PathOverlay()
                }
                State.NONE_PATH -> {
                    state = State.SHOW_PATH
                    fab_control_direction.setImageResource(R.drawable.ic_remove_black_24dp)
                    toast("경로를 표시합니다.")
                }
            }
        }
    }
}