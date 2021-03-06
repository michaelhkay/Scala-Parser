package ftanml.types

import ftanml.objects.{FtanValue, FtanNumber, SizedObject}

class MaxSizeType(size: FtanNumber) extends FtanType {
  def matches(value: FtanValue) = {
    value match {
      case v : SizedObject => v.size <= size.value
      case _ => false
    }
  }
}