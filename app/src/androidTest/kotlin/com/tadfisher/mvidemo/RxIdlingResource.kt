package com.tadfisher.mvidemo

import android.support.test.espresso.Espresso
import android.support.test.espresso.idling.CountingIdlingResource
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import org.junit.rules.ExternalResource

class RxIdlingResource {

  private val counter = CountingIdlingResource("RxIdlingResource")

  private val scheduleHandler = Function { runnable: Runnable ->
    counter.increment()
    Runnable { try { runnable.run() } finally { counter.decrement() } }
  }

  fun register() {
    Espresso.registerIdlingResources(counter)
    RxJavaPlugins.setScheduleHandler(scheduleHandler)
  }

  fun unregister() {
    Espresso.unregisterIdlingResources(counter)
    RxJavaPlugins.setScheduleHandler(null)
  }

  class TestRule : ExternalResource() {

    val resource = RxIdlingResource()

    override fun before() {
      resource.register()
    }

    override fun after() {
      resource.unregister()
    }
  }
}
