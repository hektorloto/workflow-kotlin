package com.squareup.workflow1.rx2

import com.squareup.workflow1.Worker
import com.squareup.workflow1.asWorker
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitSingleOrNull
import org.reactivestreams.Publisher

/**
 * Creates a [Worker] from this [Observable].
 *
 * The [Observable] will be subscribed to when the [Worker] is started, and disposed when it is
 * cancelled.
 *
 * Note that a [Worker] actually operates on a [kotlinx.coroutines.flow]. This means that your
 * [Observable] will first be adapted to a flow. To do this a communication structure is used
 * that will mean that the flow will process the items produced by the [Observable] asynchronously.
 *
 * Note that this [Observable] is transformed to a backpressure aware [Flowable] using the
 * [BUFFER] strategy before being transformed to a flow, which means that buffering will happen
 * in the [Flowable] before being passed over to the flow if it is experiencing backpressure.
 *
 * RxJava doesn't allow nulls, but it can't express that in its types. The receiver type parameter
 * is nullable so that the resulting [Worker] is non-nullable instead of having
 * platform nullability.
 */
public inline fun <reified T : Any> Observable<out T?>.asWorker(): Worker<T> =
  this.toFlowable(BUFFER).asWorker()

/**
 * Creates a [Worker] from this [Publisher] ([Flowable] is a [Publisher]).
 *
 * The [Publisher] will be subscribed to when the [Worker] is started, and cancelled when it is
 * cancelled.
 *
 * Note that a [Worker] actually operates on a [kotlinx.coroutines.flow]. This means that your
 * [Publisher] will first be mapped to a flow. To do this a communication structure is used
 * that will mean that the flow will process the items produced by the [Publisher] asynchronously.
 *
 * RxJava doesn't allow nulls, but it can't express that in its types. The receiver type parameter
 * is nullable so that the resulting [Worker] is non-nullable instead of having
 * platform nullability.
 */
public inline fun <reified T : Any> Publisher<out T?>.asWorker(): Worker<T> =
// This cast works because RxJava types don't actually allow nulls, it's just that they can't
  // express that in their types because Java.
  @Suppress("UNCHECKED_CAST")
  (this as Publisher<T>).asFlow().asWorker()

/**
 * Creates a [Worker] from this [Maybe].
 *
 * The [Maybe] will be subscribed to when the [Worker] is started, and disposed when it is
 * cancelled.
 *
 * RxJava doesn't allow nulls, but it can't express that in its types. The receiver type parameter
 * is nullable so that the resulting [Worker] is non-nullable instead of having
 * platform nullability.
 */
public inline fun <reified T : Any> Maybe<out T?>.asWorker(): Worker<T> =
  Worker.fromNullable { awaitSingleOrNull() }

/**
 * Creates a [Worker] from this [Single].
 *
 * The [Single] will be subscribed to when the [Worker] is started, and disposed when it is
 * cancelled.
 *
 * RxJava doesn't allow nulls, but it can't express that in its types. The receiver type parameter
 * is nullable so that the resulting [Worker] is non-nullable instead of having
 * platform nullability.
 */
public inline fun <reified T : Any> Single<out T?>.asWorker(): Worker<T> =
// This !! works because RxJava types don't actually allow nulls, it's just that they can't
  // express that in their types because Java.
  Worker.from { await()!! }
