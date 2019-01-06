package dynamodb

import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item}
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.util.TableUtils
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.collection.JavaConverters._

class DynamoDbLocalTest extends FunSuite with Matchers with BeforeAndAfterAll {

  val tableName = "testTable"
  val keyName = "testKey"
  val attributeName = "Value"

  val client = DynamoDBEmbedded.create.amazonDynamoDB
  val dynamoDB = new DynamoDB(client)
  val table = dynamoDB.getTable(tableName)

  override def beforeAll() = {
    createTableIfNotExists(tableName, keyName)
  }

  def createTableIfNotExists(tableName: String, keyName: String): Unit = {
    val attributeDefinitions = List(new AttributeDefinition().withAttributeName(keyName).withAttributeType("S"))
    val keySchema = List(new KeySchemaElement().withAttributeName(keyName).withKeyType(KeyType.HASH))

    val createResumeTableRequest = new CreateTableRequest().
      withTableName(tableName)
      .withAttributeDefinitions(attributeDefinitions.asJava)
      .withKeySchema(keySchema.asJava)
      .withProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L))

    TableUtils.createTableIfNotExists(client, createResumeTableRequest)
  }

  def getItem(keyName: String, keyValue: String): String =
    table.getItem(keyName, keyValue).getString(attributeName)

  def putItem(keyName: String, keyValue: String, data: String): Unit = {
    val item = new Item()
      .withPrimaryKey(keyName, keyValue)
      .withString(attributeName, data)
   table.putItem(item)
  }

  test("test dynamodb local") {

    val string = "top secret data"
    val keyValue = "testValue"
    putItem(keyName, keyValue, string)

    val result = getItem(keyName, keyValue)

    result should equal(string)
  }

}
