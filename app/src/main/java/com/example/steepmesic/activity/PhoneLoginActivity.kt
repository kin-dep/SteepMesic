package com.example.steepmesic.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.steepmesic.R
import com.example.steepmesic.net.MusicApi
import com.example.steepmesic.pojo.login.LoginResult
import com.example.steepmesic.util.NetUtil
import kotlinx.android.synthetic.main.activity_phone_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PhoneLoginActivity : BaseActivity() {

    private lateinit var musicApi: MusicApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        setSupportActionBar(phone_login_toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        musicApi = NetUtil.create(MusicApi::class.java)
        btn_login.setOnClickListener {
            login(phone.text.toString(), password.text.toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    //登录方法
    private fun login(phone: String, password: String) {
        musicApi.login(phone, password).enqueue(object : Callback<LoginResult> {
            override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                t.printStackTrace()
            }
            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                response.body()?.let {
                    if (it.code != 200) {
                        Toast.makeText(this@PhoneLoginActivity, "账号或密码错误", Toast.LENGTH_SHORT)
                                .show()
                    } else {
                        //缓存cookie
                        var cookies = response.headers().values("Set-Cookie")
                        var cookieArr = ArrayList<String>()
                        for (cookie in cookies) {
                            cookieArr.add(cookie.split(";").get(0))
                        }
                        val cookieStr = cookieArr.joinToString("; ")
                        getSharedPreferences("application", Context.MODE_PRIVATE)
                                .edit()
                                .putString("cookie", cookieStr)
                                .apply()
                        //登录成功的跳转
                        val intent = Intent(this@PhoneLoginActivity, MusicListActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        })
    }
}