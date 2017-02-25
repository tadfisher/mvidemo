package rx.lang.kotlin

import io.reactivex.Observable

/**
 * Filters the items emitted by an Observable, only emitting those of the specified type.
 */
inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)
