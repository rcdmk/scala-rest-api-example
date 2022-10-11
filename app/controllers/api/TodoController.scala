package controllers.api

import models.Todo
import play.api.libs.json._
import play.api.mvc._

import javax.inject._
import services.TodoService
import scala.concurrent.ExecutionContext.Implicits.global
import models.TodoForm
import scala.concurrent.Future

class TodoController @Inject() (
    cc: ControllerComponents,
    todoService: TodoService
) extends AbstractController(cc) {
  implicit val todoFormat = Json.format[Todo]

  def getAll() = Action.async { implicit request: Request[AnyContent] =>
    todoService.listAllItems() map { items =>
      Ok(Json.toJson(items))
    }
  }

  def getById(id: Long) = Action.async {
    implicit request: Request[AnyContent] =>
      todoService.getItem(id) map { item =>
        item match {
          case Some(todo) => Ok(Json.toJson(todo))
          case None       => NotFound("Not Found")
        }
      }
  }

  def add() = Action.async { implicit request: Request[AnyContent] =>
    TodoForm.form
      .bindFromRequest()
      .fold(
        errorForm => {
          errorForm.errors.foreach(println)
          Future.successful(BadRequest("Error!"))
        },
        data => {
          val newTodoItem = Todo(0, data.name, data.isComplete)
          todoService
            .addItem(newTodoItem)
            .map(_ => Created)
        }
      )
  }

  def update(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    TodoForm.form
      .bindFromRequest()
      .fold(
        errorForm => {
          errorForm.errors.foreach(println)
          Future.successful(BadRequest("Error!"))
        },
        data => {
          val todoItem = Todo(id, data.name, data.isComplete)
          todoService
            .updateItem(todoItem)
            .map(_ => NoContent)
        }
      )
  }

  def delete(id: Long) = Action.async { implicit request: Request[AnyContent] =>
    {
      todoService
        .deleteItem(id)
        .map(_ => NoContent)
    }
  }
}
