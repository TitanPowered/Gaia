import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class GaiaPlatformExtension @Inject constructor() {
  abstract val productionJar: RegularFileProperty
}
