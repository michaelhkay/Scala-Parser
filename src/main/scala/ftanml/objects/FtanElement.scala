package ftanml.objects

import java.io.Writer

import scala.collection.mutable.LinkedHashMap
import ftanml.streams.Acceptor

object FtanElement extends FtanElement(new LinkedHashMap[FtanString,FtanValue]) {
  val NAME_KEY = new FtanString("name")
  val CONTENT_KEY = new FtanString("content")
  
  def apply(attributes: (FtanString,FtanValue)*) = new FtanElement(attributes.toMap)
}

case class FtanElement(attributes: LinkedHashMap[FtanString, FtanValue]) extends FtanValue with SizedObject {
  import FtanElement._
  
  def this(attributes: Map[FtanString,FtanValue]) = this(new LinkedHashMap++=attributes)

  def name: Option[String] = {
     attributes.get(NAME_KEY) match {
      case a : Some[FtanValue] => Some(a.get.asInstanceOf[FtanString].value)
      case _ => None
    }
  }

  def content: FtanArray = {
     attributes.get(CONTENT_KEY) match {
      case a : Some[FtanValue] => a.get.asInstanceOf[FtanArray]
      case _ => FtanArray(Nil)
    }
  }

  def isEmptyContent: Boolean = content.values.isEmpty

  def isSimpleContent: Boolean = content.values.size == 1 && content.values(0).isInstanceOf[FtanString]

  def isElementOnlyContent: Boolean = !content.values.exists(!_.isInstanceOf[FtanElement])

  def isMixedContent: Boolean = content.values.exists(_.isInstanceOf[FtanElement]) && content.values.exists(_.isInstanceOf[FtanString])



  override def writeFtanML(writer: Writer) {
    var space_needed = false

    // Opening bracket
    writer.append("<");

    // Write name, if existing
    attributes.get(NAME_KEY) map {
      case string: FtanString =>
        string.writeFtanMLName(writer)
        space_needed = true
      case _ =>
    }

    //Write all attributes (except name and content attribute)
    for ((key, value) <- attributes) value match {
      //Ignore name attribute, if valid
      case string: FtanString if key == NAME_KEY =>
      //Ignore content attribute, if valid
      case array: FtanArray if key == CONTENT_KEY && array.isValidElementContent =>
      //Write all other attributes
      case value: FtanValue =>
        if (space_needed)
          writer.append(" ")
        key.writeFtanMLName(writer)
        writer.append("=")
        value.writeFtanML(writer)
        space_needed = true
    }

    //If there is valid content, write it
    attributes.get(CONTENT_KEY) map {
      case content: FtanArray if content.isValidElementContent =>
        writer.append("|")
        content.writeFtanMLContent(writer)
      case _ =>
    }

    // Closing bracket
    writer.append(">");
  }

  def writeFtanMLContent(writer: Writer) {
    writeFtanML(writer)
  }

  override def send(acceptor: Acceptor) {
    acceptor.processStartElement(name)
    for ((key, value) <- attributes) value match {
      //Ignore name attribute, if valid
      case string: FtanString if key == NAME_KEY =>
      //Ignore content attribute, if valid
      case array: FtanArray if key == CONTENT_KEY && array.isValidElementContent =>
      //Write all other attributes
      case value: FtanValue =>
        acceptor.processAttributeName(key.value)
        value.send(acceptor)
    }
    if (!isEmptyContent) {
      acceptor.processStartContent(isElementOnlyContent)
      content.values.foreach {_.send(acceptor)}
    }
    acceptor.processEndElement()
  }

  override def writeJson(writer: Writer) {
    def writeAttribute(attr:(FtanString,FtanValue)) {
      attr._1.writeJson(writer)
      writer.append(":")
      attr._2.writeJson(writer)
    }
    
    // Opening bracket
    writer.append("{");

    //Write all attributes
    if (attributes.size >= 1) {
      writeAttribute(attributes.head)
      for (element <- attributes.tail) {
        writer.append(",")
        writeAttribute(element)
      }
    }

    // Closing bracket
    writer.append("}");
  }

  override def equals(that: Any) =
    that.isInstanceOf[FtanElement] &&
      attributes.size == that.asInstanceOf[FtanElement].attributes.size &&
      attributes.equals(that.asInstanceOf[FtanElement].attributes)


  override def hashCode() : Int = {
    attributes.hashCode()
  }

  def size = {
  	var size = attributes.size
  	
  	//don't want to count name and content as attributes
  	if(attributes.contains(NAME_KEY))
  		size -= 1
  	if(attributes.contains(CONTENT_KEY))
  		size -= 1

  	size
  }
}