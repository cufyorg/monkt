package org.cufy.monktop

import kotlinx.coroutines.runBlocking
import org.cufy.bson.BsonDocument
import org.cufy.bson.Id
import org.cufy.codec.*
import org.cufy.mongodb.SET
import org.cufy.mongodb.drop
import org.cufy.monop.*
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.util.UUID.randomUUID
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleTest {
    lateinit var client: OpClient

    @BeforeEach
    fun before() {
        runBlocking {
            val connectionString = "mongodb://localhost"
            val name = "monop-example-test-${randomUUID()}"
            client = createOpClient(connectionString, name)
        }
    }

    @AfterEach
    fun after() {
        runBlocking {
            client.defaultDatabase()!!.drop()
        }
    }

    @Test
    fun `use simple read and write with codec`() {
        runBlocking {
            val input = TransactionFragment(
                id = Id(),
                from = "here",
                to = "there",
                value = BigDecimal("12.42")
            )

            val inputDocument = input encode TransactionFragmentCodec

            Transaction.insertOne(inputDocument)(client)

            val outputDocument = Transaction.find()(client).single()

            val output = outputDocument decode TransactionFragmentCodec

            assertEquals(input, output)
        }
    }

    @Test
    fun `use of from and into infix`() {
        runBlocking {
            val id = Id<Transaction>()

            val document1 = BsonDocument {
                Transaction.Id by id
                Transaction.Value by BigDecimal.ONE
            }

            Transaction.insertOne(document1)(client)

            val projection1 = TransactionProjection(document1)

            assertEquals(BigDecimal.ONE, projection1.value)

            Transaction.updateOneById(id, {
                SET { Transaction.Value by BigDecimal.TEN }
            })(client)

            val document2 = Transaction.findOneById(id)(client)!!

            val projection2 = TransactionProjection(document2)

            assertEquals(BigDecimal.TEN, projection2.value)
        }
    }
}

object Transaction : OpCollection, DocumentCodecOf<TransactionProjection> {
    override val name = "Transaction"
    override val codec = Codec {
        encodeCatching { it: TransactionProjection -> it.element }
        decodeCatching { it: BsonDocument -> TransactionProjection(it) }
    }

    val Id = FieldCodec("_id") { Id<Transaction>() }
    val From = FieldCodec("from") { String.Nullable }
    val To = FieldCodec("to") { String.Nullable }
    val Value = FieldCodec("value") { BigDecimal }
}

class TransactionProjection(override val element: BsonDocument) : DocumentProjection {
    val value by Transaction.Value from element
}

data class TransactionFragment(
    val id: Id<Transaction>,
    val from: String?,
    val to: String?,
    val value: BigDecimal
)

val TransactionFragmentCodec = Codec {
    encodeCatching { it: TransactionFragment ->
        BsonDocument {
            Transaction.Id by it.id
            Transaction.From by it.from
            Transaction.To by it.to
            Transaction.Value by it.value
        }
    }
    decodeCatching { it: BsonDocument ->
        TransactionFragment(
            id = it[Transaction.Id],
            from = it[Transaction.From],
            to = it[Transaction.To],
            value = it[Transaction.Value]
        )
    }
}
