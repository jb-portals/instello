package in.instello.annotations;

import io.github.thibaultmeyer.cuid.CUID;
import java.io.Serializable;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class CuidGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object) {
    return CUID.randomCUID2().toString();
  }
}
