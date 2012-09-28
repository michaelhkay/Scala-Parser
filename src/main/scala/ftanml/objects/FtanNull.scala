package ftanml.objects

import java.io.Writer
import ftanml.streams.Acceptor

case object FtanNull extends FtanValue {
  def apply()=this //Allow writing FtanNull() instead of FtanNull
  override def writeFtanML(writer: Writer) {
    writer.append("null")
  }
  override def writeJson(writer: Writer) {
    writer.append("null")
  }

  override def send(acceptor: Acceptor) {
    acceptor.processNull()
  }
}