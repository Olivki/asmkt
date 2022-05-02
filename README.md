## PROJECT_NAME

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras ligula leo, sodales dignissim hendrerit ac, fringilla ut massa. Praesent in arcu nunc. Mauris suscipit fermentum varius. Quisque nec ex dapibus orci accumsan egestas. Maecenas consectetur quis magna ac dapibus. Mauris condimentum in libero quis pellentesque. Sed gravida mollis lacinia. Nulla vel metus nec nulla venenatis dapibus.

## Installation

Gradle

- Groovy

  ```groovy
  repositories {
      // maven { url "https://dl.bintray.com/olivki/kanon" } // If not yet accepted into jCenter.
    	// jcenter() // If accepted into jCenter.
  }
  
  dependencies {
      implementation "GROUP_ID:ARTIFACT_ID:LATEST_VERSION"
  }
  ```

- Kotlin

  ```kotlin
  repositories {
      // maven(url = "https://dl.bintray.com/olivki/kanon") // If not yet accepted into jCenter.
      // jcenter() // If accepted into jCenter.
  }
  
  dependencies {
      implementation(group = "GROUP_ID", name = "ARTIFACT_ID", version = "LATEST_VERSION")
  }
  ```

Maven

```xml
<dependency>
    <groupId>GROUP_ID</groupId>
    <artifactId>ARTIFACT_ID</artifactId>
    <version>LATEST_VERSION</version>
    <type>pom</type>
</dependency>

```

## License

````
Copyright {CURRENT_YEAR} Oliver Berg

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````