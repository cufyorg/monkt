/*
 *	Copyright 2022 cufy.org
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
package org.cufy.monkt.internal

import org.cufy.bson.BsonType
import org.cufy.bson.BsonValue
import org.cufy.monkt.*
import org.cufy.monkt.schema.*

/**
 * A builder for creating a [ScalarDecoder]
 *
 * @author LSafer
 * @since 2.0.0
 */
@InternalMonktApi
open class ScalarDecoderBuilderImpl<T> : ScalarDecoderBuilder<T> {
    @AdvancedMonktApi
    override var types: MutableList<BsonType> = mutableListOf()

    @AdvancedMonktApi
    override val canDecodeBlocks: MutableList<(BsonValue) -> Boolean> = mutableListOf()

    @AdvancedMonktApi
    override var decodeBlock: ((BsonValue) -> T)? = null

    @AdvancedMonktApi
    override val deferred: MutableList<() -> Unit> = mutableListOf()

    @OptIn(AdvancedMonktApi::class)
    override fun build(): ScalarDecoder<T> {
        deferred.forEach { it() }
        deferred.clear()
        return ScalarDecoderImpl(
            types = types.toList(),
            decodeBlock = decodeBlock
                ?: error("decodeBlock is required but was not provided"),
            canDecodeBlock = canDecodeBlocks.toList().let {
                { bsonValue -> it.any { it(bsonValue) } }
            }
        )
    }
}

/**
 * A builder for creating an [ArraySchema]
 *
 * @author LSafer
 * @since 2.0.0
 */
@InternalMonktApi
open class ArraySchemaBuilderImpl<T> : ArraySchemaBuilder<T> {
    @AdvancedMonktApi
    override val options: MutableList<Option<List<T>, List<T>, *>> = mutableListOf()

    @AdvancedMonktApi
    override val staticOptions: MutableList<Option<Unit, Unit, *>> = mutableListOf()

    @AdvancedMonktApi
    override val decoders: MutableList<Decoder<out T>> = mutableListOf()

    @AdvancedMonktApi
    override var finalDecoder: Decoder<out T>? = null

    @AdvancedMonktApi
    override val encoders: MutableList<Encoder<in T>> = mutableListOf()

    @AdvancedMonktApi
    override var finalEncoder: Encoder<in T>? = null

    @AdvancedMonktApi
    override var schema: Lazy<Schema<T>>? = null // REQUIRED

    @AdvancedMonktApi
    override val onDecode: MutableList<ArraySchemaCodecBlock<T>> = mutableListOf()

    @AdvancedMonktApi
    override val onEncode: MutableList<ArraySchemaCodecBlock<T>> = mutableListOf()

    @AdvancedMonktApi
    override val deferred: MutableList<() -> Unit> = mutableListOf()

    @OptIn(AdvancedMonktApi::class, InternalMonktApi::class)
    override fun build(): ArraySchema<T> {
        deferred.forEach { it() }
        deferred.clear()
        return ArraySchemaImpl(
            schema = schema?.value
                ?: error("schema is required but was not provided"),
            options = options.toList(),
            staticOptions = staticOptions.toList(),
            decoders = decoders.toList(),
            finalDecoder = finalDecoder,
            encoders = encoders.toList(),
            finalEncoder = finalEncoder,
            onEncode = onEncode.toList().takeIf { it.isNotEmpty() }?.let {
                { it.forEach { it() } }
            },
            onDecode = onDecode.toList().takeIf { it.isNotEmpty() }?.let {
                { it.forEach { it() } }
            }
        )
    }
}

/**
 * A builder for creating a [ScalarSchema]
 *
 * @author LSafer
 * @since 2.0.0
 */
@InternalMonktApi
open class ScalarSchemaBuilderImpl<T> : ScalarSchemaBuilder<T> {
    @AdvancedMonktApi
    override var types: MutableList<BsonType> = mutableListOf()

    @AdvancedMonktApi
    override var canDecodeBlocks: MutableList<(BsonValue) -> Boolean> = mutableListOf()

    @AdvancedMonktApi
    override var decodeBlock: ((BsonValue) -> T)? = null

    @AdvancedMonktApi
    override val canEncodeBlocks: MutableList<(Any?) -> Boolean> = mutableListOf()

    @AdvancedMonktApi
    override var encodeBlock: ((T) -> BsonValue)? = null

    @AdvancedMonktApi
    override val deferred: MutableList<() -> Unit> = mutableListOf()

    @OptIn(InternalMonktApi::class, AdvancedMonktApi::class)
    override fun build(): ScalarSchema<T> {
        deferred.forEach { it() }
        deferred.clear()
        return ScalarSchemaImpl(
            types = types.toList(),
            decodeBlock = decodeBlock
                ?: error("decodeBlock is required but was not provided"),
            encodeBlock = encodeBlock
                ?: error("encodeBlock is required but was not provided"),
            canDecodeBlock = canDecodeBlocks.toList().let {
                { bsonValue -> it.any { it(bsonValue) } }
            },
            canEncodeBlock = canEncodeBlocks.toList().let {
                { value -> it.any { it(value) } }
            }
        )
    }
}

/**
 * A builder for creating an [EnumSchema]
 *
 * @author LSafer
 * @since 2.0.0
 */
@InternalMonktApi
open class EnumSchemaBuilderImpl<T> : EnumSchemaBuilder<T> {
    @AdvancedMonktApi
    override val deferred: MutableList<() -> Unit> = mutableListOf()

    @AdvancedMonktApi
    override val values: MutableMap<BsonValue, T> = mutableMapOf()

    @OptIn(AdvancedMonktApi::class, InternalMonktApi::class)
    override fun build(): EnumSchema<T> {
        deferred.forEach { it() }
        deferred.clear()
        return EnumSchemaImpl(
            values = values.toMap()
        )
    }
}

/**
 * A builder for creating a [FieldDefinition].
 *
 * @author LSafer
 * @since 2.0.0
 */
@InternalMonktApi
open class FieldDefinitionBuilderImpl<T : Any, M> : FieldDefinitionBuilder<T, M> {
    @AdvancedMonktApi
    override val options: MutableList<Option<T, M?, *>> = mutableListOf()

    @AdvancedMonktApi
    override val staticOptions: MutableList<Option<Unit, Unit, *>> = mutableListOf()

    @AdvancedMonktApi
    override val decoders: MutableList<Decoder<out M>> = mutableListOf()

    @AdvancedMonktApi
    override var finalDecoder: Decoder<out M>? = null

    @AdvancedMonktApi
    override val encoders: MutableList<Encoder<in M>> = mutableListOf()

    @AdvancedMonktApi
    override var finalEncoder: Encoder<in M>? = null

    @AdvancedMonktApi
    override var schema: Lazy<Schema<M>>? = null // REQUIRED

    @AdvancedMonktApi
    override val onEncode: MutableList<FieldDefinitionCodecBlock<T, M>> = mutableListOf()

    @AdvancedMonktApi
    override val onDecode: MutableList<FieldDefinitionCodecBlock<T, M>> = mutableListOf()

    @AdvancedMonktApi
    override val deferred: MutableList<() -> Unit> = mutableListOf()

    @AdvancedMonktApi
    override var name: String? = null // REQUIRED

    @AdvancedMonktApi
    override var getter: FieldDefinitionGetter<T, M>? = null // REQUIRED

    @AdvancedMonktApi
    override var setter: FieldDefinitionSetter<T, M>? = null // REQUIRED

    @OptIn(AdvancedMonktApi::class, InternalMonktApi::class)
    override fun build(): FieldDefinition<T, M> {
        deferred.forEach { it() }
        deferred.clear()
        return FieldDefinitionImpl(
            name = name
                ?: error("name is required but was not provided"),
            lazySchema = schema
                ?: error("schema is required but was not provided"),
            getter = getter
                ?: error("getter is required but was not provided"),
            setter = setter
                ?: error("setter is required but was not provided"),
            options = options.toList(),
            staticOptions = staticOptions.toList(),
            decoders = decoders.toList(),
            finalDecoder = finalDecoder,
            encoders = encoders.toList(),
            finalEncoder = finalEncoder,
            onEncode = onEncode.toList().takeIf { it.isNotEmpty() }?.let {
                { it.forEach { it() } }
            },
            onDecode = onDecode.toList().takeIf { it.isNotEmpty() }?.let {
                { it.forEach { it() } }
            }
        )
    }
}

/**
 * A builder for creating an [ObjectSchema].
 *
 * @author LSafer
 * @since 2.0.0
 */
@InternalMonktApi
open class ObjectSchemaBuilderImpl<T : Any> : ObjectSchemaBuilder<T> {
    @AdvancedMonktApi
    override val options: MutableList<Option<T, T, *>> = mutableListOf()

    @AdvancedMonktApi
    override val staticOptions: MutableList<Option<Unit, Unit, *>> = mutableListOf()

    @AdvancedMonktApi
    override val onEncode: MutableList<ObjectSchemaCodecBlock<T>> = mutableListOf()

    @AdvancedMonktApi
    override val onDecode: MutableList<ObjectSchemaCodecBlock<T>> = mutableListOf()

    @AdvancedMonktApi
    override val deferred: MutableList<() -> Unit> = mutableListOf()

    @AdvancedMonktApi
    override var constructor: ObjectSchemaConstructor<T>? = null // REQUIRED

    @AdvancedMonktApi
    override val fields: MutableList<FieldDefinition<T, *>> = mutableListOf()

    @OptIn(AdvancedMonktApi::class, InternalMonktApi::class)
    override fun build(): ObjectSchema<T> {
        deferred.forEach { it() }
        deferred.clear()
        return ObjectSchemaImpl(
            constructor = constructor
                ?: error("constructor is required but was not provided"),
            fields = fields.toList(),
            options = options.toList(),
            staticOptions = staticOptions.toList(),
            onEncode = onEncode.toList().takeIf { it.isNotEmpty() }?.let {
                { it.forEach { it() } }
            },
            onDecode = onDecode.toList().takeIf { it.isNotEmpty() }?.let {
                { it.forEach { it() } }
            }
        )
    }
}
