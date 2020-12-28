package repo

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

@Serializable
class Cart (
    val workerId: Int,
    val drugId: Int,
    override var id: Int = -1
) : Item

class CartsTable : ItemTable<Cart>() {
    val workerId = integer("workerId").references(workersTable.id)
    val drugId = integer("drugId").references(drugsTable.id)
    override fun fill(builder: UpdateBuilder<Int>, item: Cart) {
        builder[workerId] = item.workerId
        builder[drugId] = item.drugId
    }

    override fun readResult(result: ResultRow) =
        Cart(
            result[workerId],
            result[drugId],
            result[id].value
        )
}

val cartsTable = CartsTable()
