package com.maps.naver.sjnavermaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
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
                        NaverMap.DEFAULT_CAMERA_POSITION.target, NaverMap.DEFAULT_CAMERA_POSITION.zoom, 30.0, 45.0)
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
            toast(getString(R.string.format_map_long_click, coord.latitude, coord.longitude))
            Marker().apply {
                position = LatLng(coord.latitude, coord.longitude)
                map = naverMap
            }
        }
    }
}