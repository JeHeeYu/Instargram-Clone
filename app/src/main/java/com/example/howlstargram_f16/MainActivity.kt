package com.example.howlstargram_f16

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.howlstargram_f16.navigation.AlarmFragment
import com.example.howlstargram_f16.navigation.DetailViewFragment
import com.example.howlstargram_f16.navigation.GridFragment
import com.example.howlstargram_f16.navigation.UserFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//      bottom_navigation.setOnNavigationItemReselectedListener(this)
        bottom_navigation.setOnItemSelectedListener { it ->
            when (it.itemId) {
                // Navigation Bar에서 home 버튼 클릭 시 나타나는 화면
                R.id.action_home -> {
                    var detailViewFragment = DetailViewFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                    true
                }

                // Navigation Bar에서 search 버튼 클릭 시 나타나는 화면
                R.id.action_search -> {
                    var gridFragment = GridFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, gridFragment).commit()
                    true
                }

                // Navigation Bar에서 photo 버튼 클릭 시 나타나는 화면
                R.id.action_add_photo -> {

                    true
                }

                // Navigation Bar에서 alarm 버튼 클릭 시 나타나는 화면
                R.id.action_favorite_alarm -> {
                    val alarmFragment = AlarmFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment).commit()
                    true
                }

                // Navigation Bar에서 user 버튼 클릭 시 나타나는 화면
                R.id.action_account -> {
                    val userFragment = UserFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
                    true
                }
                else -> false
            }
        }
    }
}