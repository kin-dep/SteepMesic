package com.example.steepmesic.pojo.login

data class LoginResult(
    val account: Account,
    val bindings: List<Binding>,
    val code: Int,
    val cookie: String,
    val loginType: Int,
    val profile: Profile,
    val token: String,
    //msg与message在错误时使用
    var msg: String?,
    var message: String?
)