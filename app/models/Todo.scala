package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag

import javax.inject.Inject
import scala.concurrent._

case class Todo(id: Long, name: String, isComplete: Boolean)

case class TodoFromData(name: String, isComplete: Boolean)

object TodoForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "isComplete" -> boolean
    )(TodoFromData.apply)(TodoFromData.unapply)
  )
}

class TodoTableDef(tag: Tag) extends Table[Todo](tag, "todo") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def isComplete = column[Boolean]("isComplete")

  override def * = (id, name, isComplete) <> (Todo.tupled, Todo.unapply)
}

class TodoList @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  var todoList = TableQuery[TodoTableDef]

  def add(todoItem: Todo): Future[String] = {
    dbConfig.db
      .run(todoList += todoItem)
      .map(res => "TodoItem successfully added")
      .recover {
        case ex: Exception => {
          printf(ex.getMessage())
          ex.getMessage
        }
      }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(todoList.filter(_.id === id).delete)
  }

  def update(todoItem: Todo): Future[Int] = {
    dbConfig.db.run(
      todoList
        .filter(_.id === todoItem.id)
        .map(x => (x.name, x.isComplete))
        .update(todoItem.name, todoItem.isComplete)
    )
  }

  def get(id: Long): Future[Option[Todo]] = {
    dbConfig.db.run(todoList.filter(_.id === id).result.headOption)
  }

  def listAll(): Future[Seq[Todo]] = {
    dbConfig.db.run(todoList.result)
  }
}
