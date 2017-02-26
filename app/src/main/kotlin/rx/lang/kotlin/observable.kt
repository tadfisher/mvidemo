package rx.lang.kotlin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction

// From https://github.com/ReactiveX/RxKotlin/blob/2.x/src/main/kotlin/rx/lang/kotlin/observable.kt

private fun <T : Any> Iterator<T>.toIterable() = object : Iterable<T> {
    override fun iterator(): Iterator<T> = this@toIterable
}

fun BooleanArray.toObservable(): Observable<Boolean> = Observable.fromArray(*this.toTypedArray())
fun ByteArray.toObservable(): Observable<Byte> = Observable.fromArray(*this.toTypedArray())
fun ShortArray.toObservable(): Observable<Short> = Observable.fromArray(*this.toTypedArray())
fun IntArray.toObservable(): Observable<Int> = Observable.fromArray(*this.toTypedArray())
fun LongArray.toObservable(): Observable<Long> = Observable.fromArray(*this.toTypedArray())
fun FloatArray.toObservable(): Observable<Float> = Observable.fromArray(*this.toTypedArray())
fun DoubleArray.toObservable(): Observable<Double> = Observable.fromArray(*this.toTypedArray())
fun <T : Any> Array<T>.toObservable(): Observable<T> = Observable.fromArray(*this)

fun IntProgression.toObservable(): Observable<Int> =
        if (step == 1 && last.toLong() - first < Integer.MAX_VALUE) Observable.range(first, Math.max(0, last - first + 1))
        else Observable.fromIterable(this)

fun <T : Any> Iterator<T>.toObservable(): Observable<T> = toIterable().toObservable()
fun <T : Any> Iterable<T>.toObservable(): Observable<T> = Observable.fromIterable(this)
fun <T : Any> Sequence<T>.toObservable(): Observable<T> = Observable.fromIterable(iterator().toIterable())

fun <T : Any> Iterable<Observable<out T>>.merge(): Observable<T> = Observable.merge(this.toObservable())

/**
 * Observable.combineLatest(List<? extends Observable<? extends T>> sources, FuncN<? extends R> combineFunction)
 */
@Suppress("UNCHECKED_CAST")
inline fun <T, R> List<Observable<T>>.combineLatest(crossinline combineFunction: (args: List<T>) -> R): Observable<R>
        = Observable.combineLatest(this) { combineFunction(it.asList().map { it as T }) }

inline fun <T, U, R> Observable<T>.withLatestFrom(other: ObservableSource<out U>,
                                                  crossinline combineFunction: (T, U) -> R)
    : Observable<R> = withLatestFrom(other, BiFunction { t, u -> combineFunction(t, u)})

/**
 * Filters the items emitted by an Observable, only emitting those of the specified type.
 */
inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)
