package com.tadfisher.mvidemo

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers.io
import java.util.concurrent.TimeUnit

interface LoginService {
  fun login(username: String, password: String): Completable
}

class FakeLoginService : LoginService {

  /** A fake login response with a 2-second delay. */
  override fun login(username: String, password: String): Completable {
    return Completable
        .defer {
          if (username == "hello" && password == "world") {
            Completable.complete()
          } else {
            Completable.error(IllegalStateException("Incorrect username and password"))
          }
        }
        .delay(2, TimeUnit.SECONDS, io(), true)
  }
}
