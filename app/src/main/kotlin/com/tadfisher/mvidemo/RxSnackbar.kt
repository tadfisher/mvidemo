package com.tadfisher.mvidemo

import android.support.design.widget.Snackbar
import io.reactivex.functions.Consumer

inline fun Snackbar.shown(): Consumer<in Boolean> = Consumer {
  if (it) show() else dismiss()
}
