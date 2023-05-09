/*
 *	Copyright 2023 cufy.org
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */
package org.cufy.monop

import org.cufy.bson.BsonDocument
import org.cufy.bson.Id

/*
These extensions are expected to be used as follows:

val foreign by "foreignId" be { Id } from map foreign MyCollection decode {
    it decode MyCollectionCodec
} createOperation Monop
*/

/**
 * Compose a new [Lazy] instance that uses [findOneById]
 * of the given [collection] with the value of [this] as
 * the argument.
 */
@OperationKeywordMarker
infix fun Lazy<Id<*>>.foreign(collection: MonopCollection): Lazy<Op<BsonDocument?>> {
    return lazy { collection.findOneById(value) }
}

/**
 * Compose a new [Lazy] instance that uses [findOneById]
 * of the given [collection] with the value of [this] as
 * the argument.
 */
@OperationKeywordMarker
infix fun <T> Lazy<Id<*>>.foreign(collection: MonopCollectionOf<T>): Lazy<Op<T?>> {
    return lazy { collection.findOneById(value).mapCatching { it?.let(collection.projection) } }
}

/**
 * Compose a new [Lazy] instance that uses [mapCatching]
 * on the [Op] value of [then] with the given [block] as
 * the argument.
 *
 * @param block the decoding block
 */
@OperationKeywordMarker
infix fun <T> Lazy<Op<BsonDocument?>>.decode(block: (BsonDocument) -> T): Lazy<Op<T?>> {
    return lazy { value.mapCatching { it?.let(block) } }
}

/**
 * Compose a new [Lazy] instance that creates a
 * new operation from the [Op] value of [this] and
 * returns it.
 *
 * If the given [monop] is not null, enqueue the
 * created operation to it.
 */
infix fun <T> Lazy<Op<T>>.createOperation(monop: Monop?): Lazy<Operation<T>> {
    return lazy {
        value.createOperation().also {
            monop?.enqueue(it)
        }
    }
}
