package repo

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

@Serializable
class Worker (
    val workername: String,
    override var id: Int = -1
) : Item

class WorkersTable : ItemTable<Worker>() {
    val workername = varchar("workername", 50)
    override fun fill(builder: UpdateBuilder<Int>, item: Worker) {
        builder[workername] = item.workername
    }

    override fun readResult(result: ResultRow) =
        Worker(
            result[workername],
            result[id].value
        )
}

val workersTable = WorkersTable()
